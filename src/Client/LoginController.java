package Client;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class LoginController {
    private Timer time;
    private Stage stage;
    public static Scene loginScene = null;
    private ExecutorService es = Executors.newFixedThreadPool(5, r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    });

    @FXML
    private Pane root;

    @FXML
    private TextField usernameInput;

    @FXML
    private PasswordField passwordInput;

    @FXML
    private Button signupBtn;

    @FXML
    private Button loginBtn;

    @FXML
    private Circle connectCircle;

    @FXML
    public void initialize() {
        time = new Timer(true);
        TimerTask ts = new TimerTask() {
            @Override
            public void run() {
                if (Client.getClient().isConnected()) {
                    loginBtn.setDisable(false);
                    signupBtn.setDisable(false);
                    connectCircle.setFill(Color.GREEN);
                    connectCircle.setStroke(Color.GREEN);
                } else {
                    loginBtn.setDisable(true);
                    signupBtn.setDisable(true);
                    connectCircle.setFill(Color.RED);
                    connectCircle.setStroke(Color.RED);
                }
            }
        };
        time.scheduleAtFixedRate(ts, 0, 10);
    }


    @FXML
    void signUpAction(ActionEvent e){
        try {
            stage = (Stage)(root.getScene().getWindow());
            loginScene=root.getScene();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("View/Signup.fxml")),800,450));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    @FXML
    void loginAction(ActionEvent event) {
        if (usernameInput.getText().isEmpty()) {
            LoginControllerHelper.empyInput(usernameInput, loginBtn);
        } else if (passwordInput.getText().isEmpty()) {
            LoginControllerHelper.empyInput(passwordInput, loginBtn);
        } else {
            String pwd = LoginControllerHelper.encrypt(passwordInput.getText());
            passwordInput.setText(pwd);
            Client.getClient().login(usernameInput.getText(), pwd);
            es.execute(() -> {
                    JSONObject jsonObject = Client.getClient().retreiveJson("login_response");
                    if(jsonObject.has("success")) {
                        if (jsonObject.get("success").equals("no")) {
                            Platform.runLater(() -> {
                                MsgBoxController.display("Login Failed", jsonObject.getString("reply"));
                            });
                        } else {
                            Platform.runLater(() -> {
                                MsgBoxController.display("Login Succeed", jsonObject.getString("reply"));
                                ChatController.username=usernameInput.getText();
                                ChatController.password=passwordInput.getText();
                                try {
                                    time.cancel();
                                    time.purge();
                                    stage = (Stage) (root.getScene().getWindow());
                                    loginScene = root.getScene();
                                    stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("View/chatroom.fxml"))));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
            });
        }
    }

    @FXML
    void keyPress(KeyEvent e){
        if(!usernameInput.getText().isEmpty() && !passwordInput.getText().isEmpty()){
            loginBtn.setDisable(false);
        }
    }
}
