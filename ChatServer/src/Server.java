import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Observer
{
	private ServerSocket serverSocket  = null;
	private Socket 	connectionToClient = null;
	private ArrayList<ClientConnection> clients = new ArrayList<ClientConnection>();

	public void initiate(int port) throws IOException
	{
		log("Setting up server socket...");
		serverSocket = new ServerSocket(port);
		log("Server socket setup complete.");
	}
	
	
	public void waitForConnections() throws IOException
	{
		while(true)
		{
			log("Waiting for connections...");
			
			try {
				connectionToClient = serverSocket.accept();
			} 
			catch (IOException e) {
				logError("IOException caught while trying to connect to client.");
			}
			
			// Creates a client connection
			ClientConnection tempClient = new ClientConnection(connectionToClient);
			
			// Adds Server as the Observer for that connection
			tempClient.setObserver(this);
			
			// Adds it to the clients list
			clients.add(tempClient);
			
			// And starts its thread
			Thread newThread = new Thread(tempClient);
			newThread.start();
			
			log("Connection to client established.");
			log("New thread: " + newThread.getName());
		}
	}
	
	public void update()
	{
		// Iterates through all the connections
		for(ClientConnection c : clients)
		{
			// Find the one message that is not null
			if(c.getReceivedMessage() != null)
			{
				// And sends it to all the clients
				sendMessageToClients(c.getReceivedMessage());
				
				// Sets the message to null so that it's not sent again until it's updated
				c.resetMessage();
				break;
			}
		}
	}
	
	private void sendMessageToClients(String message)
	{
		for(ClientConnection c: clients)
		{
			c.send(message);
		}
	}
	
	public void close()
	{
		log("Closing connection.");
		try{
			serverSocket.close();
		}
		catch(IOException e){
			logError("Failed to properly close the connection. " + e.getMessage());
		}	
		log("Connection closed.");
	}
	
	
	private void log(String message){
		System.out.println("Server: " + message);
	}

	private void logError(String message){
		System.err.println("Server: " + message);
	}
	
	public static void main(String[] args) 
	{
		Server server = new Server();
		
		try {
			server.initiate(52000);
		} 
		catch (IOException e) {
			System.err.println("Failed to setup a server socket. " + e.getMessage());
			System.exit(1);
		}
		
		try {
			server.waitForConnections();
		} 
		catch (IOException e) {
			System.err.println("Failed to setup connection. " + e.getMessage());
			System.exit(1);
		}
		
		server.close();
	}
}
