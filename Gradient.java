import java.util.Arrays;
import java.util.function.Function;

/**
 * A class to provide a mapping from a number of iterations
 * to a 32 bit RGB int. Uses mapping instead of computation
 * to save on runtime.
 * @author Blake
 */
public class Gradient {
	
	private int[] rgbValues;
	
	/**
	 * Constructor which takes the colors the Gradient outputs.
	 * @param args - All the values (sequentially) the Gradient can produce.
	 */
	public Gradient(int... args) {
		rgbValues = args;
	}
	
	public Gradient(ColorSystem... colors) {
		rgbValues = new int[colors.length];
		for(int i=0;i<colors.length;i++) {
			rgbValues[i] = colors[i].toRgbInt();
		}
	}
	
	/**
	 * Returns the appropriate color based on given iterations.
	 * @param i - The number of iterations or the index of the color to return.
	 */
	public int get(int i) {
		return rgbValues[i % rgbValues.length];
	}
	
	public int getSize() {
		return rgbValues.length;
	}
	
	/**
	 * Represents the Gradient as a String.
	 * @return The length of the array and the array.
	 */
	public String toString() {
		String s = "Gradient " + rgbValues.length + "[\n";
		for(int i:rgbValues) {
			s += new RGB(i).toString() + "\n";
		}
		s += "]";
		return s;
	}
}

class SimpleLinearGradient extends Gradient {
	
	public SimpleLinearGradient(int maxColors, RGB... controlPoints) {
		super(makeColors(maxColors, controlPoints));
	}
	
	public SimpleLinearGradient(int maxColors, HSB... controlPoints) {
		super(makeColors(maxColors, controlPoints));
	}

	public static int[] makeColors(int maxColors, RGB... controlPoints) {
		int[] colors = new int[maxColors];
		double stepSize = (double) controlPoints.length / maxColors;
		double interval = 1 / stepSize;
		for(int i=0;i<maxColors;i++) {
			colors[i] = controlPoints[(int) (i * stepSize)].interpolate(
					RGB.LINEAR_INTERPOLATOR,
					controlPoints[((int) (i * stepSize) + 1) % controlPoints.length],
					1 - i % interval / interval,
					i % interval / interval).toRgbInt();
		}
		return colors;
	}
	
	public static int[] makeColors(int maxColors, HSB... controlPoints) {
		int[] colors = new int[maxColors];
		double stepSize = (double) controlPoints.length / maxColors;
		double interval = 1 / stepSize;
		for(int i=0;i<maxColors;i++) {
			colors[i] = controlPoints[(int) (i * stepSize)].interpolate(
					HSB.LINEAR_INTERPOLATOR,
					controlPoints[((int) (i * stepSize) + 1) % controlPoints.length],
					1 - i % interval / interval,
					i % interval / interval).toRgbInt();
		}
		return colors;
	}
}


/**
 * A class to easily create a rainbow gradient.
 * @author Blake
 */
class DefaultGradient extends Gradient {
	
	/**
	 * Constructor for the default gradient. Produces a given number of colors out
	 * of the rainbow.
	 * @param maxColors - The number of colors to generate.
	 */
	public DefaultGradient(int maxColors) {
		super(make(maxColors));
	}
	
	/**
	 * A private method to statically generate the Array needed to
	 * pass to the superconstructor.
	 * @param maxColors - The number of colors to generate.
	 * @return The array of colors to be mapped.
	 */
	private static int[] make(int maxColors) {
		int[] rgbValues = new int[maxColors];
		for(int i=0;i<maxColors;i++) {
			rgbValues[i] = HSB.toRgbInt((float) i / maxColors, 1, 1);
		}
		return rgbValues;
	}
}