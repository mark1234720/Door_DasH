package game.engine.views;

import game.engine.views.WinController;
import game.engine.*;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
public class GameController {

    // ── FXML ─────────────────────────────────────────────────────────────────
    @FXML private GridPane boardGrid;
    @FXML private Label    turnLabel;
    @FXML private Label    currentPlayerLabel;

    // Player panel
    @FXML private Label playerName;
    @FXML private Label playerType;
    @FXML private Label playerOrigRole;
    @FXML private Label playerCurrRole;
    @FXML private Label playerEnergy;
    @FXML private Label playerPos;
    @FXML private VBox  playerStatusBox;

    // Opponent panel
    @FXML private Label opponentName;
    @FXML private Label opponentType;
    @FXML private Label opponentOrigRole;
    @FXML private Label opponentCurrRole;
    @FXML private Label opponentEnergy;
    @FXML private Label opponentPos;
    @FXML private VBox  opponentStatusBox;

    // Bottom
    @FXML private Label  diceLabel;
    @FXML private Label  eventLabel;
    @FXML private Button powerupBtn;
    @FXML private Button rollBtn;

    // Log
    @FXML private VBox       logBox;
    @FXML private ScrollPane logScroll;

    // ── State ─────────────────────────────────────────────────────────────────
    private Game game;
    private int  turnCount = 1;
    private final Map<Integer, StackPane> cellPanes = new HashMap<>();

    // ── Cell size ─────────────────────────────────────────────────────────────
    private static final double CELL_W = 58;
    private static final double CELL_H = 58;

    // ═════════════════════════════════════════════════════════════════════════
    // INIT — called by StartController after loading this FXML
    // ═════════════════════════════════════════════════════════════════════════
    public void initGame(Role playerRole) {
        try {
            game = new Game(playerRole);     // ← your engine constructor
            buildBoard();
            refreshAll();
            log("Game started! You are " + game.getPlayer().getName()
                    + " [" + playerRole + "]", "INFO");
        } catch (Exception e) {
            showError("Failed to initialise game:\n" + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // BOARD CONSTRUCTION
    // ═════════════════════════════════════════════════════════════════════════
    private void buildBoard() {
        boardGrid.getChildren().clear();
        Cell[][] cells = game.getBoard().getBoardCells();

        for (int row = 0; row < Constants.BOARD_ROWS; row++) {
            for (int col = 0; col < Constants.BOARD_COLS; col++) {
                int index = boardIndexOf(row, col);
                StackPane pane = makeCellPane(cells[row][col], index);
                cellPanes.put(index, pane);
                // Flip row: cell 0 bottom-left → GridPane row = 9-row
                boardGrid.add(pane, col, Constants.BOARD_ROWS - 1 - row);
            }
        }
    }

    /**
     * Board uses boustrophedon (snake) layout.
     * Even rows go left→right, odd rows go right→left.
     */
    private int boardIndexOf(int row, int col) {
        int actualCol = (row % 2 == 0) ? col : (Constants.BOARD_COLS - 1 - col);
        return row * Constants.BOARD_COLS + actualCol;
    }

    private StackPane makeCellPane(Cell cell, int index) {
        StackPane pane = new StackPane();
        pane.setPrefSize(CELL_W, CELL_H);
        pane.setMinSize(CELL_W, CELL_H);
        pane.setMaxSize(CELL_W, CELL_H);
        pane.setStyle(cellStyle(cell));

        // Top-left index number
        Label idx = new Label(String.valueOf(index));
        idx.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 8px;");
        StackPane.setAlignment(idx, Pos.TOP_LEFT);

        // Center icon + info
        Label content = new Label(cellIcon(cell));
        content.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        content.setTextAlignment(TextAlignment.CENTER);
        content.setWrapText(true);

        pane.getChildren().addAll(idx, content);

        // Tooltip
        Tooltip tip = new Tooltip(cellTooltip(cell, index));
        tip.setStyle("-fx-font-size: 11px;");
        Tooltip.install(pane, tip);

        return pane;
    }

    // ── Cell visual helpers ───────────────────────────────────────────────────

    private String cellStyle(Cell cell) {
        String base = "-fx-background-radius: 6; -fx-border-radius: 6; -fx-border-width: 1; ";
        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            if (d.isActivated())
                return base + "-fx-background-color: #2a2a2a; -fx-border-color: #555;";
            return d.getRole() == Role.SCARER
                    ? base + "-fx-background-color: #1a0a2e; -fx-border-color: #9b59b6;"
                    : base + "-fx-background-color: #0a1a2e; -fx-border-color: #3498db;";
        }
        if (cell instanceof CardCell)
            return base + "-fx-background-color: #1a2a0a; -fx-border-color: #2ecc71;";
        if (cell instanceof MonsterCell)
            return base + "-fx-background-color: #2a0a0a; -fx-border-color: #e74c3c;";
        if (cell instanceof ConveyorBelt)
            return base + "-fx-background-color: #1a1a0a; -fx-border-color: #f1c40f;";
        if (cell instanceof ContaminationSock)
            return base + "-fx-background-color: #0a2a1a; -fx-border-color: #1abc9c;";
        // Normal
        return base + "-fx-background-color: #1a1a2e; -fx-border-color: #2a2a4a;";
    }

    private String cellIcon(Cell cell) {
        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            String role = d.getRole() == Role.SCARER ? "😱" : "😂";
            return role + "\n" + (d.isActivated() ? "✓" : d.getEnergy());
        }
        if (cell instanceof CardCell)          return "🃏";
        if (cell instanceof MonsterCell) {
            MonsterCell mc = (MonsterCell) cell;
            // Show first word of stationed monster name
            String name = mc.getCellMonster().getName().split(" ")[0];
            return "👾\n" + name;
        }
        if (cell instanceof ConveyorBelt) {
            int fx = ((ConveyorBelt) cell).getEffect();
            return fx > 0 ? "➡\n+" + fx : "⬅\n" + fx;
        }
        if (cell instanceof ContaminationSock) {
            int fx = ((ContaminationSock) cell).getEffect();
            return "🧦\n" + fx;
        }
        return "";
    }

    private String cellTooltip(Cell cell, int index) {
        StringBuilder sb = new StringBuilder("Cell #").append(index)
                .append("  [").append(cell.getName()).append("]");
        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            sb.append("\nRole: ").append(d.getRole())
              .append("\nEnergy: ").append(d.getEnergy())
              .append("\nActivated: ").append(d.isActivated());
        } else if (cell instanceof ConveyorBelt) {
            sb.append("\nTransport: ").append(((ConveyorBelt) cell).getEffect());
        } else if (cell instanceof ContaminationSock) {
            sb.append("\nTransport: ").append(((ContaminationSock) cell).getEffect())
              .append("\nSlip penalty: -").append(Constants.SLIP_PENALTY);
        } else if (cell instanceof MonsterCell) {
            sb.append("\nStationed: ").append(((MonsterCell) cell).getCellMonster().getName());
        }
        return sb.toString();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ACTIONS (wired to FXML onAction)
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void onUsePowerup() {
        Monster m = game.getCurrent();
        int energyBefore = m.getEnergy();
        try {
            game.usePowerup();   // ← your engine: deducts 500, calls executePowerupEffect()
            int spent = energyBefore - m.getEnergy();
            log(m.getName() + " used powerup! (-" + spent + " energy)", "ACTION");
            eventLabel.setText("⚡ " + m.getName() + " activated powerup!");
            refreshAll();
        } catch (OutOfEnergyException e) {
            showError("Not enough energy to use powerup!\n"
                    + "Required: " + Constants.POWERUP_COST
                    + "  |  Current: " + m.getEnergy());
        }
    }

    
    @FXML
    private void onRoll() {
        rollBtn.setDisable(true);
        powerupBtn.setDisable(true);
     
        Monster movingMonster  = game.getCurrent();
        Monster movingOpponent = (movingMonster == game.getPlayer()) ? game.getOpponent() : game.getPlayer();
     
        int posBefore    = movingMonster.getPosition();
        int energyBefore = movingMonster.getEnergy();
        int oppEnBefore  = movingOpponent.getEnergy();
        boolean wasFrozen = movingMonster.isFrozen();
     
        try {
            game.playTurn();
     
            if (wasFrozen) {
                diceLabel.setText("—");
                diceLabel.setStyle("-fx-text-fill: #00bcd4; -fx-font-size: 28px; -fx-font-weight: bold;");
                log(movingMonster.getName() + " was FROZEN — turn skipped! ❄", "FREEZE");
                eventLabel.setText("❄ " + movingMonster.getName() + " skipped (Frozen)");
     
                turnCount++;
                refreshAll();
     
                Monster winner = game.getWinner();
                if (winner != null) { showWinScreen(winner); return; }
     
                boolean isPlayerTurn = (game.getCurrent() == game.getPlayer());
                updateTurnUI(isPlayerTurn);
                if (!isPlayerTurn) playOpponentTurn();
     
            } else {
                // Infer roll from position difference
                int newPos  = movingMonster.getPosition();
                int posDiff = newPos - posBefore;
                if (posDiff < 0) posDiff += Constants.BOARD_SIZE;
     
                // Run dice animation, then do all logic AFTER it finishes (at 900ms)
                final int inferredRoll = posDiff;
                animateDice(inferredRoll);
     
                // Delay post-roll logic until animation completes
                new Timeline(new KeyFrame(Duration.millis(950), e -> {
     
                    Cell landed = getCellAt(newPos);
                    log(movingMonster.getName() + " rolled " + inferredRoll
                            + " → cell " + newPos + " (" + landed.getName() + ")", "MOVE");
     
                    if (landed instanceof CardCell) {
                        log("🃏 Card drawn at cell " + newPos + "!", "CARD");
                        eventLabel.setText("🃏 Card drawn! Check log.");
                    }
     
                    int energyDiff = movingMonster.getEnergy() - energyBefore;
                    if (energyDiff != 0)
                        log(movingMonster.getName() + " energy " + fmtDiff(energyDiff)
                                + " → " + movingMonster.getEnergy(), "ENERGY");
     
                    int oppEnergyDiff = movingOpponent.getEnergy() - oppEnBefore;
                    if (oppEnergyDiff != 0)
                        log(movingOpponent.getName() + " energy " + fmtDiff(oppEnergyDiff)
                                + " → " + movingOpponent.getEnergy(), "ENERGY");
     
                    if (movingMonster.isShielded() && energyDiff == 0 && landed instanceof DoorCell)
                        log("🛡 " + movingMonster.getName() + "'s shield blocked energy loss!", "SHIELD");
     
                    if (!(landed instanceof CardCell))
                        eventLabel.setText(movingMonster.getName() + " landed on " + landed.getName());
     
                    turnCount++;
                    refreshAll();
     
                    Monster winner = game.getWinner();
                    if (winner != null) { showWinScreen(winner); return; }
     
                    boolean isPlayerTurn = (game.getCurrent() == game.getPlayer());
                    updateTurnUI(isPlayerTurn);
                    if (!isPlayerTurn) playOpponentTurn();
     
                })).play();
            }
     
        } catch (InvalidMoveException e) {
            showError("Invalid move:\n" + e.getMessage());
            rollBtn.setDisable(false);
            powerupBtn.setDisable(false);
        }
    }

    // ── Opponent auto-play ────────────────────────────────────────────────────
    private void playOpponentTurn() {
        new Timeline(new KeyFrame(Duration.millis(1200), e -> {
            Monster opp        = game.getCurrent();
            Monster oppTarget  = (opp == game.getPlayer()) ? game.getOpponent() : game.getPlayer();
            int posBefore      = opp.getPosition();
            int enBefore       = opp.getEnergy();
            int playerEnBefore = oppTarget.getEnergy();
            boolean wasFrozen  = opp.isFrozen();

            try {
                game.playTurn();

                if (wasFrozen) {
                    log(opp.getName() + " was FROZEN — turn skipped! ❄", "FREEZE");
                    diceLabel.setText("—");
                    eventLabel.setText("❄ " + opp.getName() + " skipped (Frozen)");
                } else {
                    int posDiff = opp.getPosition() - posBefore;
                    if (posDiff < 0) posDiff += Constants.BOARD_SIZE;
                    diceLabel.setText(String.valueOf(posDiff));

                    Cell landed = getCellAt(opp.getPosition());
                    log("🤖 " + opp.getName() + " rolled " + posDiff
                            + " → cell " + opp.getPosition() + " (" + landed.getName() + ")", "MOVE");

                    if (landed instanceof CardCell)
                        log("🃏 Opponent drew a card at cell " + opp.getPosition() + "!", "CARD");

                    int enDiff = opp.getEnergy() - enBefore;
                    if (enDiff != 0)
                        log(opp.getName() + " energy " + fmtDiff(enDiff) + " → " + opp.getEnergy(), "ENERGY");

                    int playerEnDiff = oppTarget.getEnergy() - playerEnBefore;
                    if (playerEnDiff != 0)
                        log(oppTarget.getName() + " energy " + fmtDiff(playerEnDiff)
                                + " → " + oppTarget.getEnergy(), "ENERGY");

                    eventLabel.setText("🤖 " + opp.getName() + " moved to cell " + opp.getPosition());
                }

                turnCount++;
                refreshAll();

                Monster winner = game.getWinner();
                if (winner != null) {
                    showWinScreen(winner);
                    return;
                }

                updateTurnUI(true); // back to player
                rollBtn.setDisable(false);
                powerupBtn.setDisable(false);

            } catch (InvalidMoveException ex) {
                log("Opponent hit invalid move: " + ex.getMessage(), "ERROR");
                rollBtn.setDisable(false);
                powerupBtn.setDisable(false);
            }
        })).play();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UI REFRESH
    // ═════════════════════════════════════════════════════════════════════════
    private void refreshAll() {
        turnLabel.setText(String.valueOf(turnCount));
        refreshMonsterPanel(
                game.getPlayer(),
                playerName, playerType, playerOrigRole,
                playerCurrRole, playerEnergy, playerPos, playerStatusBox);
        refreshMonsterPanel(
                game.getOpponent(),
                opponentName, opponentType, opponentOrigRole,
                opponentCurrRole, opponentEnergy, opponentPos, opponentStatusBox);
        refreshBoard();
    }

    private void refreshMonsterPanel(Monster m,
            Label name, Label type, Label origRole, Label currRole,
            Label energy, Label pos, VBox statusBox) {

        name.setText(m.getName());
        type.setText(monsterTypeName(m));
        origRole.setText(m.getOriginalRole().toString());

        // Current role — orange warning if confused
        if (m.isConfused()) {
            currRole.setText(m.getRole() + " 😵");
            currRole.setStyle("-fx-text-fill: #f5a623; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            currRole.setText(m.getRole().toString());
            boolean isPlayer = (m == game.getPlayer());
            currRole.setStyle("-fx-text-fill: " + (isPlayer ? "#00d4aa" : "#e94560")
                    + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        }

        // Energy colour: green ≥1000, orange 300-999, red <300
        energy.setText(String.valueOf(m.getEnergy()));
        if (m.getEnergy() >= Constants.WINNING_ENERGY)
            energy.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 26px; -fx-font-weight: bold;");
        else if (m.getEnergy() >= 300)
            energy.setStyle("-fx-text-fill: #f5a623; -fx-font-size: 26px; -fx-font-weight: bold;");
        else
            energy.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 26px; -fx-font-weight: bold;");

        pos.setText(String.valueOf(m.getPosition()));

        // Status chips
        statusBox.getChildren().clear();
        if (m.isShielded())
            statusBox.getChildren().add(chip("🛡 Shield", "#3498db"));
        if (m.isConfused())
            statusBox.getChildren().add(chip("😵 Confused ×" + m.getConfusionTurns(), "#f5a623"));
        if (m.isFrozen())
            statusBox.getChildren().add(chip("❄ Frozen", "#00bcd4"));
        if (m instanceof Dasher && ((Dasher) m).getMomentumTurns() > 0)
            statusBox.getChildren().add(chip("💨 Momentum ×" + ((Dasher) m).getMomentumTurns(), "#9b59b6"));
        if (m instanceof MultiTasker && ((MultiTasker) m).getNormalSpeedTurns() > 0)
            statusBox.getChildren().add(chip("🎯 Focus ×" + ((MultiTasker) m).getNormalSpeedTurns(), "#1abc9c"));
    }

    private Label chip(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px; -fx-font-weight: bold;"
                + "-fx-background-color: " + color + "22;"
                + "-fx-background-radius: 8; -fx-padding: 2 8 2 8;"
                + "-fx-border-color: " + color + "; -fx-border-radius: 8; -fx-border-width: 1;");
        return l;
    }

    private void refreshBoard() {
        Cell[][] cells = game.getBoard().getBoardCells();

        for (int row = 0; row < Constants.BOARD_ROWS; row++) {
            for (int col = 0; col < Constants.BOARD_COLS; col++) {
                int index = boardIndexOf(row, col);
                Cell cell = cells[row][col];
                StackPane pane = cellPanes.get(index);
                if (pane == null) continue;

                // Refresh base style
                pane.setStyle(cellStyle(cell));

                // Refresh content icon
                if (pane.getChildren().size() >= 2) {
                    ((Label) pane.getChildren().get(1)).setText(cellIcon(cell));
                }

                // Player token overlay
                boolean hasPlayer   = (game.getPlayer().getPosition() == index);
                boolean hasOpponent = (game.getOpponent().getPosition() == index);

                if (hasPlayer && hasOpponent) {
                    pane.setStyle(pane.getStyle()
                            + "-fx-border-color: #ffffff; -fx-border-width: 3;");
                } else if (hasPlayer) {
                    pane.setStyle(pane.getStyle()
                            + "-fx-border-color: #00d4aa; -fx-border-width: 3;");
                    pulse(pane);
                } else if (hasOpponent) {
                    pane.setStyle(pane.getStyle()
                            + "-fx-border-color: #e94560; -fx-border-width: 3;");
                    pulse(pane);
                }
            }
        }
    }

    private void pulse(StackPane pane) {
        ScaleTransition st = new ScaleTransition(Duration.millis(250), pane);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.18);  st.setToY(1.18);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TURN UI
    // ═════════════════════════════════════════════════════════════════════════
    private void updateTurnUI(boolean isPlayerTurn) {
        if (isPlayerTurn) {
            currentPlayerLabel.setText("YOUR TURN");
            currentPlayerLabel.setStyle(
                    "-fx-text-fill: #00d4aa; -fx-font-size: 14px; -fx-font-weight: bold;"
                    + "-fx-background-color: #0a3d2e; -fx-background-radius: 20; -fx-padding: 6 18 6 18;");
        } else {
            currentPlayerLabel.setText("OPPONENT'S TURN");
            currentPlayerLabel.setStyle(
                    "-fx-text-fill: #e94560; -fx-font-size: 14px; -fx-font-weight: bold;"
                    + "-fx-background-color: #3d0a0a; -fx-background-radius: 20; -fx-padding: 6 18 6 18;");
        }
        rollBtn.setDisable(!isPlayerTurn);
        powerupBtn.setDisable(!isPlayerTurn);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════════════════
    private Cell getCellAt(int index) {
        Cell[][] cells = game.getBoard().getBoardCells();
        // Inverse of boardIndexOf: row = index/10, col adjusted for snake
        int row = index / Constants.BOARD_COLS;
        int col = index % Constants.BOARD_COLS;
        if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;
        return cells[row][col];
    }

    private String monsterTypeName(Monster m) {
        if (m instanceof Dasher)      return "Dasher";
        if (m instanceof Dynamo)      return "Dynamo";
        if (m instanceof Schemer)     return "Schemer";
        if (m instanceof MultiTasker) return "MultiTasker";
        return "Unknown";
    }

    private String fmtDiff(int diff) {
        return diff >= 0 ? "+" + diff : String.valueOf(diff);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // LOG
    // ═════════════════════════════════════════════════════════════════════════
    private static final Map<String, String> LOG_COLORS = new HashMap<>();
    static {
        LOG_COLORS.put("INFO",   "#888888");
        LOG_COLORS.put("MOVE",   "#3498db");
        LOG_COLORS.put("ENERGY", "#f5a623");
        LOG_COLORS.put("ACTION", "#9b59b6");
        LOG_COLORS.put("CARD",   "#2ecc71");
        LOG_COLORS.put("FREEZE", "#00bcd4");
        LOG_COLORS.put("SHIELD", "#3498db");
        LOG_COLORS.put("ERROR",  "#e74c3c");
    }

    private void log(String message, String type) {
        String color = LOG_COLORS.getOrDefault(type, "#aaaaaa");
        Label entry = new Label("▸ " + message);
        entry.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px;");
        entry.setWrapText(true);
        entry.setOpacity(0);

        FadeTransition ft = new FadeTransition(Duration.millis(350), entry);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        logBox.getChildren().add(entry);

        // Auto-scroll to bottom
        logScroll.layout();
        logScroll.setVvalue(1.0);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DIALOGS
    // ═════════════════════════════════════════════════════════════════════════
    
    private void showError(String message) {
        Stage dialog = new Stage();
        dialog.setTitle("Invalid Action");
     
        Label msg = new Label(message);
        msg.setWrapText(true);
        msg.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-padding: 20;");
     
        Button ok = new Button("OK");
        ok.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; "
                  + "-fx-font-weight: bold; -fx-background-radius: 10; "
                  + "-fx-padding: 6 24 6 24; -fx-cursor: hand;");
        ok.setOnAction(e -> dialog.close());
     
        VBox layout = new VBox(16, msg, ok);
        layout.setAlignment(javafx.geometry.Pos.CENTER);
        layout.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 20;");
     
        Scene scene = new Scene(layout, 360, 160);
        dialog.setScene(scene);
        dialog.setResizable(false);
     
        // showAndWait keeps game paused until dismissed, but does NOT terminate it
        dialog.showAndWait();
    }

    private void showWinScreen(Monster winner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/game/engine/views/WinView.fxml"));
            Parent root = loader.load();

            WinController wc = loader.getController();
            wc.init(winner, game.getPlayer(), game.getOpponent());

            Scene scene = new Scene(root);
            Stage stage = (Stage) rollBtn.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void animateDice(int finalValue) {
        rollBtn.setDisable(true);
        powerupBtn.setDisable(true);

        // Rapidly flashes random numbers before landing on real value
        Timeline animation = new Timeline();
        for (int i = 0; i < 10; i++) {
            final int frame = i;
            animation.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * 80), e -> {
                    int fake = (int)(Math.random() * 6) + 1;
                    diceLabel.setText(String.valueOf(fake));
                    diceLabel.setStyle(
                        "-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold;");
                })
            );
        }

        // Final frame shows real value with gold color
        animation.getKeyFrames().add(
            new KeyFrame(Duration.millis(900), e -> {
                diceLabel.setText(String.valueOf(finalValue));
                diceLabel.setStyle(
                    "-fx-text-fill: #f5a623; -fx-font-size: 32px; -fx-font-weight: bold;");

                // Scale pop effect
                ScaleTransition pop = new ScaleTransition(Duration.millis(200), diceLabel);
                pop.setFromX(1.0); pop.setFromY(1.0);
                pop.setToX(1.5);   pop.setToY(1.5);
                pop.setAutoReverse(true);
                pop.setCycleCount(2);
                pop.play();
            })
        );

        animation.play();
    }
}
