import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable
{
    static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    
    private Socket socket;
    private ObjectOutputStream objectWriter;
    private ObjectInputStream objectReader;
    private String clientUsername;
    private Game game;
    private Player player;
    
    public ClientHandler(Game game, int id, Socket socket)
    {
        this.game = game;
        this.player = new Player(id, game.characterHeight);
        
        try
        {
            this.socket = socket;
            this.objectWriter = new ObjectOutputStream(socket.getOutputStream());
            this.objectReader = new ObjectInputStream(socket.getInputStream());
            this.clientUsername = objectReader.readUTF();    // When a client connects their username is sent
            clientHandlers.add(this);
            System.out.println("SERVER: " + clientUsername + " has joined the game.");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            closeEverything();
        }
    }
    
    public void sendMessage(String messageToSend)
    {
        try
        {
            objectWriter.writeUTF(messageToSend);
            objectWriter.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            closeEverything();
        }
    }
    
    public void sendObject(Object object)
    {
        try
        {
            objectWriter.reset();
            objectWriter.writeObject(object);
            objectWriter.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            closeEverything();
        }
    }
    
    public void listenForMessages()
    {
        new Thread(() ->
        {
            while (!socket.isClosed())
            {
                try
                {
                    String msgFromClient = objectReader.readUTF();
                    handleClientMessage(msgFromClient);
                }
                catch (IOException | ClassNotFoundException e)
                {
                    e.printStackTrace();
                    closeEverything();
                }
            }
        }).start();
    }
    
    public void handleClientMessage(String message) throws IOException, ClassNotFoundException
    {
        if (message.equals("Player Update"))
        {
            Player player = (Player) objectReader.readObject();
            
            for (int i = 0; i < game.players.size(); i++)
            {
                if (game.players.get(i).id == player.id)
                {
                    game.players.set(i, player);
                }
            }
        }
        else if (message.equals("Shoot"))
        {
            Player shot = MathUtils.shoot(player, game.objects, game.players);
            
            if (shot != null)
            {
                game.players.get(game.players.indexOf(shot)).health -= game.damage;
            }
        }
    }
    
    // If the client disconnects for any reason remove them from the list so a message isn't sent down a broken connection.
    public void remove()
    {
        clientHandlers.remove(this);
        
        System.out.println("SERVER: " + clientUsername + " has disconnected.");
    }
    
    public void closeEverything()
    {
        remove();
        
        try
        {
            if (objectReader != null)
            {
                objectReader.close();
            }
            if (objectWriter != null)
            {
                objectWriter.close();
            }
            if (socket != null)
            {
                socket.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run()
    {
        listenForMessages();
    
        sendMessage("ID " + player.id);
        game.players.add(player);
        
        while (!socket.isClosed())
        {
            sendMessage("Game");
            sendObject(game);
        }
    }
}
