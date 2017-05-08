
/**
 * Provides an easy way to group any two object types together.
 * @author Blake
 * @param <T> - The first type of data to store.
 * @param <U> - The second type of data to store.
 */
public class Pair<T, U> {
	
	public T x;
	public U y;
	
	/**
	 * Constructor for this Pair.
	 * @param x - The first object to store.
	 * @param y - The second object to store.
	 */
	public Pair(T x, U y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Converts this object into a String.
	 * @return This object as a String in the format "Pair(x, y)".
	 */
	public String toString() {
		return String.format("Pair(%s, %s)", x, y);
	}
}