package Game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConcurrentArrayList implements Iterable<Player>
{
	private List<Player> liste = new ArrayList<Player>(); 
	
	public void add(Player p) {
		liste.add(p);	
	}

	public void remove(Player p) {
		liste.remove(p);	
	}
	
	public void clear() {
		liste.clear();
	}
	
	public int size() {
		return liste.size();
	}

	@Override
	public Iterator<Player> iterator() {
		// TODO Auto-generated method stub
		return liste.iterator();
	}
	/*public class ConcurrentArrayListIterator<Game.Player> implements Iterator<Game.Player> {

		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Game.Player next() {
			// TODO Auto-generated method stub
			return null;
		}
	
}*/
}