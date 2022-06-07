import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server
{
    static final int PORT = 14312;
    static final int NUMBER_OF_PLAYERS = 4;
    private final ServerSocket serverSocket;
    
    ArrayList<Game> games;
    ArrayList<ClientHandler> clientHandlers;
    
    public Server(ServerSocket serverSocket)
    {
        this.serverSocket = serverSocket;
        this.games = new ArrayList<>();
        this.clientHandlers = new ArrayList<>();
    }
    
    public void startServer()
    {
        try
        {
            while (!serverSocket.isClosed())
            {
                Socket socket = serverSocket.accept();
                
                int id = ClientHandler.clientHandlers.size() % NUMBER_OF_PLAYERS;
                System.out.println(id);
                
                if (id == 0)
                {
                    games.add(new Game());
                }
                
                ClientHandler clientHandler = new ClientHandler(games.get(games.size() - 1), id, socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }
        catch (IOException e)
        {
            closeServerSocket();
        }
    }
    
    public void closeServerSocket()
    {
        try
        {
            if (serverSocket != null)
            {
                serverSocket.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
