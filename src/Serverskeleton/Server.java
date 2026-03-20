package Serverskeleton;
import Game.Player;

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
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			(new ServerThread(connectionSocket,c)).start();
		}
	}

}
