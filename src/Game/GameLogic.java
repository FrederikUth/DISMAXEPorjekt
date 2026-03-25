package Game;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameLogic {
    public static List<Player> players = new CopyOnWriteArrayList<>();
    public static List<Treasure> treasures = new CopyOnWriteArrayList<>();
    public static List<Bomb> bombs = new CopyOnWriteArrayList<>();

    public enum MoveResult {
        OK, WALL, BLOCKED_BY_PLAYER, HIT_BOMB, STUNNED
    }

    public static pair lastBombHitPos = null;

    public static Player makePlayers(String name) {
        pair p = getRandomFreePosition();
        Player me = new Player(name, p, "up");
        players.add(me);
        return me;
    }

    public static synchronized pair getRandomFreePosition() {
        int x, y;
        boolean foundfreepos = false;
        Random r = new Random();
        do {
            x = Math.abs(r.nextInt() % 18) + 1;
            y = Math.abs(r.nextInt() % 18) + 1;
            if (Generel.board[y].charAt(x) == ' ') {
                foundfreepos = true;
                for (Player p : players) {
                    if (p.getXpos() == x && p.getYpos() == y)
                        foundfreepos = false;
                }
            }
        } while (!foundfreepos);
        return new pair(x, y);
    }

    public static synchronized MoveResult updatePlayer(Player me, int delta_x, int delta_y, String direction) {
        if (me.isStunned()) {
            return MoveResult.STUNNED;
        }

        me.direction = direction;
        int x = me.getXpos();
        int y = me.getYpos();

        // Wall check
        if (Generel.board[y + delta_y].charAt(x + delta_x) == 'w') {
            return MoveResult.WALL;
        }

        // Bomb check
        for (Bomb b : bombs) {
            if (b.getPosition().getX() == x + delta_x &&
                    b.getPosition().getY() == y + delta_y) {
                lastBombHitPos = b.getPosition(); // save before removing
                bombs.remove(b);
                me.setStunned(true);
                return MoveResult.HIT_BOMB;
            }
        }

        // Treasure check
        for (Treasure t : treasures) {
            if (t.getPosition().getX() == x + delta_x &&
                    t.getPosition().getY() == y + delta_y) {
                me.addPoints(t.getValue());
                treasures.remove(t);
                break;
            }
        }

        // Player collision check
        if (getPlayerAt(x + delta_x, y + delta_y) != null) {
            return MoveResult.BLOCKED_BY_PLAYER;
        }

        me.setXpos(x + delta_x);
        me.setYpos(y + delta_y);
        return MoveResult.OK;
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

    public static Bomb spawnBomb() {
        pair pos = getRandomFreePosition();
        Bomb b = new Bomb(pos, 10);
        bombs.add(b);
        return b;
    }
}