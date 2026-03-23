package Serverskeleton;

import Game.GameLogic;
import Game.GameLogic.MoveResult;
import Game.Player;
import Game.Treasure;
import Game.pair;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {

    private Socket connSocket;
    private common c;

    public Player me = null;
    public DataOutputStream outToClient;

    public ServerThread(Socket connSocket, common c) {
        this.connSocket = connSocket;
        this.c = c;
    }

    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connSocket.getInputStream()));
            outToClient = new DataOutputStream(connSocket.getOutputStream());

            // 1. Read name
            String clientSentence = inFromClient.readLine();
            System.out.println(clientSentence + " er ved at forbinde...");

            synchronized (outToClient) {
                outToClient.writeBytes("Hej " + clientSentence + '\n');
            }

            Thread.sleep(500);

            // 2. Create player
            Server.threads.add(this);
            me = GameLogic.makePlayers(clientSentence);
            Server.players.add(me);

            // 3. Send SPAWN to all
            String spawnMessage = "SPAWN " + me.getName() + " " + me.getXpos() + " " + me.getYpos() + " " + me.getDirection() + "\n";
            for (ServerThread thread : Server.threads) {
                synchronized (thread.outToClient) {
                    thread.outToClient.writeBytes(spawnMessage);
                }
            }

            // 4. Send existing players to new client
            for (Player p : Server.players) {
                if (p != me) {
                    String oldPlayerMsg = "SPAWN " + p.getName() + " " + p.getXpos() + " " + p.getYpos() + " " + p.getDirection() + "\n";
                    synchronized (outToClient) {
                        outToClient.writeBytes(oldPlayerMsg);
                    }
                }
            }

            for (Game.Treasure t : GameLogic.treasures) {
                String msg = "TREASURE " + t.getPosition().getX() + " " + t.getPosition().getY() + "\n";
                synchronized (outToClient) {
                    outToClient.writeBytes(msg);
                }
            }

            // ==========================================
            // GAME LOOP
            // ==========================================
            while (true) {
                String msg = inFromClient.readLine();
                if (msg == null) break;

                if (msg.startsWith("MOVE")) {
                    String direction = msg.split(" ")[1];

                    int oldX = me.getXpos();
                    int oldY = me.getYpos();

                    int dx = 0, dy = 0;
                    switch (direction) {
                        case "up":    dy = -1; break;
                        case "down":  dy =  1; break;
                        case "left":  dx = -1; break;
                        case "right": dx =  1; break;
                    }

                    MoveResult result = GameLogic.updatePlayer(me, dx, dy, direction);

                    if (result == MoveResult.HIT_BOMB) {
                        // Remove the bomb tile from all clients
                        if (GameLogic.lastBombHitPos != null) {
                            String removeBombMsg = "REMOVEBOMB " + GameLogic.lastBombHitPos.getX() + " " + GameLogic.lastBombHitPos.getY() + "\n";
                            for (ServerThread thread : Server.threads) {
                                synchronized (thread.outToClient) {
                                    thread.outToClient.writeBytes(removeBombMsg);
                                }
                            }
                            GameLogic.lastBombHitPos = null;
                        }

                        // Tell everyone this player is stunned (removes them visually)
                        String stunnedMsg = "STUNNED " + me.getName() + "\n";
                        for (ServerThread thread : Server.threads) {
                            synchronized (thread.outToClient) {
                                thread.outToClient.writeBytes(stunnedMsg);
                            }
                        }

                        // Respawn after 5 seconds in a background thread
                        final Player stunnedPlayer = me;
                        new Thread(() -> {
                            try {
                                Thread.sleep(5000);

                                // Find a new free position and move the player there
                                pair newPos = GameLogic.getRandomFreePosition();
                                stunnedPlayer.setXpos(newPos.getX());
                                stunnedPlayer.setYpos(newPos.getY());
                                stunnedPlayer.setDirection("up");
                                stunnedPlayer.setStunned(false);

                                // Tell all clients to respawn this player
                                String respawnMsg = "RESPAWN " + stunnedPlayer.getName() + " " +
                                        newPos.getX() + " " + newPos.getY() + " up\n";

                                for (ServerThread thread : Server.threads) {
                                    if (thread.outToClient != null) {
                                        synchronized (thread.outToClient) {
                                            thread.outToClient.writeBytes(respawnMsg);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();

                    } else if (result == MoveResult.OK) {
                        String updateMessage = "UPDATE " + me.getName() + " " + oldX + " " + oldY + " " +
                                me.getXpos() + " " + me.getYpos() + " " + direction + " " + me.getPoints() + "\n";

                        for (ServerThread thread : Server.threads) {
                            synchronized (thread.outToClient) {
                                thread.outToClient.writeBytes(updateMessage);
                            }
                        }
                    }
                    // STUNNED / WALL / BLOCKED: do nothing, ignore the move
                }
            }

        } catch (Exception e) {
            System.out.println("En spiller mistede forbindelsen.");
        } finally {
            // ==========================================
            // REMOVE PLAYER ON DISCONNECT
            // ==========================================
            if (me != null) {
                Server.threads.remove(this);
                Server.players.remove(me);
                GameLogic.players.remove(me);

                System.out.println(me.getName() + " forlod spillet.");

                String removeMsg = "REMOVE " + me.getName() + "\n";
                for (ServerThread thread : Server.threads) {
                    if (thread.outToClient != null) {
                        try {
                            synchronized (thread.outToClient) {
                                thread.outToClient.writeBytes(removeMsg);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            try {
                connSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}