package Serverskeleton;
import Game.GameLogic;
import Game.Player;

import java.net.*;
import java.io.*;
public class ServerThread extends Thread{
	Socket connSocket;
	common c;
    public static Player me = null;
	
	public ServerThread(Socket connSocket,common c) {
		this.connSocket = connSocket;
		this.c=c; // Til Web-server opgaven skal denne ikke anvendes
	}
	public void run() {
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connSocket.getOutputStream());
			
			// Do the work and the communication with the client here	
			// The following two lines are only an example
			
			String clientSentence = inFromClient.readLine();
			outToClient.writeBytes("Hej"+ c.getTekst() + '\n' );
           me = GameLogic.makePlayers(clientSentence);
           Server.players.add(me);

			while (true) {
				String msg = inFromClient.readLine();

				if (msg == null) break;

				if (msg.startsWith("MOVE")) {
					String direction = msg.split(" ")[1];

					int dx = 0, dy = 0;

					switch (direction) {
						case "up": dy = -1; break;
						case "down": dy = 1; break;
						case "left": dx = -1; break;
						case "right": dx = 1; break;
					}

					GameLogic.updatePlayer(me, dx, dy, direction);

					// send update til klienter (du mangler broadcast)
				}
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}		
		// do the work here
	}
}
//hej