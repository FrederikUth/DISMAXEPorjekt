package Serverskeleton;

import Game.GameLogic;
import Game.Player;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
    Socket connSocket;
    common c;
    public Player me = null;

    public ServerThread(Socket connSocket, common c) {
        this.connSocket = connSocket;
        this.c = c;
    }

    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(
                    connSocket.getOutputStream());

            // Første besked fra klienten = navn
            String clientName = inFromClient.readLine();
            outToClient.writeBytes("Hej " + c.getTekst() + '\n');

            me = GameLogic.makePlayers(clientName);
            Server.players.add(me);

            // Lyt efter bevægelse
            while (true) {
                String message = inFromClient.readLine();

                if (message == null) {
                    break;
                }

                if (message.startsWith("MOVE ")) {
                    String direction = message.substring(5);

                    int dx = 0;
                    int dy = 0;

                    switch (direction) {
                        case "up":
                            dy = -1;
                            break;
                        case "down":
                            dy = 1;
                            break;
                        case "left":
                            dx = -1;
                            break;
                        case "right":
                            dx = 1;
                            break;
                    }

                    GameLogic.updatePlayer(me, dx, dy, direction);
                    outToClient.writeBytes("OK\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}