import java.io.*;
import java.net.*;

public class Client {

	public static void main(String[] args) throws IOException

	{
		
		//UDP
		
		int clientPort = 8888;
		Socket clientSocket = null;
		PrintWriter toTheServer = null;
		BufferedReader fromTheServer = null;
		BufferedReader write = null;

		try {
			InetAddress host = InetAddress.getByName("localhost");

			clientSocket = new Socket(host, clientPort);
			System.out.println("Connecte "
					+ clientSocket.getRemoteSocketAddress() + " "
					+ clientSocket.getPort());

			toTheServer = new PrintWriter(clientSocket.getOutputStream(), true);

			fromTheServer = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));

			write = new BufferedReader(new InputStreamReader(System.in));

			String messageFromServer = fromTheServer.readLine();
			System.out.println(messageFromServer);

		} catch (IOException e) {
			System.out.println(e);
		}
		try {

			
		} finally {


			System.out.println("Ending the connection");

			toTheServer.close();
			fromTheServer.close();
			clientSocket.close();
		}

	}
}
