package Game;

public class Treasure {
    private pair position;
    private int value;

    public Treasure(pair position, int value) {
        this.position = position;
        this.value = value;
    }

    public pair getPosition() {
        return position;
    }

    public int getValue() {
        return value;
    }
}