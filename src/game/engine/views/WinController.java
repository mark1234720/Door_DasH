package game.engine.views;

import game.engine.monsters.Monster;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class WinController {

    @FXML private Label winBanner;
    @FXML private Label winnerName;
    @FXML private Label winnerRole;
    @FXML private Label playerFinalName;
    @FXML private Label playerFinalEnergy;
    @FXML private Label opponentFinalName;
    @FXML private Label opponentFinalEnergy;

    public void init(Monster winner, Monster player, Monster opponent) {
        winnerName.setText(winner.getName());
        winnerRole.setText(winner.getOriginalRole().toString());
        playerFinalName.setText(player.getName());
        playerFinalEnergy.setText(player.getEnergy() + " energy");
        opponentFinalName.setText(opponent.getName());
        opponentFinalEnergy.setText(opponent.getEnergy() + " energy");

        if (winner == player) {
            winBanner.setText("🎉 YOU WIN! 🎉");
            winBanner.setStyle("-fx-text-fill: #00d4aa; -fx-font-size: 48px; -fx-font-weight: bold;");
        } else {
            winBanner.setText("💀 GAME OVER 💀");
            winBanner.setStyle("-fx-text-fill: #e94560; -fx-font-size: 48px; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void onPlayAgain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/game/engine/views/StartView.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) winnerName.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
