/*******************************************************
Progamming Assignment #1

--Credit--

Andranik Baghumyan
Eman Jaan
Sherry Wong
 *All code written are our own work unless otherwise stated.*

--Resources--
1) Do/While Loop: https://docs.oracle.com/javase/tutorial/java/nutsandbolts/while.html
2) File I/O: https://www.caveofprogramming.com/java/java-file-reading-and-writing-files-in-java.html

 *******************************************************/
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class test {
	private static int portNum;
	private static String ipAddr;
	
	public static ArrayList<Integer> pNumber = new ArrayList<Integer>();
	public static ArrayList<Integer> ID = new ArrayList<Integer>();
	public static ArrayList<String> desIP = new ArrayList<String>();
	public static ServerSocketChannel channel;

	public static int countId = 1;;
	public static Selector writeSelector;
	public static Selector readSelector;

	public static void main(String args[]) throws IOException {
		readSelector = Selector.open();
		writeSelector = Selector.open();
		
		if (args.length != 1) {
			System.exit(1);
		} else {
			setIP();
			setPort(args[0]);
			initShell();
		}
	}

	/*
	 * Returns void.
	 * 
	 * Setter that assigns the port number that program is running on to to portNum. 
	 * Port number is given as a command line argument and does not change.
	 *
	 * @param	inputPortNum	port number given by user via command line 
	 *
	 */
	public static void setPort(String inputPortNum) {
		portNum = Integer.parseInt(inputPortNum);
	}
	
	/*
	 * Returns void.
	 * 
	 * Setter that assigns the IP address to ipAddr.
	 * IP address is dynamic depending on router and does not change.
	 *
	 */
	public static void setIP() {
		try {
			ipAddr = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Returns void.
	 * 
	 * Main controller of the program.
	 * Uses threads to listen to connection requests and incoming messages, and
	 * allows shell to wait for user input. 
	 *
	 */
	public static void initShell() {
		new Thread(new Runnable() {
			public void run() {
				try {
					channel = ServerSocketChannel.open();
					channel.configureBlocking(false);
					InetSocketAddress hostAddress = new InetSocketAddress(
							getPort());

					channel.bind(hostAddress);

					int ops = channel.validOps();

					channel.register(readSelector, ops, null);

					while (true) {

						readSelector.select(1000);

						Set<SelectionKey> selectedKeys = readSelector
								.selectedKeys();

						Iterator<SelectionKey> keyIterator = selectedKeys
								.iterator();

						while (keyIterator.hasNext()) {
							SelectionKey key1 = (SelectionKey) keyIterator
									.next();

							if (key1.isAcceptable()) {

								SocketChannel client = channel.accept();
								client.configureBlocking(false);
								client.register(readSelector,
										SelectionKey.OP_READ);
								client.register(writeSelector,
										SelectionKey.OP_WRITE);

								String[] clientIPAndPort = client
										.getRemoteAddress().toString()
										.substring(1).split(":");
								String clientIP = clientIPAndPort[0];
								int clientPort = Integer
										.parseInt(clientIPAndPort[1]);

								pNumber.add(clientPort - 1);
								ID.add(countId);
								desIP.add(clientIP);
								countId++;

							} else if (key1.isReadable()) {
								SocketChannel client = (SocketChannel) key1
										.channel();
								ByteBuffer buffer = ByteBuffer.allocate(100);

								int bytesRead = client.read(buffer);
								Socket socket = client.socket();
								int listeningPort = (socket.getPort() - 1);
								String clientIP = client.getRemoteAddress()
										.toString().substring(1).split(":")[0];

								String output = new String(buffer.array())
										.trim();
								if (output.equals("Disconnecting")) {
									channel.close();
									System.exit(1);
								}
								System.out.println("Message received from "
										+ clientIP + ":");
								System.out.println(output);
								System.out.print(">> ");
							}
							keyIterator.remove();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		while (true) {

			System.out.print(">> ");
			Scanner sc = new Scanner(System.in);
			String line;

			if ((line = sc.nextLine()) != null) {
				checkUserInput(line);
			}

		}
	}
	/*
	 * Returns void.
	 * 
	 * Checks the user input to the shell and calls the appropriate methods.
	 * 
	 * @param	userInput 	the user input from the command line with options
	 *
	 */

	public static void checkUserInput(String userInput) {
		String[] flags = userInput.split(" ");

		switch (flags[0]) {
		case "help":
			if (flags.length == 1) {
				displayHelp();
				break;
			} else {
				System.out.println("Incorrect use of command.");
				break;
			}
		case "myip":
			if (flags.length == 1) {
				System.out.println("The ip is " + getIP());
			} else {
				System.out.println("Incorrect use of command.");
			}
			break;
		case "myport":
			if (flags.length == 1) {
				System.out.println("The program runs on port number "
						+ getPort());
			} else {
				System.out.println("Incorrect use of command.");
			}
			break;
		case "connect":
			if (flags.length == 3) {
				String destinationIP = flags[1];
				int portNumber = Integer.parseInt(flags[2]);
				connectTcp(destinationIP, portNumber);

			} else {
				System.out.println("Incorrect use of command.");
			}
			break;
		case "list":
			if (flags.length == 1) {
				displayList();
			} else {
				System.out.println("Incorrect use of command.");
			}
			break;
		case "terminate":
			if (flags.length == 2) {
				int terminateId = Integer.parseInt(flags[1]);
				for (int i = 0; i < ID.size(); i++) {
					if (ID.get(i) == terminateId) {
						termMessage(desIP.get(i), pNumber.get(i));
						removeTheUser(terminateId);
						break;
					} else
						System.out.println("the id does not exist");
				}
			}

			else {
				System.out.println("Incorrect use of command.");
			}
			break;
		case "send":
			if (flags.length > 2) {

				int cId = Integer.parseInt(flags[1]);
				String message = "";

				for (int i = 2; i < flags.length; i++) {
					message += flags[i] + " ";
				}
				fSent(cId, message);

			} else {
				System.out.println("Incorrect use of command.");
			}
			break;
		case "exit":
			if (flags.length == 1) {
				exitApp();
			} else {
				System.out.println("Incorrect use of command.");
			}
			break;
		default:
			System.out.println("Command does not exist.");
			break;
		}
	}

	public static void termMessage(String desIP, int port) {

	}

	public static void fSent(int id, String message) {
		try {
			int indexOfId = ID.indexOf(id);
			String ipFromList = desIP.get(indexOfId);

			try {

				writeSelector.select();

				Set<SelectionKey> selectedKeys = writeSelector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
				ByteBuffer buffer = ByteBuffer.allocate(100);
				buffer.put(message.getBytes());
				buffer.flip();

				while (keyIterator.hasNext()) {
					SelectionKey key5 = (SelectionKey) keyIterator.next();
					String currentIPOnChannel = ((SocketChannel) key5.channel())
							.getRemoteAddress().toString().substring(1)
							.split(":")[0];

					if (key5.isWritable()) {
						if (currentIPOnChannel.equals(ipFromList)) {
							SocketChannel channel = (SocketChannel) key5
									.channel();
							try {
								channel.write(buffer);
							} catch (IOException e) {
								System.out.println("coun't send");
							}
						}
					}

				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Id doesn't exist");
				return;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Doesn't exist");
		}
	}

	public static void removeTheUser(int number) {

		int index = ID.indexOf(number);
		ID.remove(index);
		desIP.remove(index);
		pNumber.remove(index);
	}
//
//	public static void setPort(String inputPortNum) {
//		portNum = Integer.parseInt(inputPortNum);
//	}

	public static int getPort() {
		return portNum;
	}

//	public static void setIP() {
//		try {
//			ipAddr = Inet4Address.getLocalHost().getHostAddress();
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public static String getIP() {
		return ipAddr;
	}

	public static void displayHelp() {

		try {
			BufferedReader helpFile = new BufferedReader(new FileReader(
					"help.txt"));
			String line;
			while ((line = helpFile.readLine()) != null) {
				System.out.println(line);
			}
			helpFile.close();
		} catch (IOException ex) {
			System.out.println("Missing help.txt file.");
		}
	}

	public static void displayList() {

		System.out.println("id:\tIP address\t  Port No.");
		for (int i = 0; i < pNumber.size(); i++) {
			System.out.println(ID.get(i) + "\t" + desIP.get(i) + "\t  "
					+ pNumber.get(i));
		}
	}

	public static void connectTcp(String destinationIP, int portNumber) {

		ByteBuffer buffer = ByteBuffer.allocate(100);
		String messageForServer = "The connection to peer " + getIP()
				+ " is successfully established";
		String messageForMe = "The connection to peer " + destinationIP
				+ " is successfully established";
		buffer.put(messageForServer.getBytes());
		buffer.flip();
		try {

			SocketChannel connectChannel = SocketChannel.open();

			connectChannel.bind(new InetSocketAddress(getPort() + 1));

			connectChannel.connect(new InetSocketAddress(destinationIP,
					portNumber));

			connectChannel.configureBlocking(false);
			connectChannel.register(readSelector, SelectionKey.OP_READ);
			connectChannel.register(writeSelector, SelectionKey.OP_WRITE);
			connectChannel.write(buffer);
			System.out.println(messageForMe);

			pNumber.add(portNumber);
			ID.add(countId);
			desIP.add(destinationIP);
			countId++;

		} catch (IOException e) {
			System.out.println(e);
			System.out.println("Failure in connection");
		}
	}

	public static void exitApp() {
		System.out.println("Disconnecting");
		String message = "Disconnecting";
		for (int i = 0; i < ID.size(); i++) {
			fSent(ID.get(i), message);
		}
		try {
			channel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.exit(1);
	}
}
