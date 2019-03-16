package Server;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by bxs863 on 26/02/19.
 */
public class LogOutMessageHandler extends MessageHandler {
    LogOutMessageHandler(String message) {
        super(message);
    }

    @Override
    String process() {

        JSONObject response = new JSONObject();
        try {
            response.put("type", "logout_response");
            logout(jsonObject, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    private void logout(JSONObject jsonObject, JSONObject response) {
        try {
            if (jsonObject.has("username")) {
                String username = jsonObject.getString("username");
                if(Server.getInstance().getClients().containsKey(username)){
                    Server.getInstance().getClients().remove(username);
                    //MAYBE ADD SOME CODE TO SAVE DATA INTO THE DATABASE IN THE FUTURE.

                    response.put("success","yes");
                    response.put("reply","You have logged out successfully");
                }
                else{
                    response.put("success","no");
                    response.put("reply","You haven't logged in");
                }
            } else {
                response.put("success","no");
                response.put("reply","Username is missing");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
