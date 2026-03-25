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
                String message = inFromServer.readLine();

                if (message == null) {
                    System.out.println("Forbindelsen til serveren blev afbrudt.");
                    break;
                }

                System.out.println("Modtog fra server: " + message);

                String[] tokens = message.split(" ");
                String command = tokens[0];

                // SPAWN
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

                // UPDATE (movement)
                else if (command.equals("UPDATE")) {
                    String name = tokens[1];
                    int oldX = Integer.parseInt(tokens[2]);
                    int oldY = Integer.parseInt(tokens[3]);
                    int newX = Integer.parseInt(tokens[4]);
                    int newY = Integer.parseInt(tokens[5]);
                    String direction = tokens[6];
                    int points = Integer.parseInt(tokens[7]);

                    Player playerToMove = null;
                    for (Player p : GameLogic.players) {
                        if (p.getName().equals(name)) {
                            playerToMove = p;
                            break;
                        }
                    }

                    if (playerToMove != null) {
                        playerToMove.setXpos(newX);
                        playerToMove.setYpos(newY);
                        playerToMove.setDirection(direction);
                        playerToMove.setPoints(points);

                        final Player finalPlayer = playerToMove;
                        Platform.runLater(() -> {
                            pair oldPos = new pair(oldX, oldY);
                            pair newPos = new pair(newX, newY);
                            Gui.movePlayerOnScreen(oldPos, newPos, direction);
                            Gui.refreshScore();
                        });
                    }
                }

                // TREASURE
                else if (command.equals("TREASURE")) {
                    int x = Integer.parseInt(tokens[1]);
                    int y = Integer.parseInt(tokens[2]);
                    pair pos = new pair(x, y);
                    Platform.runLater(() -> Gui.placeTreasure(pos));
                }

                // BOMB
                else if (command.equals("BOMB")) {
                    int x = Integer.parseInt(tokens[1]);
                    int y = Integer.parseInt(tokens[2]);
                    pair pos = new pair(x, y);
                    Platform.runLater(() -> Gui.placeBomb(pos));
                }


                else if (command.equals("REMOVEBOMB")) {
                    int x = Integer.parseInt(tokens[1]);
                    int y = Integer.parseInt(tokens[2]);
                    pair pos = new pair(x, y);
                    Platform.runLater(() -> Gui.removeBomb(pos));
                }


                else if (command.equals("STUNNED")) {
                    String name = tokens[1];

                    Player stunned = null;
                    for (Player p : GameLogic.players) {
                        if (p.getName().equals(name)) {
                            stunned = p;
                            break;
                        }
                    }

                    if (stunned != null) {
                        final Player stunnedPlayer = stunned;
                        stunnedPlayer.setStunned(true);

                        // Remove player visually (they're "down")
                        Platform.runLater(() -> {
                            Gui.removePlayerOnScreen(stunnedPlayer.getLocation());
                        });
                    }
                }

                // ==========================================
                // RESPAWN — put player back on screen
                // ==========================================
                else if (command.equals("RESPAWN")) {
                    String name = tokens[1];
                    int x = Integer.parseInt(tokens[2]);
                    int y = Integer.parseInt(tokens[3]);
                    String direction = tokens[4];

                    Player respawned = null;
                    for (Player p : GameLogic.players) {
                        if (p.getName().equals(name)) {
                            respawned = p;
                            break;
                        }
                    }

                    if (respawned != null) {
                        final Player rp = respawned;
                        rp.setXpos(x);
                        rp.setYpos(y);
                        rp.setDirection(direction);
                        rp.setStunned(false);

                        pair newPos = new pair(x, y);
                        Platform.runLater(() -> {
                            Gui.placePlayerOnScreen(newPos, direction);
                            Gui.refreshScore();
                        });
                    }
                }

                // REMOVE (disconnect)
                else if (command.equals("REMOVE")) {
                    String name = tokens[1];

                    Player toRemove = null;
                    for (Player p : GameLogic.players) {
                        if (p.getName().equals(name)) {
                            toRemove = p;
                            break;
                        }
                    }

                    if (toRemove != null) {
                        GameLogic.players.remove(toRemove);
                        final Player finalPlayer = toRemove;
                        Platform.runLater(() -> {
                            Gui.removePlayerOnScreen(finalPlayer.getLocation());
                        });
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}