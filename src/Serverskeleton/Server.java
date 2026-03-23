package Serverskeleton;
import Game.Bomb;
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
		// TREASURE SPAWN THREAD
		new Thread(() -> {
			try {
				while (true) {
					Thread.sleep(2000);

					if (Math.random() < 0.5) {
						// 💰 Treasure
						Treasure t = GameLogic.spawnTreasure();

						String msg = "TREASURE " + t.getPosition().getX() + " " +
								t.getPosition().getY() + "\n";

						for (ServerThread thread : Server.threads) {
							synchronized(thread.outToClient) {
								thread.outToClient.writeBytes(msg);
							}
						}

					} else {
						// 💣 Bomb
						Bomb b = GameLogic.spawnBomb();

						String msg = "BOMB " + b.getPosition().getX() + " " +
								b.getPosition().getY() + "\n";

						for (ServerThread thread : Server.threads) {
							synchronized(thread.outToClient) {
								thread.outToClient.writeBytes(msg);
							}
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
