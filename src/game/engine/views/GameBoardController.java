package game.engine.views;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class GameBoardController {

    @FXML
    private GridPane boardGrid;

    private static final int SIZE = 10; // 10x10

    @FXML
    public void initialize() {
        createBoard();
    }

    private void createBoard() {
        int cellIndex = 0;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                StackPane cell = createCell(cellIndex);
                boardGrid.add(cell, col, row);
                cellIndex++;
            }
        }
    }

    private StackPane createCell(int index) {
        StackPane cell = new StackPane();
        cell.setPrefSize(60, 60);
        cell.setAlignment(Pos.CENTER);

        // Temporary coloring by type (visual only)
        String color = getColorByIndex(index);
        cell.setStyle("-fx-border-color: black; -fx-background-color: " + color + ";");

        Label label = new Label(String.valueOf(index));
        label.setStyle("-fx-font-weight: bold;");

        cell.getChildren().add(label);
        return cell;
    }

    // TEMP colors just for layout visualization
    private String getColorByIndex(int index) {
        if (index % 10 == 0) return "#ff9999"; // Door example
        if (index % 15 == 0) return "#99ccff"; // Card example
        if (index % 7 == 0) return "#ccffcc";  // Monster cell example
        return "#f2f2f2"; // Normal
    }
}
