import java.util.function.Predicate;

/**
 * A class for creating fractal objects.
 * Includes fields for the function, bailout, and setting start values.
 * @author Blake
 * @param <T> - Must be a type of complex number.
 */
public abstract class Fractal <T extends CNumber> {
	
	private T z0;
	
	/**
	 * Constructor for this Fractal.
	 * @param func - An interface which takes 2 complex numbers and returns one.
	 * It should accept z and c, and return the new value of z.
	 * @param bailout - An interface which takes a complex number (z) and returns
	 * true if the bailout condition has been reached. Otherwise returns false.
	 * @param startup - An interface which accepts two doubles and returns a pair
	 * of complex numbers. It should accept the adjusted and and y coordinates of
	 * the pixel and return a pair which represents the starting values for
	 * z and c respectively.
	 */
	public Fractal(
			T z0) {
		this.z0 = z0;
	}
	
	public abstract T func(T z, double x, double y);
	
	public abstract boolean bailout(T z, double x, double y);
	
	public final T getZ0() {
		return z0;
	}
	
	public final void setZ0(T z0) {
		this.z0 = z0;
	}
	
	/**
	 * Initializes z and c according to the return of startup.apply(x, y).
	 * Iterates through the function z = func.apply(z, c) until the bailout
	 * condition bailout.test(z) returns a true.
	 * @param x - The adjusted x coordinate of the pixel.
	 * @param y - The adjusted y coordinate of the pixel.
	 * @param maxIterations - The maximum number of iterations before
	 * the method returns a -1.
	 * @return -1 if the maximum number of iterations has been reached.
	 * Otherwise, returns the number of iterations before the bailout
	 * condition was triggered.
	 */
	public final int iterate(double x, double y, int maxIterations) {
		T z = z0;
		for(int i=0;i<maxIterations;i++) {
			if(bailout(z, x, y)) {
				return i;
			}
			z = func(z, x, y);
		}
		return -1;
	}
}

//TODO fix pow? generalization not working
class Multibrot extends Fractal<CDouble> {
	
	private CDouble exponent;
	private double bailoutValue;
	
	public Multibrot(CDouble exponent) {
		super(new CDouble(0, 0));
		this.exponent = exponent;
		bailoutValue = exponent.abs();
	}
	
	@Override
	public CDouble func(CDouble z, double x, double y) {
		return z.pow(exponent).add(new CDouble(x, y));
	}
	
	@Override
	public boolean bailout(CDouble z, double x, double y) {
		return z.abs() >= bailoutValue;
	}
}

class Integerbrot extends Multibrot {
	
	private int exponent;
	
	public Integerbrot(int exponent) {
		super(new CDouble(exponent, 0));
		this.exponent = exponent;
	}
	
	@Override
	public CDouble func(CDouble z, double x, double y) {
		return z.pow(exponent).add(new CDouble(x, y));
	}
}

class Mandelbrot extends Integerbrot {
	
	public Mandelbrot() {
		super(2);
	}
}

/**
 * A class to easily create Julia set fractals.
 * @author Blake
 */
class Julia extends Fractal<CDouble> {
	
	private CDouble c;
	
	/**
	 * Constructor for the Julia fractal.
	 * @param real - The real part of c to initialize.
	 * @param imag - the imaginary part of c to initialize.
	 */
	public Julia(double real, double imag) {
		super(new CDouble(0, 0));
		c = new CDouble(real, imag);
	}
	
	@Override
	public CDouble func(CDouble z, double x, double y) {
		return z.mul(z).add(c);
	}
	
	@Override
	public boolean bailout(CDouble z, double x, double y) {
		Runner.log(z.abs());
		return z.abs() >= 2;
	}
}

class Newton extends PolynomialFractal {
	
	private CPolynomial derivative;
	
	public Newton(CPolynomial polynomial) {
		super(polynomial);
		this.derivative = polynomial.derive();
	}
	
	@Override
	public CDouble func(CDouble z, double x, double y) {
		return super.func(z, x, y).div(derivative.sum(z)).add(z);
	}
}

class PolynomialFractal extends Fractal<CDouble> {
	
	private CPolynomial polynomial;
	
	public PolynomialFractal(CPolynomial polynomial) {
		super(new CDouble(0, 0));
		this.polynomial = polynomial;
	}
	
	public CDouble func(CDouble z, double x, double y) {
		return polynomial.sum(z);
	}
	
	public boolean bailout(CDouble z, double x, double y) {
		return z.abs() >= polynomial.degree();
	}
}

@SuppressWarnings("serial")
class InvalidFractalException extends Exception {
	
	public InvalidFractalException(String message) {
		super(message);
	}
}