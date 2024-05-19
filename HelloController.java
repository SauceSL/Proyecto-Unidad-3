package tap.ping_pong;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HelloController {
    @FXML private Circle BOLITA;
    @FXML private Rectangle P1;
    @FXML private Rectangle P2;
    @FXML private TextField SP1;
    @FXML private TextField SP2;

    private PrintWriter out;
    private BufferedReader in;

    public void initialize() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), e -> runGame()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        SP1.setFocusTraversable(false);
        SP2.setFocusTraversable(false);
        new Thread(this::connectToServer).start();
    }

    @FXML
    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case W -> movePlayer(P1, -10);
            case S -> movePlayer(P1, 10);
            case UP -> movePlayer(P2, -10);
            case DOWN -> movePlayer(P2, 10);
        }
    }

    private void movePlayer(Rectangle player, double deltaY) {
        double newY = player.getLayoutY() + deltaY;
        if (newY >= 0 && newY <= 320) {
            player.setLayoutY(newY);
            sendMessage("POS " + (player == P1 ? "P1" : "P2") + " " + player.getLayoutX() + " " + player.getLayoutY());
        }
    }

    private void runGame() {
    }

    private void connectToServer() {
        try (Socket socket = new Socket("localhost", 12346)) {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String input;
            while ((input = in.readLine()) != null) {
                String finalInput = input;
                Platform.runLater(() -> processServerMessage(finalInput));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void processServerMessage(String message) {
        String[] parts = message.split(" ");
        String command = parts[0];

        switch (command) {
            case "POS":
                handlePositionMessage(parts);
                break;
            case "BALL":
                handleBallMessage(parts);
                break;
            case "SCORE":
                handleScoreMessage(parts);
                break;
            default:
                System.out.println("Unknown message: " + message);
        }
    }

    private void handlePositionMessage(String[] parts) {
        String player = parts[1];
        double x = Double.parseDouble(parts[2]);
        double y = Double.parseDouble(parts[3]);

        Platform.runLater(() -> {
            if (player.equals("P1")) {
                P1.setLayoutX(x);
                P1.setLayoutY(y);
            } else if (player.equals("P2")) {
                P2.setLayoutX(x);
                P2.setLayoutY(y);
            }
        });
    }

    private void handleBallMessage(String[] parts) {
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);

        Platform.runLater(() -> {
            BOLITA.setLayoutX(x);
            BOLITA.setLayoutY(y);
        });
    }

    private void handleScoreMessage(String[] parts) {
        int scoreP1 = Integer.parseInt(parts[1]);
        int scoreP2 = Integer.parseInt(parts[2]);

        Platform.runLater(() -> {
            SP1.setText(String.valueOf(scoreP1));
            SP2.setText(String.valueOf(scoreP2));
        });
    }

    private void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}


