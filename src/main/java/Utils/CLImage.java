package Utils;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CLImage {
    private final BufferedImage origImg;
    private BufferedImage img;
    private int lastX;
    private int lastY;

    private static BufferedImage rescaleImg(BufferedImage img, int width, int height) {
        int newWidth = img.getWidth();
        int newHeight = img.getHeight();
        Double multiplier = null;
        if (img.getWidth() > width || img.getHeight() > height) {
            if (width / img.getWidth() < height / img.getHeight()) {
                multiplier = (double) width / img.getWidth();
            } else {
                multiplier = (double) height / img.getHeight();
            }
            if (multiplier - 0.04 > 0) {
                multiplier -= 0.04;
            }
            newWidth = (int) (newWidth * multiplier);
            newHeight = (int) (newHeight * multiplier);
        } else if (img.getWidth() < width || img.getHeight() < height) {
            if (width / img.getWidth() < height / img.getHeight()) {
                multiplier = (double) width / img.getWidth();
            } else {
                multiplier = (double) height / img.getHeight();
            }
            if (multiplier - 0.1 > 0) {
                multiplier -= 0.1;
            }
            newWidth = (int) (newWidth * multiplier);
            newHeight = (int) (newHeight * multiplier);
        }

        BufferedImage out = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D buf = out.createGraphics();
        buf.drawImage(img, 0, 0, newWidth, newHeight, null);
        buf.dispose();
        return out;
    }

    CLImage(String path, int x, int y) throws IOException {
        origImg = ImageIO.read(new File(path));
        img = rescaleImg(origImg, x, y);
        lastX = x;
        lastY = y;
    }

    CLImage(String path) throws IOException {
        origImg = ImageIO.read(new File(path));
        img = origImg;
    }

    public void print(int x, int y) {
        if (x != lastX && y != lastY) {
            img = rescaleImg(origImg, x, y);
            lastX = x;
            lastY = y;
        }
        print();
    }

    public void print() {
        int width = img.getWidth();
        int height = img.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                System.out.print(Ansi.colorize(" ", Attribute.BACK_COLOR(pixel)));
                System.out.print(Ansi.colorize(" ", Attribute.BACK_COLOR(pixel)));
            }
            System.out.println();
        }
    }

    public String toString(int x, int y){
        if (x != lastX && y != lastY) {
            lastX = x;
            lastY = y;
            img = rescaleImg(origImg,x,y);
        }
        return toString(img);
    }

    public String toString(){
        return toString(origImg);
    }

    public String toString(boolean getOrigImage) {
        if (getOrigImage) {
            StringBuilder imgStr = new StringBuilder();

            int width = origImg.getWidth();
            int height = origImg.getHeight();

            imgStr.append("<request> <origImg> ");

            for (int yi = 0; yi < height; yi++) {
                if (yi!=0) imgStr.append("<request> ");
                for (int xi = 0; xi < width; xi++) {
                    imgStr.append(origImg.getRGB(xi, yi)).append(" ");
                }
            }

            imgStr.append("<request> </img>");

            return imgStr.toString();
        } else {
            return toString();
        }
    }

    private static String toString(BufferedImage source) {
        StringBuilder imgStr = new StringBuilder();

        int width = source.getWidth();
        int height = source.getHeight();

        for (int y = 0; y < height; y++) {
            if (y != 0) imgStr.append("<request> ");
            for (int x = 0; x < width; x++) {
                int pixel = source.getRGB(x, y);
                Color color = new Color(pixel, true);
                Attribute rgb = Attribute.BACK_COLOR(color.getRed(),color.getGreen(),color.getBlue());
                imgStr.append(Ansi.colorize("   ", rgb));
            }
        }

        return imgStr.toString();
    }
}
