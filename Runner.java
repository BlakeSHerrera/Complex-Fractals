import static java.lang.System.out;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/*
 * TODO list:
 * implement a BigComplexDecimal
 * find cool gradients
 * find cool julia set values
 * find cool equations and sets
 */

/**
 * Main class for the fractal application.
 * @author Blake
 */
public class Runner {
	
	private static boolean logOutputEnabled = true;
	private static String lastMessage = null;
	private static int lastMessageCount = 0;
	private static int imageCount = 0;
	
	private String path;
	private Pair<Integer, Integer> dimensions;
	private Pair<Double, Double> center;
	private Pair<Double, Double> scale;
	private int maxIterations;
	private double zoomLevel;
	
	private Gradient gradient;
	private Fractal fractal;
	
	private JFrame frame;
	private JPanel panel;
	private JLabel label;
	private ImageIcon imageIcon;
	private BufferedImage image;
	private JMenuBar menuBar;
	
	private boolean isLocked;
	private boolean shouldCreateFractal;
	private final boolean freeExplore = true;
	
	/**
	 * Constructor for the main class. Has different values for the dimensions of
	 * the screen and the center coordinates and scale, etc.
	 * @param width - The width of the image in pixels.
	 * @param height - The height of the image in pixels.
	 * @param centerX - The coordinates of the center x.
	 * @param centerY - The coordinates of the center y.
	 * @param scaleX - The scale of x from the left to right of the screen.
	 * @param scaleY - The scale of y from the bottom to the top of the screen.
	 * @param gradient - The gradient being used to color the image.
	 * @param maxIterations - The number of iterations before bailout.
	 * @param fractal - The fractal being generated.
	 */
	public Runner(
			String path,
			int width, int height,
			double centerX, double centerY,
			double scaleX, double scaleY,
			int maxIterations,
			double zoomLevel,
			Gradient gradient,
			Fractal<CDouble> fractal) {
		this.path = path;
		dimensions = new Pair<>(width, height);
		center = new Pair<>(centerX, centerY);
		scale = new Pair<>(scaleX, scaleY);
		this.maxIterations = maxIterations;
		this.zoomLevel = zoomLevel;
		
		this.gradient = gradient;
		this.fractal = fractal;
		
		isLocked = false;
		shouldCreateFractal = false;
	}
	
	/**
	 * Call this to physically create the window and fractal first.
	 * Runner objects can have the variables stored for later without
	 * actually drawing the fractal itself.
	 */
	public void initialize() {
		image = new BufferedImage(dimensions.x, dimensions.y, BufferedImage.TYPE_INT_RGB);
		frame = new JFrame("Fractals");
		frame.setSize(dimensions.x, dimensions.y);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		panel = new JPanel();
		panel.setLayout(new FlowLayout());
		imageIcon = new ImageIcon(image);
		label = new JLabel(imageIcon);
		panel.add(label);
		frame.add(panel);
		
		menuBar = new JMenuBar();
		
		JMenu file = new JMenu("File");
		
		JMenuItem fileSave = new JMenuItem("Save");
		fileSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveImage();
			}
		});
		
		file.add(fileSave);
		
		menuBar.add(file);
		
		frame.setJMenuBar(menuBar);
		
		frame.pack();
		
		panel.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(isLocked) {
					logf("Locked!");
					return;
				}
				double x = xCoord(e.getX());
				double y = yCoord(e.getY());
				setCenter(x, y);
				if(e.isMetaDown()) {
					setScale(scale.x * zoomLevel, scale.y * zoomLevel);
				} else {
					setScale(scale.x / zoomLevel, scale.y / zoomLevel);
				}
				shouldCreateFractal = true;
				synchronized(fractal) {
					fractal.notify();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) { }

			@Override
			public void mouseReleased(MouseEvent e) { }

			@Override
			public void mouseEntered(MouseEvent e) { }

			@Override
			public void mouseExited(MouseEvent e) { }
			
		});
		
		createFractal();
		while(freeExplore) {
			try {
				synchronized(fractal) {
					fractal.wait();
				}
			} catch(InterruptedException e) {
				
			}
			if(shouldCreateFractal) {
				shouldCreateFractal = false;
				createFractal();
			}
		}
	}
	
	/**
	 * The main method. Starts the program.
	 * @param args - No use.
	 */
	public static void main(String[] args) throws InvalidFractalException {
		String path = "C:/Users/Blake/Pictures/mandelbrot";
		int width = 680;
		int height = 680;
		double centerX = 0;
		double centerY = 0;
		double scaleX = 4;
		double scaleY = 4;
		int maxColors = 64;
		int maxIterations = 256;
		double zoomLevel = 4;
		
		for(int i=0;i<args.length;i++) {
			switch(args[i]) {
			case "-path":
				path = args[++i];
			case "-width":
				width = Integer.parseInt(args[++i]);
				break;
			case "-height":
				height = Integer.parseInt(args[++i]);
				break;
			case "-dimensions":
				width = Integer.parseInt(args[++i]);
				height = Integer.parseInt(args[++i]);
				break;
			case "-center":
				centerX = Double.parseDouble(args[++i]);
				centerY = Double.parseDouble(args[++i]);
				break;
			case "-scale":
				scaleX = Double.parseDouble(args[++i]);
				scaleY = Double.parseDouble(args[++i]);
				break;
			case "-maxColors":
				maxColors = Integer.parseInt(args[++i]);
				break;
			case "-maxIterations":
			case "-maxIters":
				maxIterations = Integer.parseInt(args[++i]);
				break;
			default:
				logf("Unknown argument: %s", args[i]);
				return;
			}
		}
		
		DefaultGradient gradient = new DefaultGradient(maxColors);
		Fractal<CDouble> fractal = new Mandelbrot();
		
		HSB[] wikipediaColors = {
			HSB.fromRgb(0, 7, 100),
			HSB.fromRgb(32, 107, 203),
			HSB.fromRgb(237, 255, 255),
			HSB.fromRgb(255, 170, 0),
			HSB.fromRgb(0, 2, 0)
		};
		Gradient g = new SimpleLinearGradient(maxColors, wikipediaColors);
		log(g.toString());
		
		Runner test = new Runner(
			path,
			width, height,
			centerX, centerY,
			scaleX, scaleY,
			maxIterations,
			zoomLevel,
			g,
			fractal);
		test.initialize();
		
		
	}
	
	/**
	 * Updates the image display, but does not redraw the image.
	 */
	public void redraw() {
		frame.validate();
		frame.repaint();
	}
	
	/**
	 * Keeps a record of system messages. Prints it on screen and (later)
	 * writes to an output log. Displays a number indicating how many times
	 * the message has been displayed to save on space.
	 * @param message - The message to log.
	 */
	public static void log(String message) {
		if(!logOutputEnabled) {
			return;
		}
		if(message.equals(lastMessage)) {
			lastMessageCount += 1;
		} else {
			out.println("\t" + lastMessageCount);
			out.print(message);
			lastMessage = message;
			lastMessageCount = 1;
		}
	}
	
	/**
	 * Makes it easy to log numbers.
	 * @param n - The number to log.
	 */
	public static void log(Number n) {
		log(n.toString());
	}
	
	/**
	 * Like printf, but for the log.
	 * @param format - The String format of the message.
	 * @param args - The arguments for the format.
	 */
	public static void logf(String format, Object... args) {
		log(String.format(format, args));
	}
	
	/**
	 * Saves the image if possible. Logs a message based on whether or not
	 * the image was saved successfully and the error message if applicable.
	 * @param name
	 */
	public void saveImage() {
		String url = path + imageCount++ + ".png";
		File file = new File(url);
		try {
			ImageIO.write(image, "png", file);
			log("Saved image " + url + " successfully.");
		} catch(IOException e) {
			log(e.getMessage());
			log("File " + url + " could not be saved.");
		}
	}
	
	/**
	 * Draws the fractal onto the image.
	 */
	public void createFractal() {
		if(isLocked) {
			return;
		}
		isLocked = true;
		long start = System.currentTimeMillis();
		
		for(int y=0;y<dimensions.y;y++) {
			for(int x=0;x<dimensions.x;x++) {
				image.setRGB(x, y, RGB.toRgbInt(255, 255, 255));
			}
		}
		
		for(int y=0;y<dimensions.y;y++) {
			redraw();
			double newY = yCoord(y);
			for(int x=0;x<dimensions.x;x++) {
				double newX = xCoord(x);
				int i = fractal.iterate(newX, newY, maxIterations);
				if(i == -1) {
					image.setRGB(x, y, 0);
				} else {
					image.setRGB(x, y, gradient.get(i));
				}
			}
		}
		redraw();
		logf("Dimensions:(%d, %d) (total=%d)%n"
				+ "Center:(%f, %f)%n"
				+ "Scale:(%f, %f)%n"
				+ "Max Iterations: %d%n"
				+ "Time elapsed: %dms%n",
				dimensions.x, dimensions.y, dimensions.x * dimensions.y,
				center.x, center.y,
				scale.x, scale.y,
				maxIterations,
				System.currentTimeMillis() - start);
		isLocked = false;
		Toolkit.getDefaultToolkit().beep();
	}
	
	/**
	 * Converts the x pixel value to coordinates. Makes code easier to read.
	 * @param x - The untransformed x value of the pixel.
	 * @return The transformed x coordinate.
	 */
	public double xCoord(int x) {
		return scale.x / dimensions.x * (x - dimensions.x / 2) + center.x;
	}
	
	/**
	 * Converts the y pixel value to coordinates. Makes code easier to read.
	 * @param y - The untransformed y value of the pixel.
	 * @return The transformed y coordinate.
	 */
	public double yCoord(int y) {
		return scale.y / dimensions.y * (y - dimensions.y / 2) + center.y;
	}
	
	/**
	 * Sets the dimensions of the image.
	 * @param width - The new width of the image in pixels.
	 * @param height - The new height of the image in pixels.
	 */
	public void setDimensions(int width, int height) {
		dimensions.x = width;
		dimensions.y = height;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imageIcon.setImage(image);
	}
	
	/**
	 * Sets the center of the image.
	 * @param x - The new transformed x coordinate of the center.
	 * @param y - The new transformed y coordinate of the center.
	 */
	public void setCenter(double x, double y) {
		center.x = x;
		center.y = y;
	}
	
	/**
	 * Sets the scale of the image.
	 * @param x - The new transformed x scale of the image.
	 * @param y - The new transformed y scale of the image.
	 */
	public void setScale(double x, double y) {
		scale.x = x;
		scale.y = y;
	}
	
	/**
	 * Sets the maximum number of iterations.
	 * @param i - The new maximum number of iterations.
	 */
	public void setMaxIterations(int i) {
		maxIterations = i;
	}
	
	/**
	 * Sets the Gradient for the picture.
	 * @param g - The new Gradient to use.
	 */
	public void setGradient(Gradient g) {
		gradient = g;
	}
	
	/**
	 * Sets the Fractal for the picture.
	 * @param f - The new Fractal to use.
	 */
	public void setFractal(Fractal<CDouble> f) {
		fractal = f;
	}
	
	/**
	 * Returns the Pair of dimensions for the image.
	 * @return The Pair of dimensions for the image.
	 */
	public Pair<Integer, Integer> getDimensions() {
		return dimensions;
	}
	
	/**
	 * Returns the Pair of transformed coordinates for the center.
	 * @return The Pair of transformed coordinates for the center.
	 */
	public Pair<Double, Double> getCenter() {
		return center;
	}
	
	/**
	 * Returns the Pair of coordinates for the transformed scale.
	 * @return The Pair of coordinates for the transformed scale.
	 */
	public Pair<Double, Double> getScale() {
		return scale;
	}
	
	/**
	 * Returns the number of max iterations.
	 * @return The number of max iterations.
	 */
	public double getMaxIterations() {
		return maxIterations;
	}
	
	/**
	 * Returns the Gradient being used.
	 * @return The Gradient being used.
	 */
	public Gradient getGradient() {
		return gradient;
	}
	
	/**
	 * Returns the Fractal being used.
	 * @return The Fractal being used.
	 */
	public Fractal getFractal() {
		return fractal;
	}
}