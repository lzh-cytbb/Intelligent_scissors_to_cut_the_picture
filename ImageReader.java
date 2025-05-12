package dsaab_project;
import java.util.*;
import dsaab_project.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ImageReader {

    /**
     * Reads an image from the given file path and returns a 3D array of RGB values.
     * @param path Path to the image file
     * @return A 3D array where result[y][x][0] = R, [1] = G, [2] = B
     * @throws IOException if image reading fails
     */
    public int[][][] readImageToArray(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        int width = image.getWidth();
        int height = image.getHeight();

        int[][][] result = new int[height][width][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                result[y][x][0] = (rgb >> 16) & 0xFF; // Red
                result[y][x][1] = (rgb >> 8) & 0xFF;  // Green
                result[y][x][2] = rgb & 0xFF;         // Blue
            }
        }
        return result;
    }

    /**
     * Saves a portion of the image array to a file for debugging.
     * @param image the RGB image array
     * @param outputPath the output file path
     * @throws IOException if writing fails
     */
    public void saveImageArrayToFile(int[][][] image, String outputPath) throws IOException {
        try (FileWriter writer = new FileWriter(outputPath)) {
            int height = image.length;
            int width = image[0].length;
            for (int y = 0; y < Math.min(10, height); y++) {
                for (int x = 0; x < Math.min(10, width); x++) {
                    int r = image[y][x][0];
                    int g = image[y][x][1];
                    int b = image[y][x][2];
                    writer.write("(" + r + "," + g + "," + b + ") ");
                }
                writer.write("\n");
            }
        }
    }
    
    //返回图片三位数组
    public int[][][] return_array(String PATH) {
        String path = PATH;
        try {
          int[][][] image = readImageToArray(path);

            System.out.println("Image loaded successfully.");
            System.out.println("Dimensions: " + image.length + "x" + image[0].length);
            System.out.println("First pixel RGB: R=" + image[0][0][0] + ", G=" + image[0][0][1] + ", B=" + image[0][0][2]);

            System.out.println("Printing RGB array (up to 10x10 region):");
            for (int y = 0; y < Math.min(10, image.length); y++) {
                for (int x = 0; x < Math.min(10, image[0].length); x++) {
                    int r = image[y][x][0];
                    int g = image[y][x][1];
                    int b = image[y][x][2];
                    System.out.print("(" + r + "," + g + "," + b + ") ");
                }
                System.out.println();
            }
return image;
} catch (IOException e) {
            System.err.println("Failed to read image: " + e.getMessage());
        }
        return    return_array(path);
        ////saveImageArrayToFile(image, "C:\\Users\\lzh\\Desktop\\java_coding\\project\\RGB\\img_2_debug.txt");
           // System.out.println("Saved RGB debug array to RGB/img_1_debug.txt");

      
    }
}