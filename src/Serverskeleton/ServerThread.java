package Serverskeleton;
import Game.GameLogic;
import Game.Player;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
    Socket connSocket;
    common c;

    // Fjernet 'static' så hver klient har sin egen spiller!
    public Player me = null;
    public DataOutputStream outToClient;

    public ServerThread(Socket connSocket, common c) {
        this.connSocket = connSocket;
        this.c = c;
    }

    public synchronized void updateMessage(){

    }

    public synchronized void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
            outToClient = new DataOutputStream(connSocket.getOutputStream());

            // 1. Læs navnet fra klienten
            String clientSentence = inFromClient.readLine();
            System.out.println(clientSentence + " er ved at forbinde...");
            outToClient.writeBytes("Hej " + clientSentence + '\n');

            // ==========================================
            // DET SMARTE TRICK: Sove-tid!
            // ==========================================
            // Vi venter lige et halvt sekund (500 millisekunder), så klientens
            // JavaFX-vindue har tid til at loade hele brættet op, før vi sender spillere.
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 2. Tilføj denne tråd og spiller til serverens lister
            Server.threads.add(this);
            me = GameLogic.makePlayers(clientSentence);
            Server.players.add(me);

            // ==========================================
            // SYNKRONISERING AF SPILLERE
            // ==========================================
            // Fortæl ALLE klienter at der er logget en ny ind
            String spawnMessage = "SPAWN " + me.getName() + " " + me.getXpos() + " " + me.getYpos() + " " + me.getDirection() + "\n";
            for (ServerThread thread : Server.threads) {
                thread.outToClient.writeBytes(spawnMessage);
            }

            // Fortæl den NYE klient om de gamle spillere
            for (Player p : Server.players) {
                if (p != me) { // Send ikke ham selv igen
                    String oldPlayerMsg = "SPAWN " + p.getName() + " " + p.getXpos() + " " + p.getYpos() + " " + p.getDirection() + "\n";
                    outToClient.writeBytes(oldPlayerMsg);
                }
            }

            // ==========================================
            // SPILLE-LØKKEN (Lyt efter bevægelser)
            // ==========================================
            while (true) {
                String msg = inFromClient.readLine();
                if (msg == null) break; // Klienten afbrød forbindelsen

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

                    // Tjek om trækket er lovligt (mur/kollision)
                    GameLogic.updatePlayer(me, dx, dy, direction);

                    // Broadcast bevægelsen til ALLE klienter
                    String updateMessage = "UPDATE " + me.getName() + " " + oldX + " " + oldY + " " + me.getXpos() + " " + me.getYpos() + " " + direction + "\n";
                    for (ServerThread thread : Server.threads) {
                        thread.outToClient.writeBytes(updateMessage);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("En spiller mistede forbindelsen.");
        } finally {

            if (me != null) {
                Server.threads.remove(this);
                Server.players.remove(me);
                System.out.println(me.getName() + " forlod spillet.");
            }
        }
    }
}