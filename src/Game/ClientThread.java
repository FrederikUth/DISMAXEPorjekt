package Game;

import java.io.BufferedReader;
import java.io.IOException;
import javafx.application.Platform;

public class ClientThread extends Thread {
    private BufferedReader inFromServer;

    public ClientThread(BufferedReader inFromServer) {
        this.inFromServer = inFromServer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Tråden venter her, indtil serveren sender en besked
                String message = inFromServer.readLine();
                System.out.println(message);

                if (message == null) {
                    System.out.println("Forbindelsen til serveren blev afbrudt.");
                    break;
                }

                System.out.println("Modtog fra server: " + message);

                // Vi deler beskeden op i bidder (adskilt af mellemrum)
                String[] tokens = message.split(" ");
                String command = tokens[0];

                // ==========================================
                // HÅNDTER SPAWN (Nye spillere)
                // ==========================================
                if (command.equals("SPAWN")) {
                    String name = tokens[1];
                    int x = Integer.parseInt(tokens[2]);
                    int y = Integer.parseInt(tokens[3]);
                    String direction = tokens[4];

                    pair p = new pair(x, y);
                    Player newPlayer = new Player(name, p, direction);
                    GameLogic.players.add(newPlayer);

                    Platform.runLater(() -> {
                        Gui.placePlayerOnScreen(p, direction);
                    });
                }

                // ==========================================
                // HÅNDTER UPDATE (Bevægelse)
                // ==========================================
                else if (command.equals("UPDATE")) {
                    String name = tokens[1];
                    int oldX = Integer.parseInt(tokens[2]);
                    int oldY = Integer.parseInt(tokens[3]);
                    int newX = Integer.parseInt(tokens[4]);
                    int newY = Integer.parseInt(tokens[5]);
                    String direction = tokens[6];

                    // Find spilleren via NAVN i vores lokale liste
                    Player playerToMove = null;
                    for (Player p : GameLogic.players) {
                        if (p.getName().equals(name)) {
                            playerToMove = p;
                            break; // Stop med at lede, når vi har fundet ham
                        }
                    }

                    // Hvis vi fandt spilleren, opdaterer vi hans position og tegner ham
                    if (playerToMove != null) {
                        playerToMove.setXpos(newX);
                        playerToMove.setYpos(newY);
                        playerToMove.setDirection(direction);

                        // Bed GUI'en om at rykke billedet på skærmen (skal gøres på JavaFX tråden)
                        Platform.runLater(() -> {
                            pair oldPos = new pair(oldX, oldY);
                            pair newPos = new pair(newX, newY);
                            Gui.movePlayerOnScreen(oldPos, newPos, direction);
                        });
                    }
                }

                else if (command.equals("TREASURE")) {
                    int x = Integer.parseInt(tokens[1]);
                    int y = Integer.parseInt(tokens[2]);

                    pair pos = new pair(x, y);

                    Platform.runLater(() -> {
                        Gui.placeTreasure(pos);
                    });
                }

                else if (command.equals("REMOVE")) {
                    String name = tokens[1];

                    Player toRemove = null;

                    // 🔍 Find spilleren
                    for (Player p : GameLogic.players) {
                        if (p.getName().equals(name)) {
                            toRemove = p;
                            break;
                        }
                    }

                    // ❌ Fjern spilleren
                    if (toRemove != null) {
                        GameLogic.players.remove(toRemove);

                        Player finalPlayer = toRemove;

                        // 🎮 Fjern fra GUI
                        Platform.runLater(() -> {
                            Gui.removePlayerOnScreen(finalPlayer.getLocation());
                        });
                    }
                }

                else if (command.equals("TREASURE")) {
                    int x = Integer.parseInt(tokens[1]);
                    int y = Integer.parseInt(tokens[2]);

                    pair pos = new pair(x, y);

                    Platform.runLater(() -> {
                        Gui.placeTreasure(pos);
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}