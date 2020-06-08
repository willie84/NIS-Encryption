import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client implements Runnable{
	static volatile BufferedReader in  = null;
	static String request = "";
	static Socket socket;
	static PrintWriter out;
	static volatile boolean flag = false;
	static Object lock = new Object();
	static Object lock2 = new Object();
	static GenerateRSAKeys generateRSAKeys;
    
    // sending image to serverThread
	public static void sendFile(String fileName, Socket sock) throws Exception{
		File myFile = new File(fileName);
		byte[] mybytearray = new byte[ (int) myFile.length()];
		InputStream in = new FileInputStream(myFile);
		OutputStream os = sock.getOutputStream();
		int count;
		while ((count = in.read(mybytearray)) > 0 ){
			os.write(mybytearray, 0, count);
		}
		os.flush();
	}
    
    // receiving image from serverThread
	public void receiveFile(String fileName, int fileSize){
		try{
			byte[] bytes = new byte[8192];
			DataInputStream is = new DataInputStream(socket.getInputStream());
			FileOutputStream fos = new FileOutputStream("clientSide"+fileName);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			int bytesRead =0;
			int count ;
			while ( bytesRead < fileSize)
			{
				count = is.read(bytes);
				bytesRead += count;
				bos.write(bytes,0,count);

			}
			bos.flush();
			bos.close();
		}
		catch (Exception e){System.out.println(e);}
	}

    // main method does writing in one thread
	public static void main(String[] args){
		//create the keys first
		generateRSAKeys = new GenerateRSAKeys("clientPublicKey.txt", "clientPrivateKey");
		generateRSAKeys.generate();
		System.out.println("Keys generated.");

		//connect to server
		try{
			//the server's socket
			socket = new Socket("LocalHost", 1342);
			out = new PrintWriter(socket.getOutputStream(), true);
			Scanner sc = new Scanner(System.in);
			System.out.println("Enter your name");
			String name = sc.nextLine();
			out.println(name + " has joined the chat");
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("Type a message");
			String message;
			int fileLength;
            // new thread to read messages
			new Thread(new Client()).start();
            while (!(message = sc.nextLine()).equals("quit")){ // user quits on client side
				if(message.equals("./sendimage")){ //if user wants to send image
						System.out.println("Enter file name:");
						String filename = sc.nextLine();
						File myFile = new File(filename);
						fileLength = (int) myFile.length();
						out.println("sendingImage...|"+filename+"~"+fileLength );
						try{
							sendFile(filename, socket);
							synchronized(lock){
								try{
										lock.wait();
								}
								catch(InterruptedException e){
									System.out.println(e);
								}
							}
						}
						catch(Exception e){
							System.out.println(e);
						}

				}
				else if(message.equals("./accept")){
						synchronized (lock2){
							flag = true;
							out.println(message);
							lock2.notify();
						}

				}
				else if(message.equals("./reject")){
					synchronized (lock2){
						flag = false;
						lock2.notify();
					}
				}
				else{
					out.println(name + ": "+ message);
				}
      		}
			out.println(name + " has left the chat");
            out.close();
			socket.close();

		}
		catch (UnknownHostException e){
    		System.out.println("Hostname error");
    	}
		catch (IOException e){
    		System.out.println("IO error");
    	}
	}

        @Override
	public void run(){
		try{
			//read messages sent from the server
			while(true){
				  String line = in.readLine();
				  if(line.equals("received")){
						synchronized(lock){
							lock.notify();
							continue;
						}
					}
					if (line.contains("download image?(./accept or ./reject)")){
						System.out.println(line+"B");
						synchronized(lock2){
							try{
									lock2.wait();

							}
							catch(InterruptedException e){
								System.out.println(e);
							}
							if (flag){ // if the client wants to receive
								String fileNamed = line.substring(line.indexOf("|")+1,line.indexOf("~"));
								int fileSize = Integer.parseInt(line.substring((line.indexOf("~")+1)));
								receiveFile(fileNamed,fileSize);
								System.out.println(fileNamed+" received");
								continue;
							}
							if (!flag){continue;} //client does not want to receive

						}
					}
					if (line.contains("sendingImage")){continue;}
					System.out.println(line);
				}
		}
		catch(Exception e){

		}

	}

}
