package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by bxs863 on 26/02/19.
 */
public class Server {
    private Hashtable<String,Socket> clients = new Hashtable<>();
    private static Server server = null;

    private Server(){

    }

    public static Server getInstance(){
        if(server == null){
            server = new Server();
        }
        return server;
    }

    /**
     * Get the client
     * @return
     */
    public Hashtable<String, Socket> getClients() {
        return clients;
    }

    public void Start(int port){
        try {
            ServerSocket ss = new ServerSocket(port);
            while(true){
                new SocketHandler(ss.accept()).start();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server.getInstance().Start(4399);
    }
}
