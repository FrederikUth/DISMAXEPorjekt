import java.net.*;
import java.io.*;
import javafx.application.Application;

public class App {
	public static Player me=null;
	public static void main(String[] args) throws Exception{
        String sentence;
        String modifiedSentence;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket= new Socket("localhost",6767);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("Indtast spillernavn");
        String navn = inFromUser.readLine();
        outToServer.writeBytes(navn + '\n');
        modifiedSentence = inFromServer.readLine();
        System.out.println("FROM SERVER: " + modifiedSentence);
        clientSocket.close();

		me = GameLogic.makePlayers(navn);
		Application.launch(Gui.class);
	}
}
;