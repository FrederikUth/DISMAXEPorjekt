package Game;

import java.io.DataOutputStream;

public class Player {
	String name;
	pair location;
	public int point;
	String direction;
    DataOutputStream outToClient;


    public Player(String name, pair loc, String direction) {
		this.name = name;
		this.location = loc;
		this.direction = direction;
		this.point = 0;

	};
	public pair getLocation() {
		return this.location;
	}

	public void setLocation(pair p) {
		this.location=p;
	}

	public int getXpos() {
		return location.getX();
	}

	public void setXpos(int xpos) {
		this.location.setX(xpos);
	}

	public int getYpos() {
		return location.getY();
	}

	public void setYpos(int ypos) {
		this.location.setY(ypos);
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public void addPoints(int p) {
		point+=p;
	}
	public String toString() {
		return name+":   "+point;
	}
    public String getName() {
        return this.name;
    }

}

