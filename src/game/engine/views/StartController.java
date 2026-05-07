
package game.engine.views;

import game.engine.views.App;
import game.engine.Role;
import javafx.fxml.FXML;

public class StartController {

    @FXML
    private void playScarer() {
        App.showGameScreen(Role.SCARER);
    }

    @FXML
    private void playLaugher() {
        App.showGameScreen(Role.LAUGHER);
    }
}

