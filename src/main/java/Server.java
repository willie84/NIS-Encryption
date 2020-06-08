
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server{
	static Socket clientSocket;
	//static ArrayList<Socket> socketList = new ArrayList<Socket>();
	static ArrayList<Socket> imageResponse = new ArrayList<Socket>();
	static Object lock = new Object();
	private Socket socket;
	static String filename;
	static GenerateRSAKeys generateRSAKeys;

	//main method
	public static void main(String[] args) throws Exception{
		clientSocket = null;
		ServerSocket serverSocket = null;

		//create the server socket
		try {
			serverSocket = new ServerSocket(1342);
		}catch(IOException e){
			System.out.println("IO "+e);
		}

		//create the keys first
		generateRSAKeys = new GenerateRSAKeys("serverPublicKey.txt", "serverPrivateKey.txt");
		generateRSAKeys.generate();
		System.out.println("Keys generated.");

		//listen for connection request from client
		System.out.println("Listening for incoming client requests....");
		while (true) {
			try {
					clientSocket = serverSocket.accept();
					System.out.println("New client request received. Creating a new handler for this client...");
					clientHandler c = new clientHandler(clientSocket);
					c.start();
			}
			catch (IOException e)
			{
				System.out.println("IOaccept "+e);
			}
		}
	}
}
