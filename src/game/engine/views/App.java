package game.engine.views;




import game.engine.Role;
import game.engine.views.StartController;
import game.engine.views.GameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

 private static Stage primaryStage;

 @Override
 public void start(Stage stage) throws Exception {
     primaryStage = stage;
     showStartScreen();
     stage.setTitle("DoorDasH");
     stage.show();
 }

 public static void showStartScreen() {
     try {
         FXMLLoader loader = new FXMLLoader(App.class.getResource("StartView.fxml"));
         Parent root = loader.load();
         primaryStage.setScene(new Scene(root, 900, 650));
     } catch (Exception e) {
         e.printStackTrace();
     }
 }

 public static void showGameScreen(Role role) {
     try {
         FXMLLoader loader = new FXMLLoader(App.class.getResource("GameView.fxml"));
         Parent root = loader.load();

         GameController controller = loader.getController();
         controller.initGame(role);

         primaryStage.setScene(new Scene(root, 900, 650));
     } catch (Exception e) {
         e.printStackTrace();
     }
 }

 public static void main(String[] args) {
     launch(args);
 }
}

