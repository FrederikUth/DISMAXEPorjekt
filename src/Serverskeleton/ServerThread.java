package Serverskeleton;
import Game.GameLogic;
import Game.Player;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
    Socket connSocket;
    common c;

    // RETTET: Fjernet 'static', så hver tråd/klient har sin egen spiller!
    public Player me = null;

    // NYT: Vi gemmer outToClient heroppe, så broadcast-løkken kan få fat i den
    public DataOutputStream outToClient;

    public ServerThread(Socket connSocket, common c) {
        this.connSocket = connSocket;
        this.c = c;
    }

    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
            // Tildel værdien til vores klasse-variabel
            outToClient = new DataOutputStream(connSocket.getOutputStream());

            // NYT: Tilføj denne specifikke tråd til serverens fælles liste over klienter
            Server.threads.add(this);

            String clientSentence = inFromClient.readLine();
            outToClient.writeBytes("Hej " + clientSentence + '\n');
            me = GameLogic.makePlayers(clientSentence);
            Server.players.add(me);

            String spawnMessage = "SPAWN " + me.getName() + " " + me.getXpos() + " " + me.getYpos() + " " + me.getDirection() + "\n";
            outToClient.writeBytes(spawnMessage);

            while (true) {
                String msg = inFromClient.readLine();

                if (msg == null) break;

                if (msg.startsWith("MOVE")) {
                    String direction = msg.split(" ")[1];

                    // Husk den gamle position til UPDATE-beskeden
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

                    // NYT: Den færdige BROADCAST funktion!
                    // Vi bygger beskeden én gang...
                    String updateMessage = "UPDATE " + me.getName() + " " + oldX + " " + oldY + " " + me.getXpos() + " " + me.getYpos() + " " + direction + "\n";

                    // ... og sender den til ALLE tråde i listen (inklusive os selv)
                    for (ServerThread thread : Server.threads) {
                        thread.outToClient.writeBytes(updateMessage);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // NYT: God stil at rydde op, hvis en klient lukker spillet
            Server.threads.remove(this);
            Server.players.remove(me);
            System.out.println(me.getName() + " forlod spillet.");
        }
    }
}