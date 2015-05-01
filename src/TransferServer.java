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
		//得到当前程序所在目录

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
					System.out.println("服务器关闭");
				default:
					System.out.println("命令错误");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	void download() throws IOException{
//		准备缓存
		int bufferSize = 8192;
		byte[] buf = new byte[bufferSize];
		
		System.out.println("建立socket链接，提供下载服务");
//		  准备文件
		File filesendingdir = new File(properties.getProperty("user.dir")+"\\sending");
		File[] files = filesendingdir.listFiles();
		int num = files.length;
		for(int i=0; i < num; i++){
			System.out.println(files[i].getName()+ "文件长度:"+ (int) files[i].length());
		}
		
//		传输参数
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		dos.writeInt(num);
		dos.flush();
//		循环传输文件
		for(int i = 0;i < num;i++){
			dos.writeUTF(files[i].getName());
			dos.flush();
			dos.writeLong(files[i].length());
			dos.flush();
			
			DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(files[i])));
			System.out.println(files[i].getName()+"文件开始传输");
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
			System.out.println(files[i].getName()+"文件传输完成");
		}
		socket.close();
	}
	
	
	
	void upload() throws IOException{
//		准备缓存和参数
		int bufferSize = 8192;
		byte[] buf = new byte[bufferSize];
		int num = 0;
		String savepath = null;
		String name = null;
		long len = 0;
		
		System.out.println("建立socket链接");
		
//		接收参数
		DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		num = dis.readInt();
		
//		接收文件参数和文件
		for(int i = 0;i < num;i++){
			savepath = fileReceving.getPath();
			name = dis.readUTF();
			savepath += ("\\"+name);
			len = dis.readLong();
			DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(savepath)));
			
			System.out.println("开始接收文件："+name);
			
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
			System.out.println(name+"文件接收完成");
		}
		socket.close();
	}




	public static void main(String[] args) {

		new TransferServer().start();
	}

}
