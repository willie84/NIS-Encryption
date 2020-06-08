
import java.io.*;
import java.net.*;
import java.util.*;

public class clientHandler extends Thread{
	private Socket clientSocket;
	//static ArrayList<Socket> socketList = new ArrayList<Socket>();
	static ArrayList<Socket> imageResponse = new ArrayList<Socket>();
	static Object lock = new Object();
	private Socket socket;
    static String filename;
	public GenerateRSAKeys generateServerKeys;
  
	//constructor that takes a client socket
	public clientHandler(Socket socket){
		this.clientSocket = socket;
		generateServerKeys = new GenerateRSAKeys("serverPublicKey.txt", "serverPrivateKey.txt");
	}

    // Method to send file from server to client
	public void sendFile(String fileName, Socket sock) {
		try{
			File myFile = new File(fileName);
			byte[] mybytearray = new byte[(int) myFile.length()];
			InputStream in = new FileInputStream(myFile);
			OutputStream os = sock.getOutputStream();
			int count;
			while ((count = in.read(mybytearray)) > 0 ){
				os.write(mybytearray, 0, count);
			}
			os.flush();
		}
		catch(Exception e){System.out.println(e);}
	}
    
    // Method to receive file from client
	public void receiveFile(String fileName, int fileLength) throws Exception{
		byte[] bytes = new byte[8192];
		DataInputStream is = new DataInputStream(socket.getInputStream());
		FileOutputStream fos = new FileOutputStream(fileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		int bytesRead =0;
		int count ;
		while ( bytesRead < fileLength)
    		{
					count = is.read(bytes);
					bytesRead += count;
					bos.write(bytes,0,count);

    		}

		bos.close();

	}

	// thread to send messages to client
	Thread sendMessage = new Thread(new Runnable()
	{
		@Override
		public void run() {
			final Scanner scn = new Scanner(System.in);
			try {
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				while (true) {
					// read the message to deliver.
					String msg = scn.nextLine();

					//encrypt the message
					String encryptedMsg;
					encryptedMsg = generateServerKeys.encrypt("serverPublicKey.txt", msg);
					System.out.println(encryptedMsg);
					// write on the output stream of the client
					out.println("Server: "+encryptedMsg);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	});

	public void run(){
		//start the send message thread
		sendMessage.start();

		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String input;
			while(true){
				input = in.readLine();
        		if (input == null || input.equals("quit")){break;}
        		//print received message to server screen
        		System.out.println(input);
			}
			in.close();
			socket.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}

}
