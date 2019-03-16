package Client;

import Server.MessageHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;


/**
 * Created by bxs863 on 05/03/19.
 */
public class MsgBoxController {

    private static Stage stage;

    @FXML
    private Label title;
    @FXML
    private Text context;

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setContext(String context) {
        this.context.setText(context);
    }

    @FXML
    public void initialize() {

    }


    public static void display(String title, String content) {
        try {
            stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            FXMLLoader loader = new FXMLLoader(MsgBoxController.class.getResource("View/MsgBox.fxml"));
            Parent calcRoot = loader.load();
            MsgBoxController controller = loader.getController();
            controller.setTitle(title);
            controller.setContext(content);
            Scene scene = new Scene(calcRoot);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {



        });
    }

    public void quit(MouseEvent mouseEvent) {
        stage.close();
    }
}
