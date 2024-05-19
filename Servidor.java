package tap.ping_pong;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Servidor {

    private static final int SERVER_PORT = 201004;
    private static final Set<PrintWriter> clientWriters = new CopyOnWriteArraySet<>();
    private static double ballX = 360;
    private static double ballY = 200;
    private static double ballXSpeed = 1;
    private static double ballYSpeed = 1;
    private static double p1Y = 160;
    private static double p2Y = 160;
    private static int scoreP1 = 0;
    private static int scoreP2 = 0;

    public static void main(String[] args) throws IOException {
        System.out.println("Server started on port " + SERVER_PORT);
        ServerSocket listener = new ServerSocket(SERVER_PORT);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(Servidor::updateGame, 0, 10, TimeUnit.MILLISECONDS);
        try {
            while (true) {
                new ClientHandler(listener.accept()).start();
            }
        } finally {
            listener.close();
            executor.shutdown();
        }
    }

    private static void updateGame() {
        ballX += ballXSpeed;
        ballY += ballYSpeed;
        if (ballY <= 0 || ballY >= 400 - 14 * 2) {
            ballYSpeed *= -1;
        }
        if (ballX <= 34 && ballY + 14 >= p1Y && ballY - 14 <= p1Y + 80) {
            ballXSpeed *= -1;
            ballX = 34;
        } else if (ballX >= 686 - 14 && ballY + 14 >= p2Y && ballY - 14 <= p2Y + 80) {
            ballXSpeed *= -1;
            ballX = 686 - 14;
        }
        if (ballX <= 0) {
            scoreP2++;
            resetBall();
            sendScoreUpdate();
        } else if (ballX >= 720) {
            scoreP1++;
            resetBall();
            sendScoreUpdate();
        }
        String message = "BALL " + ballX + " " + ballY;
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }
    private static void resetBall() {
        ballX = 360;
        ballY = 200;
        ballXSpeed = 1;
        ballYSpeed = 1;
    }
    private static void sendScoreUpdate() {
        String message = "SCORE " + scoreP1 + " " + scoreP2;
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }
    private static class ClientHandler extends Thread {
        private final Socket socket;
        private PrintWriter out;
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    String[] parts = message.split(" ");
                    if (parts[0].equals("POS")) {
                        if (parts[1].equals("P1")) {
                            p1Y = Double.parseDouble(parts[3]);
                            System.out.println("Servidor: Actualizando P1 a " + p1Y);
                        } else if (parts[1].equals("P2")) {
                            p2Y = Double.parseDouble(parts[3]);
                            System.out.println("Servidor: Actualizando P2 a " + p2Y);
                        }
                    }
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }
    }
}