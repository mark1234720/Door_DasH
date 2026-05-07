package game.engine.views;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class GameController {

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private GridPane   boardGrid;
    @FXML private Label      turnLabel;
    @FXML private Label      currentPlayerLabel;

    // Player panel
    @FXML private ImageView  playerImage;
    @FXML private Label      playerName;
    @FXML private Label      playerType;
    @FXML private Label      playerOrigRole;
    @FXML private Label      playerCurrRole;
    @FXML private Label      playerEnergy;
    @FXML private Label      playerPos;
    @FXML private VBox       playerStatusBox;

    // Opponent panel
    @FXML private ImageView  opponentImage;
    @FXML private Label      opponentName;
    @FXML private Label      opponentType;
    @FXML private Label      opponentOrigRole;
    @FXML private Label      opponentCurrRole;
    @FXML private Label      opponentEnergy;
    @FXML private Label      opponentPos;
    @FXML private VBox       opponentStatusBox;

    // Bottom
    @FXML private Label      diceLabel;
    @FXML private Label      eventLabel;
    @FXML private Button     powerupBtn;
    @FXML private Button     rollBtn;

    // Log
    @FXML private VBox       logBox;
    @FXML private ScrollPane logScroll;

    // Board background
    @FXML private ImageView  boardBg;
    @FXML private ImageView  logoImage;

    // ── State ─────────────────────────────────────────────────────────────────
    private Game game;
    private int  turnCount = 1;
    private final Map<Integer, StackPane> cellPanes = new HashMap<>();

    private static final double CELL_W = 58;
    private static final double CELL_H = 58;

    // ═════════════════════════════════════════════════════════════════════════
    // INIT
    // ═════════════════════════════════════════════════════════════════════════
    public void initGame(Role playerRole) {
        try {
            SoundManager.load();
            game = new Game(playerRole);
            loadImage(boardBg, "/game/engine/views/doors.jpeg");
            loadImage(logoImage, "/game/engine/views/monsters/MikeWazowski.png");
            buildBoard();
            refreshAll();
            log("Game started! You are " + game.getPlayer().getName()
                    + " [" + playerRole + "]", "INFO");
        } catch (Exception e) {
            showError("Failed to initialise game:\n" + e.getMessage());
        }
    }

    private void loadImage(ImageView view, String path) {
        if (view == null) return;
        try {
            Image img = new Image(getClass().getResourceAsStream(path));
            view.setImage(img);
        } catch (Exception e) {
            System.out.println("Image not found: " + path);
        }
    }

    private Image getMonsterImage(String monsterName) {
        String n = monsterName.toLowerCase();
        String file;
        if      (n.contains("sullivan") || n.contains("james") || n.contains("sully"))
            file = "JamesP.Sullivan.png";
        else if (n.contains("mike") || n.contains("wazowski"))
            file = "MikeWazowski.png";
        else if (n.contains("randall"))
            file = "Randall.png";
        else if (n.contains("mae") || n.contains("celia"))
            file = "Mae.png";
        else if (n.contains("roz"))
            file = "Roz.png";
        else if (n.contains("fungus"))
            file = "Fungus.png";
        else if (n.contains("henry"))
            file = "Henry.png";
        else if (n.contains("yeti"))
            file = "Yeti.png";
        else
            file = "MikeWazowski.png";
        try {
            return new Image(getClass().getResourceAsStream(
                    "/game/engine/views/monsters/" + file));
        } catch (Exception e) {
            return null;
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
                boardGrid.add(pane, col, Constants.BOARD_ROWS - 1 - row);
            }
        }
    }

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

        Label idx = new Label(String.valueOf(index));
        idx.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 8px;");
        StackPane.setAlignment(idx, Pos.TOP_LEFT);

        Label content = new Label(cellIcon(cell));
        content.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        content.setTextAlignment(TextAlignment.CENTER);
        content.setWrapText(true);

        pane.getChildren().addAll(idx, content);

        Tooltip tip = new Tooltip(cellTooltip(cell, index));
        tip.setStyle("-fx-font-size: 11px;");
        Tooltip.install(pane, tip);

        return pane;
    }

    // ── Cell visuals ──────────────────────────────────────────────────────────

    private String cellStyle(Cell cell) {
        // Monster Inc. themed cell colors
        String base = "-fx-background-radius: 6; -fx-border-radius: 6; -fx-border-width: 2; ";
        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            if (d.isActivated())
                return base + "-fx-background-color: #3a3a3a; -fx-border-color: #666;";
            return d.getRole() == Role.SCARER
                    ? base + "-fx-background-color: #3d0a52; -fx-border-color: #c8a8e9;"   // purple - scarer
                    : base + "-fx-background-color: #0a3d52; -fx-border-color: #7acce0;";  // teal - laugher
        }
        if (cell instanceof CardCell)
            return base + "-fx-background-color: #1a4a0a; -fx-border-color: #a8e063;";     // green - card
        if (cell instanceof MonsterCell)
            return base + "-fx-background-color: #4a2000; -fx-border-color: #f4722b;";     // orange - monster
        if (cell instanceof ConveyorBelt)
            return base + "-fx-background-color: #7a5c00; -fx-border-color: #f9d71c; "
                        + "-fx-border-width: 3; -fx-effect: dropshadow(gaussian, #f9d71c88, 6, 0, 0, 0);";
        if (cell instanceof ContaminationSock)
            return base + "-fx-background-color: #006644; -fx-border-color: #00ff99; "
                        + "-fx-border-width: 3; -fx-effect: dropshadow(gaussian, #00ff9988, 6, 0, 0, 0);";
        return base + "-fx-background-color: #0a3040; -fx-border-color: #1a6080;";         // normal
    }

    private String cellIcon(Cell cell) {
        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            String role = d.getRole() == Role.SCARER ? "😱" : "😂";
            return role + "\n" + (d.isActivated() ? "✓" : d.getEnergy());
        }
        if (cell instanceof CardCell)     return "🃏";
        if (cell instanceof MonsterCell) {
            String name = ((MonsterCell) cell).getCellMonster().getName().split(" ")[0];
            return "👾\n" + name;
        }
        if (cell instanceof ConveyorBelt) {
            int fx = ((ConveyorBelt) cell).getEffect();
            return fx > 0 ? "▶▶\n+" + fx : "◀◀\n" + fx;
        }
        if (cell instanceof ContaminationSock) {
            int fx = ((ContaminationSock) cell).getEffect();
            return "🧦\n" + fx;
        }
        return "";
    }
    private void flashCell(int index, String color) {
        StackPane pane = cellPanes.get(index);
        if (pane == null) return;

        Timeline flash = new Timeline(
            new KeyFrame(Duration.millis(0),   e -> pane.setStyle(pane.getStyle()
                    + "-fx-background-color: " + color + ";")),
            new KeyFrame(Duration.millis(150), e -> pane.setStyle(pane.getStyle()
                    + "-fx-background-color: transparent;")),
            new KeyFrame(Duration.millis(300), e -> pane.setStyle(pane.getStyle()
                    + "-fx-background-color: " + color + ";")),
            new KeyFrame(Duration.millis(450), e -> pane.setStyle(pane.getStyle()
                    + "-fx-background-color: transparent;")),
            new KeyFrame(Duration.millis(600), e -> refreshBoard())
        );
        flash.play();
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
    // ACTIONS
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void onUsePowerup() {
        Monster m = game.getCurrent();
        int energyBefore = m.getEnergy();
        try {
            game.usePowerup();
            SoundManager.playPowerup();
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
                SoundManager.playFreeze();
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
                int newPos = movingMonster.getPosition();
                final int inferredRoll = game.getLastRoll();

                // Step 1: dice animation (900ms)
                animateDice(inferredRoll);

                // Step 2: after dice finishes, walk cell by cell to destination
                new Timeline(new KeyFrame(Duration.millis(920), e -> {
                    animateMovement(posBefore, newPos, true, () -> {

                        Cell landed = getCellAt(newPos);
                        if (landed instanceof DoorCell)               SoundManager.playDoor();
                        else if (landed instanceof CardCell)          SoundManager.playCard();
                        else if (landed instanceof ContaminationSock) SoundManager.playFreeze();
                        else if(landed instanceof ConveyorBelt) SoundManager.playBonus();
                        if (landed instanceof ConveyorBelt)
                            flashCell(newPos, "#f9d71c");
                        else if (landed instanceof ContaminationSock)
                            flashCell(newPos, "#00ff99");
                        log(movingMonster.getName() + " rolled " + inferredRoll
                                + " → cell " + newPos + " (" + landed.getName() + ")", "MOVE");

                        if (landed instanceof CardCell) {
                            log("🃏 Card drawn at cell " + newPos + "!", "CARD");
                            eventLabel.setText("🃏 Card drawn! Check log.");
                        }

                        int energyDiff = movingMonster.getEnergy() - energyBefore;
                        if (energyDiff != 0) {
                            SoundManager.playEnergy();
                            log(movingMonster.getName() + " energy " + fmtDiff(energyDiff)
                                    + " → " + movingMonster.getEnergy(), "ENERGY");
                        }

                        int oppEnergyDiff = movingOpponent.getEnergy() - oppEnBefore;
                        if (oppEnergyDiff != 0)
                            log(movingOpponent.getName() + " energy " + fmtDiff(oppEnergyDiff)
                                    + " → " + movingOpponent.getEnergy(), "ENERGY");

                        if (movingMonster.isShielded() && energyDiff == 0 && landed instanceof DoorCell)
                            log("🛡 " + movingMonster.getName() + "'s shield blocked energy loss!", "SHIELD");

                        if (landed instanceof ConveyorBelt) {
                            int effect = ((ConveyorBelt) landed).getEffect();
                            eventLabel.setText("▶▶ " + movingMonster.getName() + " conveyed "
                                    + (effect > 0 ? "+" : "") + effect + " cells!");
                        } else if (landed instanceof ContaminationSock) {
                            int effect = ((ContaminationSock) landed).getEffect();
                            eventLabel.setText("🧦 " + movingMonster.getName() + " slipped! "
                                    + effect + " cells + energy penalty!");
                        } else if (!(landed instanceof CardCell)) {
                            eventLabel.setText(movingMonster.getName() + " landed on " + landed.getName());
                        }

                        turnCount++;
                        refreshAll();

                        Monster winner = game.getWinner();
                        if (winner != null) { showWinScreen(winner); return; }

                        boolean isPlayerTurn = (game.getCurrent() == game.getPlayer());
                        updateTurnUI(isPlayerTurn);
                        if (!isPlayerTurn) playOpponentTurn();
                    });
                })).play();
            }

        } catch (InvalidMoveException e) {
            showError("Invalid move:\n" + e.getMessage());
            rollBtn.setDisable(false);
            powerupBtn.setDisable(false);
        }
    }

    private void playOpponentTurn() {
        // 1.5 second pause so player can see what happened before opponent moves
        new Timeline(new KeyFrame(Duration.millis(1500), e -> {
     
            Monster opp        = game.getCurrent();
            Monster oppTarget  = (opp == game.getPlayer()) ? game.getOpponent() : game.getPlayer();
            int posBefore      = opp.getPosition();
            int enBefore       = opp.getEnergy();
            int playerEnBefore = oppTarget.getEnergy();
            boolean wasFrozen  = opp.isFrozen();
     
            try {
                game.playTurn();
     
                if (wasFrozen) {
                    SoundManager.playFreeze();
                    log(opp.getName() + " was FROZEN — turn skipped! ❄", "FREEZE");
                    diceLabel.setText("—");
                    diceLabel.setStyle("-fx-text-fill: #00c8ff; -fx-font-size: 28px; -fx-font-weight: bold;");
                    eventLabel.setText("❄ " + opp.getName() + " skipped (Frozen)");
     
                    turnCount++;
                    refreshAll();
     
                    Monster winner = game.getWinner();
                    if (winner != null) { showWinScreen(winner); return; }
     
                    updateTurnUI(true);
                    rollBtn.setDisable(false);
                    powerupBtn.setDisable(false);
     
                } else {
                    int roll    = game.getLastRoll();
                    int newPos  = opp.getPosition();
     
                    diceLabel.setText(String.valueOf(roll));
                    diceLabel.setStyle("-fx-text-fill: #f4722b; -fx-font-size: 28px; -fx-font-weight: bold;");
     
                    // Animate step by step from posBefore → newPos
                    animateMovement(posBefore, newPos, false, () -> {
     
                        Cell landed = getCellAt(newPos);
                        if (landed instanceof DoorCell)          SoundManager.playDoor();
                        else if (landed instanceof CardCell)     SoundManager.playCard();
                        else if (landed instanceof ContaminationSock) SoundManager.playFreeze();
     
                        log("👾 " + opp.getName() + " rolled " + roll
                                + " → cell " + newPos + " (" + landed.getName() + ")", "MOVE");
     
                        if (landed instanceof CardCell)
                            log("🃏 Opponent drew a card!", "CARD");
     
                        int enDiff = opp.getEnergy() - enBefore;
                        if (enDiff != 0) {
                            SoundManager.playEnergy();
                            log(opp.getName() + " energy " + fmtDiff(enDiff) + " → " + opp.getEnergy(), "ENERGY");
                        }
     
                        int playerEnDiff = oppTarget.getEnergy() - playerEnBefore;
                        if (playerEnDiff != 0)
                            log(oppTarget.getName() + " energy " + fmtDiff(playerEnDiff)
                                    + " → " + oppTarget.getEnergy(), "ENERGY");
     
                        eventLabel.setText("👾 " + opp.getName() + " moved to cell " + newPos);
     
                        turnCount++;
                        refreshAll();
     
                        Monster winner = game.getWinner();
                        if (winner != null) { showWinScreen(winner); return; }
     
                        updateTurnUI(true);
                        rollBtn.setDisable(false);
                        powerupBtn.setDisable(false);
                    });
                }
     
            } catch (InvalidMoveException ex) {
                log("Opponent invalid move: " + ex.getMessage(), "ERROR");
                rollBtn.setDisable(false);
                powerupBtn.setDisable(false);
            }
        })).play();
    }
    private void animateMovement(int fromPos, int toPos, boolean isPlayer, Runnable onDone) {
        // Build list of intermediate cells to visit
        int total = toPos - fromPos;
        if (total < 0) total += Constants.BOARD_SIZE;
     
        // 120ms per cell step
        long stepMs = 120;
        String borderColor = isPlayer ? "#a8e063" : "#f4722b";
     
        Timeline walk = new Timeline();
     
        for (int step = 0; step <= total; step++) {
            final int visitIndex = (fromPos + step) % Constants.BOARD_SIZE;
            final int prevIndex  = step == 0 ? -1 : (fromPos + step - 1) % Constants.BOARD_SIZE;
     
            walk.getKeyFrames().add(new KeyFrame(Duration.millis(step * stepMs), e -> {
     
                // Remove highlight from previous cell
                if (prevIndex >= 0) {
                    StackPane prev = cellPanes.get(prevIndex);
                    if (prev != null) {
                        Cell prevCell = getCellAt(prevIndex);
                        prev.setStyle(cellStyle(prevCell));
                    }
                }
     
                // Highlight current cell
                StackPane cur = cellPanes.get(visitIndex);
                if (cur != null) {
                    cur.setStyle(cur.getStyle()
                            + "-fx-border-color: " + borderColor + "; -fx-border-width: 3;");
     
                    // Tiny bounce on each step
                    ScaleTransition bounce = new ScaleTransition(Duration.millis(80), cur);
                    bounce.setFromX(1.0); bounce.setFromY(1.0);
                    bounce.setToX(1.12);  bounce.setToY(1.12);
                    bounce.setAutoReverse(true);
                    bounce.setCycleCount(2);
                    bounce.play();
                }
            }));
        }
     
        // After last step, run the callback
        walk.getKeyFrames().add(new KeyFrame(Duration.millis((total + 1) * stepMs), e -> {
            if (onDone != null) onDone.run();
        }));
     
        walk.play();
    } 
    // ═════════════════════════════════════════════════════════════════════════
    // UI REFRESH
    // ═════════════════════════════════════════════════════════════════════════
    private void refreshAll() {
        turnLabel.setText(String.valueOf(turnCount));
        refreshMonsterPanel(game.getPlayer(),
                playerImage, playerName, playerType, playerOrigRole,
                playerCurrRole, playerEnergy, playerPos, playerStatusBox, true);
        refreshMonsterPanel(game.getOpponent(),
                opponentImage, opponentName, opponentType, opponentOrigRole,
                opponentCurrRole, opponentEnergy, opponentPos, opponentStatusBox, false);
        refreshBoard();
    }

    private void refreshMonsterPanel(Monster m, ImageView portrait,
            Label name, Label type, Label origRole, Label currRole,
            Label energy, Label pos, VBox statusBox, boolean isPlayer) {

        // Update portrait image
        Image img = getMonsterImage(m.getName());
        if (img != null && portrait != null) portrait.setImage(img);

        name.setText(m.getName());
        type.setText(monsterTypeName(m));
        origRole.setText(m.getOriginalRole().toString());

        if (m.isConfused()) {
            currRole.setText(m.getRole() + " 😵");
            currRole.setStyle("-fx-text-fill: #f9d71c; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            currRole.setText(m.getRole().toString());
            currRole.setStyle("-fx-text-fill: " + (isPlayer ? "#a8e063" : "#f4722b")
                    + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        }

        energy.setText(String.valueOf(m.getEnergy()));
        if (m.getEnergy() >= Constants.WINNING_ENERGY)
            energy.setStyle("-fx-text-fill: #a8e063; -fx-font-size: 30px; -fx-font-weight: bold;");
        else if (m.getEnergy() >= 300)
            energy.setStyle("-fx-text-fill: #f9d71c; -fx-font-size: 30px; -fx-font-weight: bold;");
        else
            energy.setStyle("-fx-text-fill: #f4722b; -fx-font-size: 30px; -fx-font-weight: bold;");

        pos.setText(String.valueOf(m.getPosition()));

        statusBox.getChildren().clear();
        if (m.isShielded())
            statusBox.getChildren().add(chip("🛡 Shield", "#7acce0"));
        if (m.isConfused())
            statusBox.getChildren().add(chip("😵 Confused ×" + m.getConfusionTurns(), "#f9d71c"));
        if (m.isFrozen())
            statusBox.getChildren().add(chip("❄ Frozen", "#00c8ff"));
        if (m instanceof Dasher && ((Dasher) m).getMomentumTurns() > 0)
            statusBox.getChildren().add(chip("💨 Momentum ×" + ((Dasher) m).getMomentumTurns(), "#c8a8e9"));
        if (m instanceof MultiTasker && ((MultiTasker) m).getNormalSpeedTurns() > 0)
            statusBox.getChildren().add(chip("🎯 Focus ×" + ((MultiTasker) m).getNormalSpeedTurns(), "#a8e063"));
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

                pane.setStyle(cellStyle(cell));
                if (pane.getChildren().size() >= 2)
                    ((Label) pane.getChildren().get(1)).setText(cellIcon(cell));

                boolean hasPlayer   = (game.getPlayer().getPosition() == index);
                boolean hasOpponent = (game.getOpponent().getPosition() == index);

                if (hasPlayer && hasOpponent) {
                    pane.setStyle(pane.getStyle() + "-fx-border-color: #f9d71c; -fx-border-width: 3;");
                } else if (hasPlayer) {
                    pane.setStyle(pane.getStyle() + "-fx-border-color: #a8e063; -fx-border-width: 3;");
                    pulse(pane);
                } else if (hasOpponent) {
                    pane.setStyle(pane.getStyle() + "-fx-border-color: #f4722b; -fx-border-width: 3;");
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
                    "-fx-text-fill: #042030; -fx-font-size: 13px; -fx-font-weight: bold;"
                    + "-fx-background-color: #a8e063; -fx-background-radius: 22; -fx-padding: 8 22 8 22;");
        } else {
            currentPlayerLabel.setText("OPPONENT'S TURN");
            currentPlayerLabel.setStyle(
                    "-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;"
                    + "-fx-background-color: #f4722b; -fx-background-radius: 22; -fx-padding: 8 22 8 22;");
        }
        rollBtn.setDisable(!isPlayerTurn);
        powerupBtn.setDisable(!isPlayerTurn);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DICE ANIMATION
    // ═════════════════════════════════════════════════════════════════════════
    private void animateDice(int finalValue) {
        SoundManager.playRoll();
        Timeline animation = new Timeline();
        for (int i = 0; i < 10; i++) {
            animation.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * 80), e -> {
                    int fake = (int)(Math.random() * 6) + 1;
                    diceLabel.setText(String.valueOf(fake));
                    diceLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold;");
                })
            );
        }
        animation.getKeyFrames().add(
            new KeyFrame(Duration.millis(900), e -> {
                diceLabel.setText(String.valueOf(finalValue));
                diceLabel.setStyle("-fx-text-fill: #f9d71c; -fx-font-size: 32px; -fx-font-weight: bold;");
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

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════════════════
    private Cell getCellAt(int index) {
        Cell[][] cells = game.getBoard().getBoardCells();
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
        LOG_COLORS.put("INFO",   "#7acce0");
        LOG_COLORS.put("MOVE",   "#a8e063");
        LOG_COLORS.put("ENERGY", "#f9d71c");
        LOG_COLORS.put("ACTION", "#c8a8e9");
        LOG_COLORS.put("CARD",   "#00c896");
        LOG_COLORS.put("FREEZE", "#00c8ff");
        LOG_COLORS.put("SHIELD", "#7acce0");
        LOG_COLORS.put("ERROR",  "#f4722b");
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
        ok.setStyle("-fx-background-color: #f4722b; -fx-text-fill: white;"
                  + "-fx-font-weight: bold; -fx-background-radius: 10;"
                  + "-fx-padding: 6 24 6 24; -fx-cursor: hand;");
        ok.setOnAction(e -> dialog.close());

        VBox layout = new VBox(16, msg, ok);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #0a3040; -fx-padding: 20;");

        Scene scene = new Scene(layout, 380, 170);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    private void showWinScreen(Monster winner) {
        try {
            SoundManager.playWin();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/game/engine/views/WinView.fxml"));
            Parent root = loader.load();
            WinController wc = loader.getController();
            wc.init(winner, game.getPlayer(), game.getOpponent());
            Stage stage = (Stage) rollBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}