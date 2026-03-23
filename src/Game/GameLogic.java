package Game;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Random;

public class GameLogic {
    public static List<Player> players = new CopyOnWriteArrayList<Player>();
    public static List<Treasure> treasures = new CopyOnWriteArrayList<Treasure>();

    public static Player makePlayers(String name) {
        pair p = getRandomFreePosition();
        Player me = new Player(name, p, "up");
        players.add(me);
        return me;
    }

    public static synchronized pair getRandomFreePosition() {
        int x = 1;
        int y = 1;
        boolean foundfreepos = false;
        while (!foundfreepos) {
            Random r = new Random();
            x = Math.abs(r.nextInt() % 18) + 1;
            y = Math.abs(r.nextInt() % 18) + 1;
            if (Generel.board[y].charAt(x) == ' ') // er det gulv ?
            {
                foundfreepos = true;
                for (Player p : players) {
                    if (p.getXpos() == x && p.getYpos() == y) // pladsen optaget af en anden
                        foundfreepos = false;
                }
            }
        }
        pair p = new pair(x, y);
        return p;
    }

    public static synchronized void updatePlayer(Player me, int delta_x, int delta_y, String direction) {
        me.direction = direction;
        int x = me.getXpos();
        int y = me.getYpos();

        if (Generel.board[y + delta_y].charAt(x + delta_x) == 'w') {
            me.addPoints(0);
        }
        else {
            // 🔥 TJEK TREASURE
            for (Treasure t : treasures) {
                if (t.getPosition().getX() == x + delta_x &&
                        t.getPosition().getY() == y + delta_y) {

                    me.addPoints(t.getValue());
                    treasures.remove(t);
                    break;
                }
            }
            // Kollisions-detektion
            Player p = getPlayerAt(x + delta_x, y + delta_y);
            if (p != null) {
                return;
            }

            me.setXpos(x + delta_x);
            me.setYpos(y + delta_y);
        }
    }

    public static Player getPlayerAt(int x, int y) {
        for (Player p : players) {
            if (p.getXpos() == x && p.getYpos() == y) {
                return p;
            }
        }
        return null;
    }

    public static Treasure spawnTreasure() {
        pair pos = getRandomFreePosition();
        Treasure t = new Treasure(pos, 10);
        treasures.add(t);
        return t;
    }
}