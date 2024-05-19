package tap.ping_pong;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.net.Socket;

public class Cliente extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 201004;
    private PrintWriter out;
    private BufferedReader in;

    private double playerOneYPos = 200;
    private double ballXPos = 360;
    private double ballYPos = 199;

    private Rectangle P1;
    private Rectangle P2;
    private Circle BOLITA;
    private TextField SP1;
    private TextField SP2;

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("interfaz.fxml"));
        Scene scene = new Scene(loader.load());

        P1 = (Rectangle) scene.lookup("#P1");
        P2 = (Rectangle) scene.lookup("#P2");
        BOLITA = (Circle) scene.lookup("#BOLITA");
        SP1 = (TextField) scene.lookup("#SP1");
        SP2 = (TextField) scene.lookup("#SP2");

        stage.setScene(scene);
        stage.show();

        connectToServer();

        scene.setOnKeyPressed(this::handleKeyPress);
    }
    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case W -> {
                playerOneYPos = Math.max(playerOneYPos - 10, 0);
                P1.setLayoutY(playerOneYPos);
                sendMessage("POS P1 " + P1.getLayoutX() + " " + P1.getLayoutY());
            }
            case S -> {
                playerOneYPos = Math.min(playerOneYPos + 10, 400);
                P1.setLayoutY(playerOneYPos);
                sendMessage("POS P1 " + P1.getLayoutX() + " " + P1.getLayoutY());
            }
        }
    }
    private void connectToServer() {
        new Thread(() -> {
            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String input;
                while ((input = in.readLine()) != null) {
                    processServerMessage(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
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
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
