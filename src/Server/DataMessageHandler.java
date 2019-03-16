package Server;

import Database.Database;
import org.json.JSONObject;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by bxs863 on 26/02/19.
 */
public class DataMessageHandler extends MessageHandler {
    public DataMessageHandler(String message) {
        super(message);
    }

    @Override
    String process() {
        JSONObject response = new JSONObject();
        response.put("type","data_response");
        if(jsonObject.get("sub_type").equals("check_user")){
            checkUserExist(response);
        }
        else if(jsonObject.getString("sub_type").equals("start_game")){
            processNewGame(jsonObject,response);
        }
        else if(jsonObject.getString("sub_type").equals("start_game_answer")){
            processStartGameResponse(jsonObject,response);
        }
        return response.toString();
    }

    private void processStartGameResponse(JSONObject jsonObject, JSONObject response) {

    }

    private void checkUserExist(JSONObject response) {
        response.put("sub_type", "check_user_response");
        if (jsonObject.has("username")) {
            boolean result = Database.getInstance().checkExist("user_info", "username", jsonObject.getString("username"));
            response.put("success","yes");
            response.put("reply",result);
        }
        else{
            response.put("success","no");
            response.put("reply","You haven't provide the username");
        }
    }

    private void processNewGame(JSONObject jsonObject,JSONObject response){
        response.put("sub_type","start_game_response");
        if(jsonObject.has("first_user")&&jsonObject.has("second_user")&&jsonObject.has("game")){
            try {
                String s = String.format("%s wants to play %s with you.",jsonObject.getString("first_user"),jsonObject.getString("game"));
                JSONObject invite = new JSONObject();
                invite.put("type","invitation");
                invite.put("user",jsonObject.getString("first_user"));
                invite.put("game",jsonObject.getString("game"));
                invite.put("message",s);
                new PrintWriter(Server.getInstance().getClients().get(jsonObject.getString("second_user")).getOutputStream(),true).println(invite.toString());
                response.put("reply","Your invitation has been sent.");
            } catch (IOException e) {
                response.put("reply","Your invitation has not been sent.");
                e.printStackTrace();
            }
        }
    }

}
