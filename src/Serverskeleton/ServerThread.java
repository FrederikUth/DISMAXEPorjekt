package Serverskeleton;

import Game.GameLogic;
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

            // 1. Læs navn
            String clientSentence = inFromClient.readLine();
            System.out.println(clientSentence + " er ved at forbinde...");

            synchronized (outToClient) {
                outToClient.writeBytes("Hej " + clientSentence + '\n');
            }

            // Lille pause (som du allerede havde)
            Thread.sleep(500);

            // 2. Opret spiller
            Server.threads.add(this);
            me = GameLogic.makePlayers(clientSentence);
            Server.players.add(me);

            // 3. Send SPAWN til alle
            String spawnMessage = "SPAWN " + me.getName() + " " + me.getXpos() + " " + me.getYpos() + " " + me.getDirection() + "\n";

            for (ServerThread thread : Server.threads) {
                synchronized (thread.outToClient) {
                    thread.outToClient.writeBytes(spawnMessage);
                }
            }

            // 4. Send eksisterende spillere til ny klient
            for (Player p : Server.players) {
                if (p != me) {
                    String oldPlayerMsg = "SPAWN " + p.getName() + " " + p.getXpos() + " " + p.getYpos() + " " + p.getDirection() + "\n";

                    synchronized (outToClient) {
                        outToClient.writeBytes(oldPlayerMsg);
                    }
                }
            }

            for (Treasure t : GameLogic.treasures) {
                String msg = "TREASURE " + t.getPosition().getX() + " " + t.getPosition().getY() + "\n";
                synchronized(outToClient) {
                    outToClient.writeBytes(msg);
                }
            }

            // ==========================================
            // GAME LOOP
            // ==========================================
            while (true) {
                String msg = inFromClient.readLine();
                if (msg == null) break;

                // =========================
                // MOVE
                // =========================
                if (msg.startsWith("MOVE")) {
                    String direction = msg.split(" ")[1];

                    int oldX = me.getXpos();
                    int oldY = me.getYpos();

                    int dx = 0, dy = 0;
                    switch (direction) {
                        case "up": dy = -1; break;
                        case "down": dy = 1; break;
                        case "left": dx = -1; break;
                        case "right": dx = 1; break;
                    }

                    GameLogic.updatePlayer(me, dx, dy, direction);

                    // 💀 Hvis spilleren døde
                    if (!me.isAlive()) {

                        // SEND REMOVE til alle
                        String removeMsg = "REMOVE " + me.getName() + "\n";

                        for (ServerThread thread : Server.threads) {
                            synchronized(thread.outToClient) {
                                thread.outToClient.writeBytes(removeMsg);
                            }
                        }

                        // ⏱ Respawn efter 5 sek
                        new Thread(() -> {
                            try {
                                Thread.sleep(5000);

                                pair newPos = GameLogic.getRandomFreePosition();
                                me.setLocation(newPos);
                                me.setAlive(true);

                                String spawnMsg = "SPAWN " + me.getName() + " " +
                                        me.getXpos() + " " +
                                        me.getYpos() + " " +
                                        me.getDirection() + "\n";

                                for (ServerThread thread : Server.threads) {
                                    synchronized(thread.outToClient) {
                                        thread.outToClient.writeBytes(spawnMsg);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();

                        return; // stop resten af update
                    }

                    String updateMessage = "UPDATE " + me.getName() + " " + oldX + " " + oldY + " " +
                            me.getXpos() + " " + me.getYpos() + " " + direction + " " + me.getPoints() + "\n";

                    for (ServerThread thread : Server.threads) {
                        synchronized (thread.outToClient) {
                            thread.outToClient.writeBytes(updateMessage);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("En spiller mistede forbindelsen.");
        } finally {
            // ==========================================
            // REMOVE PLAYER (DISCONNECT FIX)
            // ==========================================
            if (me != null) {
                Server.threads.remove(this);
                Server.players.remove(me);

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