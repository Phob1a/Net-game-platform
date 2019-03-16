package Client;

import Server.MessageHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


/**
 * Created by bxs863 on 06/03/19.
 */
public class ChatController {



    //INNER CLASS
    class ContactListCell extends ListCell<Profile> {
        @Override
        public void updateItem(Profile item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setGraphic(item.getItem());
            } else {
                setGraphic(null);
            }
        }
    }

    class MessageCell extends ListCell<String>{

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setText(item);
            } else {
                setGraphic(null);
            }
        }
    }

    class Profile{
        private final String username;
        private ImageView icon;
        private HBox item = new HBox(5);
        private ListView<String> listView = new ListView<>();

        public Profile(String username){
            this.username = username;
            Random random = new Random();
            this.icon = new ImageView();
            icon.setImage(new Image(String.format("Client/View/icon/%d.png",random.nextInt(9)+1),30,30,false,false));
            item.getChildren().add(icon);
            VBox vBox = new VBox();
            vBox.setSpacing(0);
            Label label = new Label(username);
            label.setStyle("-fx-font-weight: bold");
            vBox.getChildren().add(label);
            item.getChildren().add(vBox);
        }

        public HBox getItem() {
            return item;
        }

        public String getUsername() {
            return username;
        }

        public ListView<String> getListView() {
            return listView;
        }
    }
    //---------------------------------------------------


    public static String username;
    public static String password;

    private ExecutorService es = Executors.newFixedThreadPool(10, r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    });


    @FXML
    private ListView<Profile> contactList;
    @FXML
    private ListView<String> gameList;
    @FXML
    private TextArea inputArea;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button sendBtn;
    @FXML
    private Button startBtn;
    @FXML
    public ListView<String> chatArea;


    @FXML
    public void initialize() {
        contactList.setCellFactory(list -> new ContactListCell());
        contactList.getSelectionModel().selectedItemProperty().addListener(
                (ov, old_val, new_val) -> {
                    if (old_val != null) {
                        old_val.getListView().setItems(chatArea.getItems());
                        chatArea.setItems(new_val.getListView().getItems());
                    }
                });
        es.execute(() -> {
            while (true) {
                checkConnection();
                JSONObject response;
                if ( (response= Client.getClient().findNext("forward_new_message"))!=null) {
                    processNewMessage(response);
                }
                if ((response= Client.getClient().findNext("forward_response"))!=null) {
                    processResponse(response);
                }
                if ((response=Client.getClient().findNext("contact_list"))!=null){
                    updateContactList(response);
                }
                if((response=Client.getClient().findNext("start_game_response"))!=null){
                    processStartGameResponse(response);
                }
                if((response=Client.getClient().findNext("start_game_answer"))!=null){
                    processStartGameAnswer(response);
                }
                if((response=Client.getClient().findNext("invitation"))!=null){
                    processInvitation(response);
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        usernameLabel.setText(username);
    }

    private void processInvitation(JSONObject response) {
        Platform.runLater(()->{MsgBoxController.display("Invitation",response.getString("message"));});

        //Still need to change the thing
    }

    private void processStartGameAnswer(JSONObject response) {

    }

    private void processStartGameResponse(JSONObject response) {
        //Do nothing
    }



    public void startNewGame(MouseEvent mouseEvent) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","data");
        jsonObject.put("sub_type","start_game");
        jsonObject.put("first_user",username);
        jsonObject.put("second_user",contactList.getSelectionModel().getSelectedItem().getUsername());
        jsonObject.put("game",gameList.getSelectionModel().getSelectedItem()==null?"snake":gameList.getSelectionModel().getSelectedItem());
        es.execute(()->Client.getClient().sendMessage(jsonObject));
        MsgBoxController.display("Message has sent","You message has been sent");

    }

    private void checkConnection(){
        if (!Client.getClient().isConnected()){
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask(){
                @Override
                public void run() {
                    Platform.exit();
                }
            },60000);
            while(!Client.getClient().isConnected()) try {
                Platform.runLater(()->{inputArea.setDisable(true);sendBtn.setDisable(true);});
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            JSONObject cookie = new JSONObject();
            cookie.put("type","login");
            cookie.put("username",username);
            cookie.put("password",password);
            Client.getClient().sendMessage(cookie);
            Platform.runLater(()->{inputArea.setDisable(false);sendBtn.setDisable(false);});
            timer.cancel();
            timer.purge();
        }
    }
    private void updateContactList(JSONObject jsonObject) {
        boolean isEmpty = false;
        if (contactList.getItems().isEmpty()) {
            isEmpty = true;
        }
        List<String> nameList = jsonObject.getJSONArray("contact_names").toList().stream().map(Object::toString).collect(Collectors.toList());
        Profile[] offlineList = filterOffline(nameList);
        nameList = filterExist(nameList);
        String[] name = new String[nameList.size()];
        name = nameList.toArray(name);
        String[] finalName = name;
        boolean finalIsEmpty = isEmpty;
        Platform.runLater(() -> {
            contactList.getItems().addAll(stringsToProfiles(finalName));
            contactList.getItems().removeAll(offlineList);
            if (finalIsEmpty) contactList.getSelectionModel().selectFirst();
            contactList.refresh();
        });
    }

    private void processNewMessage(JSONObject jsonObject) {
        String fromUser = jsonObject.getString("from_user");
        String message = jsonObject.getString("message");
        for (Profile p : contactList.getItems()) {
            if (p.getUsername().equals(fromUser)) {
                Platform.runLater(() -> {
                    p.getListView().getItems().add(message);
                    if (p.getUsername().equals(contactList.getSelectionModel().getSelectedItem().getUsername())) {
                        contactList.getItems().remove(p);
                        contactList.getItems().add(0, p);
                        contactList.getSelectionModel().selectFirst();
                    } else {
                        contactList.getItems().remove(p);
                        contactList.getItems().add(0, p);
                    }
                });
                break;
            }
        }
    }

    private void processResponse(JSONObject jsonObject) {
        //RESERVE FOR FUTURE USE
    }



    public void sendByKeyboard(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER){
            sendMessage();
        }
    }

    public void sendMessage(MouseEvent mouseEvent) {
        sendMessage();
    }



    private void sendMessage(){
        String msg = inputArea.getText();
        Profile p = contactList.getSelectionModel().getSelectedItem();
        chatArea.getItems().add(msg);
        inputArea.clear();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","forward");
        jsonObject.put("sub_type","forward");
        jsonObject.put("to_user",contactList.getSelectionModel().getSelectedItem().getUsername());
        jsonObject.put("message",msg);
        jsonObject.put("from_user",username);
        es.execute(()->Client.getClient().sendMessage(jsonObject));

        contactList.getItems().remove(p);
        contactList.getItems().add(0,p);
        contactList.getSelectionModel().selectFirst();

    }

    private Profile[] stringsToProfiles(String[] args){
        Profile[] profiles = new Profile[args.length];
        for(int i = 0;i<args.length;i++){
            profiles[i]=new Profile(args[i]);
        }
        return profiles;
    }

    private List<String> filterExist(List<String> names){
        List<String> existName = new ArrayList<>();
        for(Profile i : contactList.getItems()){
            existName.add(i.getUsername());
        }
        List<String> result = new ArrayList<>();
        for(String name:names){
            if(existName.contains(name)) continue;
            result.add(name);
        }
        return result;
    }

    private Profile[] filterOffline(List<String> nameList) {
        List<Profile> offline = new ArrayList<Profile>();
        for(Profile p : contactList.getItems()){
            if(!nameList.contains(p.getUsername())){
                offline.add(p);
            }
        }
        return offline.toArray(new Profile[0]);
    }

}

