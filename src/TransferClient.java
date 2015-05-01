

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TransferClient {

	Socket socket= null;
	private String ip = "169.254.241.254";// ���óɷ�����IP
	private int port = 8000;
	String command = "upload";
	String savepath = "G:\\aa\\����";
	String filesavepath = null;
	String uploadpath = "G:\\aa\\�ϴ�";


	//  ������
	public TransferClient() {
		try {
			socket = new Socket(ip, port);

			switch (command) {
			case "download":
				download();
				break;
			case "upload":
				upload();
				break;
			case "close":
				closeserver();
			default:
				socket.close();
				break;
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	void upload() throws IOException{
//		׼������
		int bufferSize = 8192;
		byte[] buf = new byte[bufferSize];
		
		System.out.println("����socket���ӣ�׼���ϴ�");
//		  ׼���ļ�
		File filesending = new File(uploadpath);
		File[] files = filesending.listFiles();
		int num = files.length;
		for(int i=0; i < num; i++){
			System.out.println(files[i].getName()+ "�ļ�����:"+ (int) files[i].length());
		}
		
//		�������
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		dos.writeUTF("upload");
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
	}
	
	
	
	void download() throws IOException{
//		׼������Ͳ���
		int bufferSize = 8192;
		byte[] buf = new byte[bufferSize];
		int num = 0;
		String name = null;
		long len = 0;
		
		System.out.println("����socket����");
		
//		��������
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		dos.writeUTF("download");
		dos.flush();
//		���ղ���
		DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		num = dis.readInt();
		
//		�����ļ��������ļ�
		for(int i = 0;i < num;i++){
			filesavepath = savepath;
			name = dis.readUTF();
			filesavepath += ("\\"+name);
			System.out.println(filesavepath);
			len = dis.readLong();
			DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filesavepath)));
			
			System.out.println("��ʼ�����ļ���"+name);
			
			while(len>0){
				int read = 0;
				if(len>bufferSize){
					read = dis.read(buf);
				}else{
					read = dis.read(buf, 0, (int)len);
				}
				len -= read;
				fileOut.write(buf, 0, read);
			}
			fileOut.flush();
			fileOut.close();
			System.out.println(name+"�ļ��������");
		}
	}
	void closeserver() throws IOException{
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		dos.writeUTF("close");
		dos.flush();
	}


	
	
	

	public static void main(String arg[]) {
		new TransferClient();
	}
}