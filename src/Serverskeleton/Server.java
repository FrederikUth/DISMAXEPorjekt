package Serverskeleton;
import Game.GameLogic;
import Game.Player;
import Game.Treasure;

import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
//

public class Server {
    public static List<Player> players = new CopyOnWriteArrayList<Player>();
    public static List<ServerThread> threads = new CopyOnWriteArrayList<ServerThread>();

	/**s
	 * @param args
	 */

	public static void main(String[] args)throws Exception {
		common c = new common("eksempel");
		ServerSocket welcomeSocket = new ServerSocket(6767);
		// 🔥 TREASURE SPAWN THREAD
		new Thread(() -> {
			try {
				while (true) {
					Thread.sleep(5000);

					Treasure t = GameLogic.spawnTreasure();

					String msg = "TREASURE " + t.getPosition().getX() + " " + t.getPosition().getY() + "\n";

					for (ServerThread thread : Server.threads) {
						synchronized(thread.outToClient) {
							thread.outToClient.writeBytes(msg);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			(new ServerThread(connectionSocket,c)).start();
		}

	}

}
