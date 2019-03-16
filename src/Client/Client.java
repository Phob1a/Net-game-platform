package Client;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private static Client client = null;


    private AtomicBoolean connected = new AtomicBoolean(false);
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService es = Executors.newFixedThreadPool(5, r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    });
    private Queue<JSONObject> messageQueue = new LinkedBlockingDeque<>();


    public static Client getClient(){
        if(client == null){
            client = new Client("localhost",4399);
        }
        return client;
    }

    private Client(String ip, int port) {
        es.execute(()->{
            tryToConnect(ip,port);
            while(true){
                try {
                    String messageStr = in.readLine();
                    if(messageStr==null){
                        connected.set(false);
                        tryToConnect(ip,port);
                        continue;
                    }
                    if(messageQueue.size()>=1000){
                        for(int i = 0;i<500;i++){
                            messageQueue.remove();
                        }
                    }
                    messageQueue.add(new JSONObject(messageStr));
                } catch (IOException e) {
                    System.out.println("Something wrong when reading message from server.");
                }
            }
        });
    }

    private void tryToConnect(String ip,int port) {
        while(!connected.get()) {
            try {
                socket = new Socket(ip, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                connected.set(true);
            } catch (IOException e) {
                System.out.println("Server is offline... Try to connect again.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    public boolean isConnected(){
        return connected.get();
    }

    public synchronized JSONObject findNext(String type){
        messageQueue.removeIf(obj->!obj.has("type"));
        for(JSONObject obj : messageQueue){
            if(obj.getString("type").equals(type)){
                messageQueue.remove(obj);
                return obj;
            }
        }
        return null;
    }

    public synchronized JSONObject retreiveJson(String type){
        JSONObject result;
        while ((result = Client.getClient().findNext(type)) == null){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void login(String username,String password){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","login");
        jsonObject.put("username",username);
        jsonObject.put("password",password);
        sendMessage(jsonObject);
    }

    public void logout(String username){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","logout");
        jsonObject.put("username",username);
        sendMessage(jsonObject);
    }

    public void signup(String username, String password){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","signup");
        jsonObject.put("username",username);
        jsonObject.put("password",password);
        sendMessage(jsonObject);
    }


    public String receiveMessage(){
        try {
            String message = in.readLine();
            return message;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Client sendMessage(String message){
        out.println(message);
        return this;
    }

    public Client sendMessage(JSONObject jsonObject){
        sendMessage(jsonObject.toString());
        return this;
    }

}
