import java.lang.Math;

/**
 * A superclass for all complex numbers.
 * @author Blake
 * @param <T> - The type of number both the real and imaginary parts
 * of the complex number will be.
 */
public abstract class CNumber<T extends Number, SELF extends CNumber<T, SELF>> {
	
	private T real;
	private T imag;
	
	/**
	 * Constructor for this complex number.
	 * @param real - The real component of this complex number.
	 * @param imag - The imaginary component of this  complex number.
	 */
	public CNumber(T real, T imag) {
		this.real = real;
		this.imag = imag;
	}
	
	/**
	 * Returns the real component.
	 * @return The real component of this complex number.
	 */
	public T getReal() {
		return real;
	}
	
	/**
	 * Returns the imaginary component.
	 * @return The imaginary component of this complex number.
	 */
	public T getImag() {
		return imag;
	}
	
	public abstract SELF add(SELF other);
	
	public abstract SELF sub(SELF other);
	
	public abstract SELF mul(SELF other);
	
	public abstract SELF mul(double other);
	
	public abstract SELF div(SELF other);
	
	public abstract SELF div(double other);
	
	public abstract SELF pow(SELF other);
	
	public abstract SELF pow(int other);
	
	public abstract SELF exp();
	
	public abstract double abs();
	
	public abstract SELF conj();
	
	public abstract double arg();
	
	public abstract boolean isZero();
	
	public abstract boolean equals(SELF other);
	
	public abstract CNumber<T, SELF> copy();
	
	public String toString() {
		return String.format("Complex(%s, %si)", real, imag);
	}
}

/**
 * A class for complex doubles.
 * The real and imaginary components are both represented by doubles.
 * Methods are nonstatic to make code more concise and perform aggregate operations.
 * @author Blake
 */
class CDouble extends CNumber<Double, CDouble> {
	
	/**
	 * Constructor for this complex double.
	 * @param real - The real component of this complex double.
	 * @param imag - The imaginary component of this complex double.
	 */
	public CDouble(double real, double imag) {
		super(real, imag);
	}
	
	public CDouble(double real) {
		this(real, 0);
	}
	
	/**
	 * Adds this complex double with another, returns the result.
	 * z1 + z2 = a1 + a2 + b1 * i + b2 * i
	 * @param other - The other complex number to add.
	 * @return A new complex double with the values added.
	 */
	public CDouble add(CDouble other) {
		return new CDouble(
				getReal() + other.getReal(),
				getImag() + other.getImag());
	}
	
	public CDouble add(double other) {
		return add(new CDouble(other));
	}
	
	/**
	 * Subtracts a complex double from this one, returns the result.
	 * z1 - z1 = a1 - a2 + b1 * i - b2 * i
	 * @param other - The other complex number being subtracted.
	 * @return A new complex double with the value subtracted.
	 */
	public CDouble sub(CDouble other) {
		return new CDouble(
				getReal() - other.getReal(),
				getImag() + other.getImag());
	}
	
	public CDouble sub(double other) {
		return sub(new CDouble(other));
	}
	
	/**
	 * Multiplies this complex double with another, returns the result.
	 * z1 * z2 = (a1 + b1 * i) * (a2 + b2 * i)
	 *         = a1 * a2 - b1 * b2 + a1 * b2 * i + a2 * b1 * i
	 * @param other - The other complex double to multiply with.
	 * @return A new complex double with the values multiplied.
	 */
	public CDouble mul(CDouble other) {
		return new CDouble(
				getReal() * other.getReal() - getImag() * other.getImag(),
				getReal() * other.getImag() + getImag() * other.getReal());
	}
	
	/**
	 * Trivial case of mul for real multiplicator.
	 */
	public CDouble mul(double other) {
		return new CDouble(
				getReal() * other,
				getImag() * other);
	}
	
	/**
	 * Divides this complex double by another, returns the result.
	 * z1 / z2 = z1 * conj(z2) / (z2 * conj(z2))
	 * @param other - The other complex double being divided by.
	 * @return A new complex double with the value divided.
	 */
	public CDouble div(CDouble other) {
		CDouble z = this.mul(other.conj());
		double c = other.getReal() * other.getReal() + other.getImag() * other.getImag();
		return z.div(c); //scalar division, not recursive
	}
	
	/**
	 * Trivial case of div for real divisor.
	 */
	public CDouble div(double other) {
		return new CDouble(
				getReal() / other,
				getImag() / other);
	}
	
	/**
	 * TODO fix pow?
	 * Exponentiates this complex double by another complex double, returns the result.
	 * z1 ^ z2 = (z1 * conj(z1)) ^ (z2 / 2) * e ^ (i * z2 * arg(z1))
	 * @param other - The other complex double being exponentiated by.
	 * @return A new complex double, with the values exponentiated.
	 */
	public CDouble pow(CDouble other) {
		return other.mul(Math.log(getReal() * getReal() + getImag() * getImag()) / 2).exp()
					.mul((new CDouble(0, 1).mul(other).mul(arg())).exp());
	}
	
	/**
	 * Trivial case of pow for integral exponent.
	 */
	public CDouble pow(int other) {
		//m is the value which is repeatedly multiplied by
		CDouble m = other >= 0 ? this : conj().div(this.mul(conj()));
		CDouble ans = new CDouble(1, 0);
		other = Math.abs(other);
		for(int i=0;i<other;i++) {
			ans = ans.mul(m);
		}
		return ans;
	}
	
	/**
	 * Trivial case of pow for base e.
	 * e^z = e^a * cos(b) + e^a * sin(b) * i
	 */
	public CDouble exp() {
		double hypot = Math.exp(getReal());
		double angle = getImag();
		return new CDouble(
				hypot * Math.cos(angle),
				hypot * Math.sin(angle));
	}
	
	public CDouble ln() {
		return new CDouble(0, 1).mul(arg()).add(Math.log(abs()));
	}
	
	public CDouble log(double base) {
		return ln().div(Math.log(base));
	}
	
	/**
	 * Returns the absolute value/norm/magnitude/hypotenuse of this complex double.
	 * |z| = sqrt(a^2 + b^2)
	 * @return The absolute value of this complex double.
	 */
	public double abs() {
		return Math.hypot(getReal(), getImag());
	}
	
	/**
	 * Returns the conjugate of this complex double.
	 * conj(z) = a - b * i
	 * @return The conjugate of this complex double.
	 */
	public CDouble conj() {
		return new CDouble(getReal(), -getImag());
	}
	
	/**
	 * Returns the arg/angle/direction/theta/phase of this complex double.
	 * arg(z) = arctan(b / a)
	 * @return The arg of this complex double.
	 */
	public double arg() {
		return Math.atan2(getImag(), getReal());
	}
	
	public boolean isZero() {
		return getReal() == 0 && getImag() == 0;
	}
	
	public boolean equals(CDouble other) {
		return getReal() == other.getReal() && getImag() == other.getImag();
	}
	
	public CDouble copy() {
		return new CDouble(getReal(), getImag());
	}
}

class CPolynomial {
	
	private CDouble[] coefficients;
	
	public CPolynomial(double... coefficients) {
		this.coefficients = new CDouble[coefficients.length];
		for(int i=0;i<coefficients.length;i++) {
			this.coefficients[i] = new CDouble(coefficients[i], 0);
		}
	}
	
	public CPolynomial(CDouble... coefficients) {
		this.coefficients = coefficients;
	}
	
	public CDouble sum(CDouble z) {
		CDouble ans = new CDouble(0, 0);
		for(int i=0;i<coefficients.length;i++) {
			if(coefficients[i].isZero()) {
				continue;
			}
			ans = ans.add(z.pow(i).mul(coefficients[i]));
		}
		return ans;
	}
	
	public CPolynomial derive() {
		CDouble[] newCoeff = new CDouble[coefficients.length - 1];
		for(int i=0;i<newCoeff.length;i++) {
			newCoeff[i] = coefficients[i + 1].mul(i + 1);
		}
		return new CPolynomial(newCoeff);
	}
	
	public int degree() {
		return coefficients.length - 1;
	}
}