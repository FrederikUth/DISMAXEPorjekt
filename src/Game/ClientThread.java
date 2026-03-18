package Game;

import java.io.BufferedReader;
import java.io.IOException;

public class ClientThread extends Thread{
    private BufferedReader inFromServer;

    public ClientThread (BufferedReader inFromServer){
    this.inFromServer=inFromServer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = inFromServer.readLine();
                System.out.println(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
