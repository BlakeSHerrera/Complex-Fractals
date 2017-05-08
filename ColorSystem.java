import java.awt.Color;

public abstract class ColorSystem<SELF extends ColorSystem<SELF, T>, T extends Number> {
	
	public abstract int toRgbInt();
	
	public abstract ColorSystem<SELF, T> interpolate(Interpolator<T> interpolator, SELF other, double percent1, double percent2);
	
	public abstract String toString();
}

interface Interpolator<T extends Number> {
	
	public T interpolate(T first, T second, double percent1, double percent2);
}

/**
 * A helper class to organize and handle RGB values.
 * @author Blake
 */
class RGB extends ColorSystem<RGB, Integer> {
	
	public static final Interpolator<Integer> LINEAR_INTERPOLATOR =
			(a, b, p1, p2) -> (int) Math.round(a * p1 + b * p2);
	
	public int r;
	public int g;
	public int b;
	
	/**
	 * Constructor with one arg for grayscale value.
	 * @param gray - The value (from 0 to 255) of the gray being used.
	 */
	public RGB(int rgb) {
		this((rgb >> 0) % (1 << 8),
			 (rgb >> 8) % (1 << 8),
			 (rgb >> 16) % (1 << 8));
	}
	
	/**
	 * Constructor with three params for RGB channels.
	 * @param r - The red channel (0 to 255).
	 * @param g - The green channel (0 to 255).
	 * @param b - The blue channel (0 to 255).
	 */
	public RGB(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	/**
	 * Converts the RGB to an int.
	 * @return int - The RGB channels as a 32 bit int.
	 */
	public int toRgbInt() {
		return toRgbInt(r, g, b);
	}
	
	/**
	 * Statically converts the RGB to an int without an object.
	 * @param r - The red channel (0 to 255).
	 * @param g - The green channel (0 to 255).
	 * @param b - The blue channel (0 to 255).
	 * @return - The RGB channels represented as a 32 bit int.
	 */
	public static int toRgbInt(int r, int g, int b) {
		return r << 16 | g << 8 | b;
	}
	
	public RGB interpolate(Interpolator<Integer> interpolator, RGB other, double percent1, double percent2) {
		int r = interpolator.interpolate(this.r, other.r, percent1, percent2);
		int g = interpolator.interpolate(this.g, other.g, percent1, percent2);
		int b = interpolator.interpolate(this.b, other.b, percent1, percent2);
		return new RGB(r, g, b);
	}
	
	/**
	 * Represents the RGB Object as a String.
	 * @return - The RGB Object as a String in format "RGB(r, g, b)"
	 */
	public String toString() {
		return String.format("RGB(%d, %d, %d)", r, g, b);
	}
}

/**
 * A helper class to organize and handle HSB values.
 * @author Blake
 */
class HSB extends ColorSystem<HSB, Float> {
	
	public static final Interpolator<Float> LINEAR_INTERPOLATOR =
			(a, b, p1, p2) -> (float) (a * p1 + b * p2);
	
	public float h;
	public float s;
	public float b;
	
	/**
	 * Constructor for just a hue, sets s and b to 1.
	 * @param h - The hue channel (0 to 1).
	 */
	public HSB(float h) {
		this(h, 1, 1);
	}
	
	/**
	 * Constructor for the hue, saturation, and brightness.
	 * @param h - The hue channel (0 to 1).
	 * @param s - The saturation channel (0 to 1).
	 * @param b - The brightness channel (0 to 1).
	 */
	public HSB(float h, float s, float b) {
		this.h = h;
		this.s = s;
		this.b = b;
	}
	
	/**
	 * Converts the HSB color into an RGB int.
	 * @return the HSB color represented as a 32 bit RGB int.
	 */
	public int toRgbInt() {
		return toRgbInt(h, s, b);
	}
	
	/**
	 * Statically converts the given HSB values into an RGB int.
	 * @param h - The hue channel (0 to 1).
	 * @param s - The saturation channel (0 to 1).
	 * @param b - The brightness channel (0 to 1).
	 * @return The given HSB color as a 32 bit RGB int.
	 */
	public static int toRgbInt(float h, float s, float b) {
		return Color.HSBtoRGB(h, s, b);
	}
	
	public static HSB fromRgb(int r, int g, int b) {
		float[] v = Color.RGBtoHSB(r, g, b, null);
		return new HSB(v[0], v[1], v[2]);
	}
	
	public HSB interpolate(Interpolator<Float> interpolator, HSB other, double percent1, double percent2) {
		float hVal = other.h < h ? other.h + 1 : other.h;
		float h = interpolator.interpolate(this.h, hVal, percent1, percent2) % 1;
		float s = interpolator.interpolate(this.s, other.s, percent1, percent2);
		float b = interpolator.interpolate(this.b, other.b, percent1, percent2);
		return new HSB(h, s, b);
	}
	
	/**
	 * Represents the HSB Object as a String.
	 * @return The HSB Object as a String in the format "HSB(h, s, b)".
	 * h, s, and b all have 2 digits after the decimal.
	 */
	public String toString() {
		return String.format("HSB(%.2f, %.2f, %.2f)", h, s, b);
	}
}
