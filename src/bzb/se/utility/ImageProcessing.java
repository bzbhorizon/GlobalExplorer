package bzb.se.utility;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public abstract class ImageProcessing {

	public static BufferedImage resize (BufferedImage fullScaleImage, int newWidth, int newHeight) {
		BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = scaledImage.createGraphics();
        double sx = (double) newWidth / (double) fullScaleImage.getWidth();
        double sy = (double) newHeight / (double) fullScaleImage.getHeight();
        AffineTransform at = AffineTransform.getScaleInstance(sx, sy);
        g2.setTransform(at);
        g2.drawImage(fullScaleImage, 0 , 0 , null);
		return scaledImage;
	}
	
}
