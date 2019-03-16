package Server;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;

/**
 * Created by bxs863 on 26/02/19.
 */
public abstract class MessageHandler {
    MessageType type = MessageType.None;
    JSONObject jsonObject = null;


    MessageHandler(String message){
        try {
            jsonObject = new JSONObject(message);
            type = MessageHandlerHelper.getType(jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static MessageHandler getMessageHandler(String message, Socket socket){
        MessageHandler result = null;
        try {
            JSONObject js = new JSONObject(message);
            MessageType type = MessageHandlerHelper.getType(js);
            switch (type) {
                case LogIn:
                    result = new LoginMessageHandler(message,socket);
                    break;
                case LogOut:
                    result = new LogOutMessageHandler(message);
                    break;
                case SignUp:
                    result = new SignupMessageHandler(message);
                    break;
                case Forward:
                    result = new ForwardMessageHandler(message);
                    break;
                case Data:
                    result = new DataMessageHandler(message);
                    break;
                case None:
                    result = new BadMessageHandler(message);
                    break;
                default:
                    result = new BadMessageHandler(message);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    abstract String process();




}
