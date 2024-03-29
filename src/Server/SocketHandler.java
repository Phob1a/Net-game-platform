package Server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bxs863 on 26/02/19.
 */
public class SocketHandler extends Thread {

    private Socket socket;
    private Timer timer;
    public SocketHandler(Socket socket){

        this.socket = socket;
    }

    @Override
    public void run(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            while (true) {
                String line = in.readLine();
                if(line==null || line.equals("")){ //Handle the situation when client quits
                    closeSocket();
                    break;
                }
                System.out.println(line);
                String responce = MessageHandler.getMessageHandler(line,socket).process();
                out.println(responce);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void closeSocket() throws IOException {

        socket.close();
        Server.getInstance().getClients().entrySet().removeIf(p -> p.getValue().isClosed());
        sendNewContactList();
    }

    private void sendNewContactList() throws IOException {
        JSONObject response = new JSONObject();
        response.put("type","contact_list");
        response.put("contact_names",Server.getInstance().getClients().keySet());
        for(Socket socket:Server.getInstance().getClients().values()){
            new PrintWriter(socket.getOutputStream(),true).println(response.toString());
        }
    }

}
