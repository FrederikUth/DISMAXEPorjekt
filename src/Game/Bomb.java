package Game;

public class Bomb {
    private pair position;
    private int damage;

    public Bomb(pair position, int damage) {
        this.position = position;
        this.damage = damage;
    }

    public pair getPosition() {
        return position;
    }

    public int getDamage() {
        return damage;
    }
}