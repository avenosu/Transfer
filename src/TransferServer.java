import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;


public class TransferServer {

	int port = 8000;
	Socket socket = null;
	String type = null;
	Properties properties = System.getProperties();
	File fileSending = new File(properties.getProperty("user.dir")+"\\sending");
	File fileReceving = new File(properties.getProperty("user.dir")+"\\Receving");

	void start() {
		//�õ���ǰ��������Ŀ¼

		System.out.println(properties.getProperty("user.dir"));
		fileSending.mkdir();
		fileReceving.mkdir();
		try {
			ServerSocket ss = new ServerSocket(port);
			while(true){
				socket = ss.accept();
				type = new DataInputStream(socket.getInputStream()).readUTF();
				

				switch (type) {
				case ("download"):
					download();
				break;
				case ("upload"):
					upload();
				break;
				case("close"):
					ss.close();
					System.out.println("�������ر�");
				default:
					System.out.println("�������");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	void download() throws IOException{
//		׼������
		int bufferSize = 8192;
		byte[] buf = new byte[bufferSize];
		
		System.out.println("����socket���ӣ��ṩ���ط���");
//		  ׼���ļ�
		File filesendingdir = new File(properties.getProperty("user.dir")+"\\sending");
		File[] files = filesendingdir.listFiles();
		int num = files.length;
		for(int i=0; i < num; i++){
			System.out.println(files[i].getName()+ "�ļ�����:"+ (int) files[i].length());
		}
		
//		�������
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		dos.writeInt(num);
		dos.flush();
//		ѭ�������ļ�
		for(int i = 0;i < num;i++){
			dos.writeUTF(files[i].getName());
			dos.flush();
			dos.writeLong(files[i].length());
			dos.flush();
			
			DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(files[i])));
			System.out.println(files[i].getName()+"�ļ���ʼ����");
			while(true){
				int read = 0;
				if(fis!=null){
					read = fis.read(buf);
				}
				if(read == -1){
					break;
				}
				dos.write(buf, 0, read);
			}
			dos.flush();
			fis.close();
			System.out.println(files[i].getName()+"�ļ��������");
		}
		socket.close();
	}
	
	
	
	void upload() throws IOException{
//		׼������Ͳ���
		int bufferSize = 8192;
		byte[] buf = new byte[bufferSize];
		int num = 0;
		String savepath = null;
		String name = null;
		long len = 0;
		
		System.out.println("����socket����");
		
//		���ղ���
		DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		num = dis.readInt();
		
//		�����ļ��������ļ�
		for(int i = 0;i < num;i++){
			savepath = fileReceving.getPath();
			name = dis.readUTF();
			savepath += ("\\"+name);
			len = dis.readLong();
			DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(savepath)));
			
			System.out.println("��ʼ�����ļ���"+name);
			
			while(len>0){
				int read = 0;
				if(len>bufferSize){
					read = dis.read(buf);
				}else{
					read = dis.read(buf, 0, (int)len);
				}
				len -=read;
				fileOut.write(buf, 0, read);
			}
			fileOut.flush();
			fileOut.close();
			System.out.println(name+"�ļ��������");
		}
		socket.close();
	}




	public static void main(String[] args) {

		new TransferServer().start();
	}

}
