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

                if (message == null) {
                    System.out.println("Forbindelsen til serveren blev afbrudt.");
                    break;
                }

                System.out.println("Modtog fra server: " + message);

                // Vi deler beskeden op i bidder (adskilt af mellemrum)
                String[] tokens = message.split(" ");
                String command = tokens[0]; // F.eks. "SPAWN"

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
                        // Hvis du vil have scorelisten opdateret med det samme:
                        // Gui.updateScoreTable(); // Kræver at metoden er static i Gui
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}