package Game;

import java.net.*;
import java.io.*;
import javafx.application.Application;

public class App {
    public static DataOutputStream outToServer;

    public static void main(String[] args) throws Exception {
        String modifiedSentence;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = new Socket("10.10.132.48", 6767);

        App.outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        System.out.println("Indtast spillernavn");
        String navn = inFromUser.readLine();

        App.outToServer.writeBytes(navn + '\n');

        modifiedSentence = inFromServer.readLine();
        System.out.println("FROM SERVER: " + modifiedSentence);

        ClientThread listener = new ClientThread(inFromServer);
        listener.start();

        Application.launch(Gui.class);
    }
}