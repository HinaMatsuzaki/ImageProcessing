import java.io.*;
import java.util.TreeSet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.Scanner;
import java.util.Random;
import javax.swing.JFileChooser;
import java.io.File;
import java.util.Arrays;
 
public class Demo extends Component implements ActionListener {
    
    //************************************
    // List of the options(Original, Negative); correspond to the cases:
    //************************************

    String descs[] = {
        "Original", 
        "Select Region",
        "Negative",
        "Re-Scaling",
        "Shifting",
        "Noise and Shifting/Re-Scaling",
        "Addition",
        "Subtraction",
        "Multiplication",
        "Division",
        "BitwiseNOT",
        "AND",
        "OR",
        "XOR",
        "ROI-Based Operation",
        "Logarithmic Function",
        "Power-Law",
        "Random LUT",
        "Bit-Plane Slicing",
        "Histogram Equalization",
        "Averaging",
        "Weighted Averaging",
        "4-neighbour Laplacian",
        "8-neighbour Laplacian",
        "4-neighbour Laplacian Enhancement",
        "8-neighbour Laplacian Enhancement",
        "Roberts",
        "Sobel X",
        "Sobel Y",
        "Salt-and-Pepper Noise",
        "Min Filtering",
        "Max Filtering",
        "Midpoint Filtering",
        "Median Filtering",
        "Mean and Standard Deviation",
        "Simple Thresholding",
        "Automated Thresholding",
    };
 
    int opIndex;  // option index for 
    int lastOp;
    int prevOp;   // previous operation
    boolean region_selected = false;
    

    private BufferedImage bi, biFiltered, bi3, bi5, biFiltered_mask;   // the input image saved as bi;//
    int w, h;
     
    public Demo() {
        try {
            bi = ImageIO.read(new File("Baboon.bmp"));

            w = bi.getWidth(null);
            h = bi.getHeight(null);
            System.out.println(bi.getType());
            if (bi.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics big = bi2.getGraphics();
                big.drawImage(bi, 0, 0, null);
                biFiltered = bi = bi2;
            }
        } catch (IOException e) {      // deal with the situation that th image has problem;/
            System.out.println("Image could not be read");

            System.exit(1);
        }
    }

    public void storeImage (File file){
        try {
            bi3 = ImageIO.read(file);

            w = bi3.getWidth(null);
            h = bi3.getHeight(null);
            System.out.println(bi3.getType());
            if (bi3.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage bi4 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics big = bi4.getGraphics();
                big.drawImage(bi3, 0, 0, null);
                biFiltered = bi3 = bi4;
            }
        } catch (IOException e) {      // deal with the situation that th image has problem;/
            System.out.println("Image could not be read");

            System.exit(1);
        }
    }                      
 
    public Dimension getPreferredSize() {
        return new Dimension(w, h);
    }
 
    String[] getDescriptions() {
        return descs;
    }

    // Return the formats sorted alphabetically and in lower case
    public String[] getFormats() {
        String[] formats = {"bmp","gif","jpeg","jpg","png"};
        TreeSet<String> formatSet = new TreeSet<String>();
        for (String s : formats) {
            formatSet.add(s.toLowerCase());
        }

        return formatSet.toArray(new String[0]);
    }

    void setOpIndex(int i) {
        opIndex = i;
    }
 
    public void paint(Graphics g) { //  Repaint will call this function so the image will change.
        filterImage();      

        g.drawImage(biFiltered, 0, 0, null);

        if (region_selected == true){
            g.drawImage(biFiltered_mask, 0, 0, null);
        }
    }

    //************************************
    //  Convert the Buffered Image to Array
    //************************************
    private static int[][][] convertToArray(BufferedImage image){
      int width = image.getWidth();
      int height = image.getHeight();

      int[][][] result = new int[width][height][4];

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int p = image.getRGB(x,y);

            int a = (p>>24)&0xff;
            int r = (p>>16)&0xff;
            int g = (p>>8)&0xff;
            int b = p&0xff;

            result[x][y][0] = a;
            result[x][y][1] = r;
            result[x][y][2] = g;
            result[x][y][3] = b;
         }
      }

      return result;
    }

    //************************************
    //  Convert the  Array to BufferedImage
    //************************************
    public BufferedImage convertToBimage(int[][][] TmpArray){

        int width = TmpArray.length;
        int height = TmpArray[0].length;

        BufferedImage tmpimg=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int a = TmpArray[x][y][0];
                int r = TmpArray[x][y][1];
                int g = TmpArray[x][y][2];
                int b = TmpArray[x][y][3];
                
                //set RGB value
                int p = (a<<24) | (r<<16) | (g<<8) | b;
                tmpimg.setRGB(x, y, p);

            }
        }

        return tmpimg;
    }


    //************************************
    //  Example:  Image Negative
    //************************************
    public BufferedImage ImageNegative(BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);  //  Convert the image to array

        // Image Negative Operation:
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                
                ImageArray[x][y][1] = 255 - ImageArray[x][y][1];  //r
                ImageArray[x][y][2] = 255 - ImageArray[x][y][2];  //g
                ImageArray[x][y][3] = 255 - ImageArray[x][y][3];  //b
            }
        } 

        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }


    //************************************
    //  Your turn now:  Add more function below
    //************************************

    // re-scaling
    // factor is from 0 to 2
    // the larger the factor is, the brighter the image will be
    // when the factor = 0, the image is pure black
    public BufferedImage rescaling(BufferedImage image, float factor){
        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image); 

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                ImageArray[x][y][1] = Math.min(255, Math.round(factor * ImageArray[x][y][1]));
                ImageArray[x][y][2] = Math.min(255, Math.round(factor * ImageArray[x][y][2]));
                ImageArray[x][y][3] = Math.min(255, Math.round(factor * ImageArray[x][y][3]));
            }
        }

        return convertToBimage(ImageArray);
    }

    // shifting
    // factor is from -255 to 255
    // the larger the factor is, the brighter the image will be
    // when the factor = -255, the image is pure black
    // when the factor = 255, the image is pure white
    public BufferedImage shifting(BufferedImage image, int factor){
        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image);

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                ImageArray[x][y][1] = ImageArray[x][y][1] + factor;
                ImageArray[x][y][2] = ImageArray[x][y][2] + factor;
                ImageArray[x][y][3] = ImageArray[x][y][3] + factor;

                if (ImageArray[x][y][1] > 255){
                    ImageArray[x][y][1] = 255;
                }
                if (ImageArray[x][y][1] < 0){
                    ImageArray[x][y][1] = 0;
                }
                if (ImageArray[x][y][2] > 255){
                    ImageArray[x][y][2] = 255;
                }
                if (ImageArray[x][y][2] < 0){
                    ImageArray[x][y][2] = 0;
                }
                if (ImageArray[x][y][3] > 255){
                    ImageArray[x][y][3] = 255;
                }
                if (ImageArray[x][y][3] < 0){
                    ImageArray[x][y][3] = 0;
                }
            }
        }

        return convertToBimage(ImageArray);
    }

    // adding a random noise and then shifting and re-scaling
    // re-scaling factor is from 0 to 2 (float)
    // shifting factor is from -255 to 255 (int)
    // find the min and max in a new image to change the dynamic range
    public BufferedImage noise_shifting_rescaling(BufferedImage image, int shifting_factor, float rescaling_factor){
        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image);

        int red_min = Math.round(rescaling_factor * (ImageArray[0][0][1] + shifting_factor));
        int green_min = Math.round(rescaling_factor * (ImageArray[0][0][2] + shifting_factor));
        int blue_min = Math.round(rescaling_factor * (ImageArray[0][0][3] + shifting_factor));
        int red_max = red_min;
        int green_max = green_min;
        int blue_max = blue_min;

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){

                Random r = new Random();
                // the range of random noise is between -255 and 255
                // noise = r.nextInt(max + 1 - min) + min
                int noise = r.nextInt(255 + 255) -255;

                ImageArray[x][y][1] = Math.round(rescaling_factor * (ImageArray[x][y][1] + noise + shifting_factor));
                ImageArray[x][y][2] = Math.round(rescaling_factor * (ImageArray[x][y][2] + noise + shifting_factor));
                ImageArray[x][y][3] = Math.round(rescaling_factor * (ImageArray[x][y][3] + noise + shifting_factor));
                
                if (red_min > ImageArray[x][y][1]){
                    red_min = ImageArray[x][y][1];
                }
                if (green_min > ImageArray[x][y][2]){
                    green_min = ImageArray[x][y][2];
                }
                if (blue_min > ImageArray[x][y][3]){
                    blue_min = ImageArray[x][y][3];
                }
                if (red_max < ImageArray[x][y][1]){
                    red_max = ImageArray[x][y][1];
                }
                if (green_max < ImageArray[x][y][2]){
                    green_max = ImageArray[x][y][2];
                }
                if (blue_max < ImageArray[x][y][3]){
                    blue_max = ImageArray[x][y][3];
                }
            }
        }

        // chenge the dynamic range by using a general method
        // g(x, y) = 255 * ((f(x, y)-fmin)/(fmax-fmin))
        // if f(x, y) = fmin, f(x, y) = 0
        // if f(x, y) = fmax, f(x, y) = 255
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                ImageArray[x][y][1] = 255 * (ImageArray[x][y][1] - red_min)/(red_max - red_min);
                ImageArray[x][y][2] = 255 * (ImageArray[x][y][2] - green_min)/(green_max - green_min);
                ImageArray[x][y][3] = 255 * (ImageArray[x][y][3] - blue_min)/(blue_max - blue_min);
            }
        }

        return convertToBimage(ImageArray);
    }

    // add two images
    public BufferedImage add(BufferedImage image1, BufferedImage image2){
        int width1 = image1.getWidth();
        int height1 = image1.getHeight();
        int width2 = image2.getWidth();
        int height2 = image2.getHeight();

        int[][][] ImageArray1 = convertToArray(image1); 
        int[][][] ImageArray2 = convertToArray(image2); 

        for(int y = 0; y < Math.min(height1, height2); y++){
            for(int x = 0; x < Math.min(width1, width2); x++){
                ImageArray1[x][y][1] = Math.min(255, ImageArray1[x][y][1] + ImageArray2[x][y][1]);
                ImageArray1[x][y][2] = Math.min(255, ImageArray1[x][y][2] + ImageArray2[x][y][2]);
                ImageArray1[x][y][3] = Math.min(255, ImageArray1[x][y][3] + ImageArray2[x][y][3]);
            }
        }

        return convertToBimage(ImageArray1);
    }

    // subtract the second image from the first image
    public BufferedImage subtract(BufferedImage image1, BufferedImage image2){
        int width1 = image1.getWidth();
        int height1 = image1.getHeight();
        int width2 = image2.getWidth();
        int height2 = image2.getHeight();

        int[][][] ImageArray1 = convertToArray(image1); 
        int[][][] ImageArray2 = convertToArray(image2); 

        for(int y = 0; y < Math.min(height1, height2); y++){
            for(int x = 0; x < Math.min(width1, width2); x++){
                ImageArray1[x][y][1] = Math.max(0, ImageArray1[x][y][1] - ImageArray2[x][y][1]);
                ImageArray1[x][y][2] = Math.max(0, ImageArray1[x][y][2] - ImageArray2[x][y][2]);
                ImageArray1[x][y][3] = Math.max(0, ImageArray1[x][y][3] - ImageArray2[x][y][3]);
            }
        }

        return convertToBimage(ImageArray1);
    }

    // multiply the first image by the second image
    public BufferedImage multiply(BufferedImage image1, BufferedImage image2){
        int width1 = image1.getWidth();
        int height1 = image1.getHeight();
        int width2 = image2.getWidth();
        int height2 = image2.getHeight();

        int[][][] ImageArray1 = convertToArray(image1); 
        int[][][] ImageArray2 = convertToArray(image2); 

        for(int y=0; y<Math.min(height1, height2); y++){
            for(int x=0; x<Math.min(width1, width2); x++){
                ImageArray1[x][y][1] = ImageArray1[x][y][1]*ImageArray2[x][y][1]/255;
                ImageArray1[x][y][2] = ImageArray1[x][y][2]*ImageArray2[x][y][2]/255;
                ImageArray1[x][y][3] = ImageArray1[x][y][3]*ImageArray2[x][y][3]/255;
            }
        }

        return convertToBimage(ImageArray1);
    }

    // divide the first image by the second image
    public BufferedImage divide(BufferedImage image1, BufferedImage image2){
        int width1 = image1.getWidth();
        int height1 = image1.getHeight();
        int width2 = image2.getWidth();
        int height2 = image2.getHeight();

        int[][][] ImageArray1 = convertToArray(image1); 
        int[][][] ImageArray2 = convertToArray(image2); 

        for(int y = 0; y < Math.min(height1, height2); y++){
            for(int x = 0; x < Math.min(width1, width2); x++){
                // to avoid dividing by 0
                // if the divisor is 0, set the divisor to 255, or the largest possible value
                // becuase when the divisor is closer to 0, the output gets larger
                if (ImageArray2[x][y][1] == 0){
                    ImageArray2[x][y][1] = 255;
                }
                if (ImageArray2[x][y][2] == 0){
                    ImageArray2[x][y][2] = 255;
                }
                if (ImageArray2[x][y][3] == 0){
                    ImageArray2[x][y][3] = 255;
                }
                
                ImageArray1[x][y][1] = Math.abs(ImageArray1[x][y][1] / ImageArray2[x][y][1] * 255);
                ImageArray1[x][y][2] = Math.abs(ImageArray1[x][y][2] / ImageArray2[x][y][2] * 255);
                ImageArray1[x][y][3] = Math.abs(ImageArray1[x][y][3] / ImageArray2[x][y][3] * 255);

                if (ImageArray1[x][y][1] > 255){
                    ImageArray1[x][y][1] = 255;
                }
                if (ImageArray1[x][y][2] > 255){
                    ImageArray1[x][y][2] = 255;
                }
                if (ImageArray1[x][y][3] > 255){
                    ImageArray1[x][y][3] = 255;
                }
            }
        }

        return convertToBimage(ImageArray1);
    }

    // bitwiseNOT
    // 0 if the bit is 1
    // 1 if the bit is 0
    public BufferedImage bitwiseNOT(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image); 

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){

                // x&0xFF gives the lowest 8 bits from x
                // e.g. 00000101 -> 11111010
                ImageArray[x][y][1] = ~(ImageArray[x][y][1])&0xFF;
                ImageArray[x][y][2] = ~(ImageArray[x][y][2])&0xFF;
                ImageArray[x][y][3] = ~(ImageArray[x][y][3])&0xFF;
            }
        }

        return convertToBimage(ImageArray);
    }

    // bitwise AND on two images
    // 1 if both bits are 1
    // 0 otherwise
    public BufferedImage ANDop(BufferedImage image1, BufferedImage image2){
        int width1 = image1.getWidth();
        int height1 = image1.getHeight();
        int width2 = image2.getWidth();
        int height2 = image2.getHeight();

        int[][][] ImageArray1 = convertToArray(image1); 
        int[][][] ImageArray2 = convertToArray(image2); 

        for(int y = 0; y < Math.min(height1, height2); y++){
            for(int x = 0; x < Math.min(width1, width2); x++){  
                ImageArray1[x][y][1] = ImageArray1[x][y][1] & ImageArray2[x][y][1];
                ImageArray1[x][y][2] = ImageArray1[x][y][2] & ImageArray2[x][y][2];
                ImageArray1[x][y][3] = ImageArray1[x][y][3] & ImageArray2[x][y][3];
            }
        }

        return convertToBimage(ImageArray1);
    }

    // bitwise OR on two images
    // 1 if at least one of the bits is 1
    // 0 otherwise
    public BufferedImage ORop(BufferedImage image1, BufferedImage image2){
        int width1 = image1.getWidth();
        int height1 = image1.getHeight();
        int width2 = image2.getWidth();
        int height2 = image2.getHeight();

        int[][][] ImageArray1 = convertToArray(image1); 
        int[][][] ImageArray2 = convertToArray(image2); 

        for(int y = 0; y < Math.min(height1, height2); y++){
            for(int x = 0; x < Math.min(width1, width2); x++){
                
                ImageArray1[x][y][1] = ImageArray1[x][y][1] | ImageArray2[x][y][1];
                ImageArray1[x][y][2] = ImageArray1[x][y][2] | ImageArray2[x][y][2];
                ImageArray1[x][y][3] = ImageArray1[x][y][3] | ImageArray2[x][y][3];
            }
        }

        return convertToBimage(ImageArray1);
    }

    // bitwise XOR on two images
    // 1 if they have different bits(e.g. (0, 1), (1, 0))
    // 0 if both have the same bit (e.g. (0, 0), (1, 1))
    public BufferedImage XORop(BufferedImage image1, BufferedImage image2){
        int width1 = image1.getWidth();
        int height1 = image1.getHeight();
        int width2 = image2.getWidth();
        int height2 = image2.getHeight();

        int[][][] ImageArray1 = convertToArray(image1); 
        int[][][] ImageArray2 = convertToArray(image2); 

        for(int y = 0; y < Math.max(height1, height2); y++){
            for(int x = 0; x < Math.max(width1, width2); x++){
                
                ImageArray1[x][y][1] = ImageArray1[x][y][1] ^ ImageArray2[x][y][1];
                ImageArray1[x][y][2] = ImageArray1[x][y][2] ^ ImageArray2[x][y][2];
                ImageArray1[x][y][3] = ImageArray1[x][y][3] ^ ImageArray2[x][y][3];
            }
        }

        return convertToBimage(ImageArray1);
    }

    // ROI based operation
    // alpha of image1 depends on image2
    public BufferedImage ROI_based_operation(BufferedImage image1, BufferedImage image2){
        int width1 = image1.getWidth();
        int height1 = image1.getHeight();
        int width2 = image2.getWidth();
        int height2 = image2.getHeight();

        int[][][] ImageArray1 = convertToArray(image1); 
        int[][][] ImageArray2 = convertToArray(image2); 
        int alpha;

        for(int y = 0; y < Math.max(height1, height2); y++){
            for(int x = 0; x < Math.max(width1, width2); x++){
                alpha = (ImageArray2[x][y][1] + ImageArray2[x][y][2] + ImageArray2[x][y][3])/3;
                ImageArray1[x][y][0] = alpha;
            }
        }

        return convertToBimage(ImageArray1);
    }

    // logarithmic function
    // s = c log(1+k) = (log(1+k)*255)/log(256)
        // c = 255/log(256)
        // when k = 0, c log(1+0) = 0
        // when k = 255, c log(1+255) = 255
    // lower levels of input intensity have larger range of levels in output 
    public BufferedImage logarithmic_function(BufferedImage image){
        int[] LUT = new int[256]; 

        // generate a LUT of 256 levels for logarithmic function
        for(int k = 0; k <= 255; k++){
            LUT[k] = (int)(Math.log(1+k)*255/Math.log(256));
        }

        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image);

        // point transform with LUT of 256 levels
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int r = ImageArray[x][y][1];
                int g = ImageArray[x][y][2];
                int b = ImageArray[x][y][3];
                ImageArray[x][y][1] = LUT[r];
                ImageArray[x][y][2] = LUT[g];
                ImageArray[x][y][3] = LUT[b];
            }   
        }

        return convertToBimage(ImageArray);
    }

    // power-law
    // power-law factor p is from 0.01 to 25 (float)
    // when p = 0.01, the image is almost pure white (the object can be slightly seen)
    // when p = 25, the image is pure black
    
    // s = c k^p = 255^(1-p) k^p
        // c = 255/(255^p) = 255^(1-p)
        // when k = 0, c 0^p = 0
        // when k = 255, c 255^p = 255
    // if p is smaller, lower levels of input intensity have larger range of level in output image
    // if p is larger, higher levels of input intensity have larger range of level in output image
    public BufferedImage power_law(BufferedImage image, double p){
        int[] LUT = new int[256]; 
        // generate a LUT of 256 levels for power law p
        for(int k = 0; k <= 255; k++){
            LUT[k] = (int)(Math.pow(255,1-p)*Math.pow(k,p));
        }

        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image);

        // point transform with LUT of 256 levels
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int r = ImageArray[x][y][1]; 
                int g = ImageArray[x][y][2]; 
                int b = ImageArray[x][y][3]; 
                ImageArray[x][y][1] = LUT[r];
                ImageArray[x][y][2] = LUT[g];
                ImageArray[x][y][3] = LUT[b];
            }   
        }

        return convertToBimage(ImageArray);
    }

    // random look-up table
    public BufferedImage random_LUT(BufferedImage image){
        int[] LUT = new int[256]; 
        // generate a look-up table with random numbers
        for(int k = 0; k <= 255; k++){
            Random r = new Random();
            // random_num = r.nextInt(max - min) + min
            // the range of random numbers if between 0 and 255
            int random_num = r.nextInt(255-0)+0;    
            LUT[k] = random_num;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image);

        // point transform with LUT of 256 levels
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int r = ImageArray[x][y][1]; 
                int g = ImageArray[x][y][2]; 
                int b = ImageArray[x][y][3]; 
                ImageArray[x][y][1] = LUT[r]; 
                ImageArray[x][y][2] = LUT[g]; 
                ImageArray[x][y][3] = LUT[b]; 
            }   
        }

        return convertToBimage(ImageArray);
    }

    // bit plane slicing
    // bit k is from 0 to 7 (int)
    public BufferedImage bit_plane_slicing(BufferedImage image, int k){
        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image);

        // Find the bit plane k of an image
        // if x = 00111011 and x >> 2, x will be 00001110
        // >> dropped the last two values and added two 0 at the left 
        // 8-bit image
        // plane 0 contains the lowest order bit (the most right: 一番下の位) of all the pixels in the image
        // plane 7 contains the highest order bit (the most left: 一番上の位) of all the pixels in the image
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int r = ImageArray[x][y][1]; 
                int g = ImageArray[x][y][2]; 
                int b = ImageArray[x][y][3]; 
                ImageArray[x][y][1] = ((r>>k)&1)*255;
                ImageArray[x][y][2] = ((g>>k)&1)*255;
                ImageArray[x][y][3] = ((b>>k)&1)*255;

                if (ImageArray[x][y][1] > 255){
                    ImageArray[x][y][1] = 255;
                }
                if (ImageArray[x][y][1] < 0){
                    ImageArray[x][y][1] = 0;
                }
                if (ImageArray[x][y][2] > 255){
                    ImageArray[x][y][2] = 255;
                }
                if (ImageArray[x][y][2] < 0){
                    ImageArray[x][y][2] = 0;
                }
                if (ImageArray[x][y][3] > 255){
                    ImageArray[x][y][3] = 255;
                }
                if (ImageArray[x][y][3] < 0){
                    ImageArray[x][y][3] = 0;
                }
            }
        }

        return convertToBimage(ImageArray);
    }

    // construct histogram
    // example
        // gray level | 0    | 1    | 2    | 3    | 4    | 5    | 6    | 7    |
        // histogram  | 1    | 5    | 2    | 1    | 2    | 2    | 5    | 2    |
        // normalized | 0.05 | 0.25 | 0.10 | 0.05 | 0.10 | 0.10 | 0.25 | 0.10 |
        // cumulative | 0.05 | 0.30 | 0.40 | 0.45 | 0.55 | 0.65 | 0.90 | 1.0  |
        // (L-1)*     | 0.35 | 2.1  | 2.8  | 3.15 | 3.85 | 4.55 | 6.3  | 7.0  |
        // rounded    | 0    | 2    | 3    | 3    | 4    | 5    | 6    | 7    |
    
    public BufferedImage construct_histogram (BufferedImage image){
    	int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image);

    	int[] HistogramR = new int[256];
    	int[] HistogramG = new int[256];
    	int[] HistogramB = new int[256];
        int r, g, b;
        
        // initialisation
	    for(int k=0; k<256; k++){ 
			HistogramR[k] = 0;
			HistogramG[k] = 0;
			HistogramB[k] = 0;
		}

        //* histogram *//
        // count the number of pixels for every gray level
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				r = ImageArray[x][y][1]; //r
				g = ImageArray[x][y][2]; //g
				b = ImageArray[x][y][3]; //b
				HistogramR[r]++;
				HistogramG[g]++;
				HistogramB[b]++;
			}
		}

        //* normalized *//
        // divide the number of pixels by the total pixels (= height*width)
        float[] nHistogramR = new float[256];
        float[] nHistogramG = new float[256];
        float[] nHistogramB = new float[256];
        for(int k = 0; k < 256; k++){ 
            nHistogramR[k] = (float)HistogramR[k]/(float)(height*width); // r
            nHistogramG[k] = (float)HistogramG[k]/(float)(height*width); // g
            nHistogramB[k] = (float)HistogramB[k]/(float)(height*width); // b
        }

        // equalization
        float[] eHistogramR = new float[256];
        float[] eHistogramG = new float[256];
        float[] eHistogramB = new float[256];
        eHistogramR[0] = nHistogramR[0];
        eHistogramG[0] = nHistogramG[0];
        eHistogramB[0] = nHistogramB[0];
        
        //* cumulative *//
        for(int k = 1; k < 256; k++){ 
            eHistogramR[k] = eHistogramR[k-1]+nHistogramR[k]; // r
            eHistogramG[k] = eHistogramG[k-1]+nHistogramG[k]; // g
            eHistogramB[k] = eHistogramB[k-1]+nHistogramB[k]; // b
        }
        
        //* (L-1)*  *//
        //* rounded *//
        for(int k = 0; k < 256; k++){ 
            eHistogramR[k] = Math.round(eHistogramR[k]*(256-1));
            eHistogramG[k] = Math.round(eHistogramG[k]*(256-1));
            eHistogramB[k] = Math.round(eHistogramB[k]*(256-1));
        } 

        int r_original, g_original, b_original;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                r_original = ImageArray[x][y][1]; //r
                g_original = ImageArray[x][y][2]; //g
                b_original = ImageArray[x][y][3]; //b
                ImageArray[x][y][1] = (int)eHistogramR[r_original];
                ImageArray[x][y][2] = (int)eHistogramG[g_original];
                ImageArray[x][y][3] = (int)eHistogramB[b_original];
            }
        }

        return convertToBimage(ImageArray);    
	}

    // averaging
    public BufferedImage normal_average (BufferedImage image){ 
        int[][] mask = {{1, 1, 1},
                        {1, 1, 1},
                        {1, 1, 1}};
        // divide the sum by 9
        return apply_mask(image, mask, 9);
    }

    // weieghted averaging
    public BufferedImage weighted_average (BufferedImage image){ 
        int[][] mask = {{1, 2, 1},
                        {2, 4, 2},
                        {1, 2, 1}};
        // divide the sum by 16
        return apply_mask(image, mask, 16);
    }
    
    // 4-neighbour laplacian
    public BufferedImage four_neighbour_laplacian (BufferedImage image){ 
        int[][] mask = {{0, -1, 0},
                        {-1, 4, -1},
                        {0, -1, 0}};
        // divide the sum by 1 (= no change)
        return apply_mask(image, mask, 1);
    }

    // 8-neighbour laplacian
    public BufferedImage eight_neighbour_laplacian (BufferedImage image){ 
        int[][] mask = {{-1, -1, -1},
                        {-1, 8, -1},
                        {-1, -1, -1}};
        return apply_mask(image, mask, 1);
    }

    // 4-neighbour laplacian enhancement
    public BufferedImage four_neighbour_laplacian_enhancement (BufferedImage image){ 
        int[][] mask = {{0, -1, 0},
                        {-1, 5, -1},
                        {0, -1, 0}};
        return apply_mask(image, mask, 1);
    }

    // 8-neighbour laplacian enhancement
    public BufferedImage eight_neighbour_laplacian_enhancement (BufferedImage image){ 
        int[][] mask = {{-1, -1, -1},
                        {-1, 9, -1},
                        {-1, -1, -1}};
        return apply_mask(image, mask, 1);
    }

    // sobel X
    public BufferedImage sobel_x (BufferedImage image){ 
        int[][] mask = {{-1, 0, 1},
                        {-2, 0, 2},
                        {-1, 0, 1}};
        return apply_mask(image, mask, 1);
    }
    
    // sobel Y
    public BufferedImage sobel_y (BufferedImage image){ 
        int[][] mask = {{-1, -2, -1},
                        {0, 0, 0},
                        {1, 2, 1}};
        return apply_mask(image, mask, 1);
    }

    // roberts
    // with absolute value conversion
    public BufferedImage roberts (BufferedImage image){ 
        int[][] mask1 = {{0, 0, 0},
                        {0, 0, -1},
                        {0, 1, 0}};
        int[][] mask2 = {{0, 0, 0},
                        {0, -1, 0},
                        {0, 0, 1}};

        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image);
        int[][][] newImageArray = new int[width][height][4];
        
        
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                if (y == 0 || y == height-1 || x == 0 || x == width-1){
                    newImageArray[x][y][1] = ImageArray[x][y][1];
                    newImageArray[x][y][2] = ImageArray[x][y][2];
                    newImageArray[x][y][3] = ImageArray[x][y][3];

                }else{
                    newImageArray[x][y][1] = Math.abs(Math.round(ImageArray[x-1][y-1][1] * mask1[0][0] 
                                         + ImageArray[x-1][y][1] * mask1[0][1]
                                         + ImageArray[x-1][y+1][1] * mask1[0][2]
                                         + ImageArray[x][y-1][1] * mask1[1][0]
                                         + ImageArray[x][y][1] * mask1[1][1]
                                         + ImageArray[x][y+1][1] * mask1[1][2]
                                         + ImageArray[x+1][y-1][1] * mask1[2][0]
                                         + ImageArray[x+1][y][1] * mask1[2][1]
                                         + ImageArray[x+1][y+1][1]* mask1[2][2])) 

                    					 + Math.abs(Math.round(ImageArray[x-1][y-1][1] * mask2[0][0] 
                                         + ImageArray[x-1][y][1] * mask2[0][1]
                                         + ImageArray[x-1][y+1][1] * mask2[0][2]
                                         + ImageArray[x][y-1][1] * mask2[1][0]
                                         + ImageArray[x][y][1] * mask2[1][1]
                                         + ImageArray[x][y+1][1] * mask2[1][2]
                                         + ImageArray[x+1][y-1][1] * mask2[2][0]
                                         + ImageArray[x+1][y][1] * mask2[2][1]
                                         + ImageArray[x+1][y+1][1]* mask2[2][2]));

                    newImageArray[x][y][2] = Math.abs(Math.round(ImageArray[x-1][y-1][2] * mask1[0][0] 
                                         + ImageArray[x-1][y][2] * mask1[0][1]
                                         + ImageArray[x-1][y+1][2] * mask1[0][2]
                                         + ImageArray[x][y-1][2] * mask1[1][0]
                                         + ImageArray[x][y][2] * mask1[1][1]
                                         + ImageArray[x][y+1][2] * mask1[1][2]
                                         + ImageArray[x+1][y-1][2] * mask1[2][0]
                                         + ImageArray[x+1][y][2] * mask1[2][1]
                                         + ImageArray[x+1][y+1][2] * mask1[2][2]))

                    					 + Math.abs(Math.round(ImageArray[x-1][y-1][2] * mask2[0][0] 
                                         + ImageArray[x-1][y][2] * mask2[0][1]
                                         + ImageArray[x-1][y+1][2] * mask2[0][2]
                                         + ImageArray[x][y-1][2] * mask2[1][0]
                                         + ImageArray[x][y][2] * mask2[1][1]
                                         + ImageArray[x][y+1][2] * mask2[1][2]
                                         + ImageArray[x+1][y-1][2] * mask2[2][0]
                                         + ImageArray[x+1][y][2] * mask2[2][1]
                                         + ImageArray[x+1][y+1][2] * mask2[2][2]));

                    newImageArray[x][y][3] = Math.abs(Math.round(ImageArray[x-1][y-1][3] * mask1[0][0]
                                         + ImageArray[x-1][y][3] * mask1[0][1]
                                         + ImageArray[x-1][y+1][3] * mask1[0][2]
                                         + ImageArray[x][y-1][3] * mask1[1][0]
                                         + ImageArray[x][y][3] * mask1[1][1]
                                         + ImageArray[x][y+1][3] * mask1[1][2]
                                         + ImageArray[x+1][y-1][3] * mask1[2][0]
                                         + ImageArray[x+1][y][3] * mask1[2][1]
                                         + ImageArray[x+1][y+1][3] * mask1[2][2]))

                    					 + Math.abs(Math.round(ImageArray[x-1][y-1][3] * mask2[0][0]
                                         + ImageArray[x-1][y][3] * mask2[0][1]
                                         + ImageArray[x-1][y+1][3] * mask2[0][2]
                                         + ImageArray[x][y-1][3] * mask2[1][0]
                                         + ImageArray[x][y][3] * mask2[1][1]
                                         + ImageArray[x][y+1][3] * mask2[1][2]
                                         + ImageArray[x+1][y-1][3] * mask2[2][0]
                                         + ImageArray[x+1][y][3] * mask2[2][1]
                                         + ImageArray[x+1][y+1][3] * mask2[2][2]));
                }

                if(newImageArray[x][y][1] < 0){
                	newImageArray[x][y][1] = 0;
                }
                if(newImageArray[x][y][2] < 0){
                	newImageArray[x][y][2] = 0;
                }
                if(newImageArray[x][y][3] < 0){
                	newImageArray[x][y][3] = 0;
                }
                if(newImageArray[x][y][1] > 255){
                	newImageArray[x][y][1] = 255;
                }
                if(newImageArray[x][y][2] > 255){
                	newImageArray[x][y][2] = 255;
                }
                if(newImageArray[x][y][3] > 255){
                	newImageArray[x][y][3] = 255;
                }
                // set alpha to 255;
                newImageArray[x][y][0] = 255;

            }
        }

        return convertToBimage(newImageArray);
    }
    
    // apply a mask
    public BufferedImage apply_mask (BufferedImage image, int[][] mask, int average){
        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image);
        int[][][] newImageArray = new int[width][height][4];
        
        // ----------------------------------------- 
        // |             |           |             |
        // | f(x-1, y-1) | f(x-1, y) | f(x-1, y+1) |
        // |             |           |             |
        // -----------------------------------------
        // |             |           |             |
        // |  f(x, y-1)  |  f(x, y)  |  f(x, y+1)  |
        // |             |           |             |
        // -----------------------------------------
        // |             |           |             |
        // | f(x+1, y-1) | f(x+1, y) | f(x+1, y+1) |
        // |             |           |             |
        // -----------------------------------------
        
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                if (y == 0 || y == height-1 || x == 0 || x == width-1){
                    newImageArray[x][y][1] = ImageArray[x][y][1];
                    newImageArray[x][y][2] = ImageArray[x][y][2];
                    newImageArray[x][y][3] = ImageArray[x][y][3];

                }else{
                    newImageArray[x][y][1] = Math.round((ImageArray[x-1][y-1][1] * mask[0][0] 
                                         + ImageArray[x-1][y][1] * mask[0][1]
                                         + ImageArray[x-1][y+1][1] * mask[0][2]
                                         + ImageArray[x][y-1][1] * mask[1][0]
                                         + ImageArray[x][y][1] * mask[1][1]
                                         + ImageArray[x][y+1][1] * mask[1][2]
                                         + ImageArray[x+1][y-1][1] * mask[2][0]
                                         + ImageArray[x+1][y][1] * mask[2][1]
                                         + ImageArray[x+1][y+1][1]* mask[2][2])/average);

                    newImageArray[x][y][2] = Math.round((ImageArray[x-1][y-1][2] * mask[0][0] 
                                         + ImageArray[x-1][y][2] * mask[0][1]
                                         + ImageArray[x-1][y+1][2] * mask[0][2]
                                         + ImageArray[x][y-1][2] * mask[1][0]
                                         + ImageArray[x][y][2] * mask[1][1]
                                         + ImageArray[x][y+1][2] * mask[1][2]
                                         + ImageArray[x+1][y-1][2] * mask[2][0]
                                         + ImageArray[x+1][y][2] * mask[2][1]
                                         + ImageArray[x+1][y+1][2] * mask[2][2])/average);

                    newImageArray[x][y][3] = Math.round((ImageArray[x-1][y-1][3] * mask[0][0]
                                         + ImageArray[x-1][y][3] * mask[0][1]
                                         + ImageArray[x-1][y+1][3] * mask[0][2]
                                         + ImageArray[x][y-1][3] * mask[1][0]
                                         + ImageArray[x][y][3] * mask[1][1]
                                         + ImageArray[x][y+1][3] * mask[1][2]
                                         + ImageArray[x+1][y-1][3] * mask[2][0]
                                         + ImageArray[x+1][y][3] * mask[2][1]
                                         + ImageArray[x+1][y+1][3] * mask[2][2])/average);
                }

                if(newImageArray[x][y][1] < 0){
                	newImageArray[x][y][1] = 0;
                }
                if(newImageArray[x][y][2] < 0){
                	newImageArray[x][y][2] = 0;
                }
                if(newImageArray[x][y][3] < 0){
                	newImageArray[x][y][3] = 0;
                }
                if(newImageArray[x][y][1] > 255){
                	newImageArray[x][y][1] = 255;
                }
                if(newImageArray[x][y][2] > 255){
                	newImageArray[x][y][2] = 255;
                }
                if(newImageArray[x][y][3] > 255){
                	newImageArray[x][y][3] = 255;
                }
                // set alpha to 255;
                newImageArray[x][y][0] = 255;
            }
        }

        return convertToBimage(newImageArray);
    }

    // salt-and-pepper noise
    // add noise at a random location
    public BufferedImage salt_pepper_noise(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image); 
        int x_coord, y_coord;
        // add 500 salt noise (white)
        for (int i = 0; i < 500; i++){
        	Random randX = new Random();
            Random randY = new Random();
        	// pick a random x coordinate
        	x_coord = randX.nextInt(width);
        	// pick a random y coordinate
        	y_coord = randY.nextInt(height);
        	ImageArray[x_coord][y_coord][1] = 255;
        	ImageArray[x_coord][y_coord][2] = 255;
        	ImageArray[x_coord][y_coord][3] = 255;
    	}

    	// add 500 pepper noise (black)
        for (int i = 0; i < 500; i++){
        	Random randX = new Random();
            Random randY = new Random();
            // pick a random x coordinate
            x_coord = randX.nextInt(width);
            // pick a random y coordinate
            y_coord = randY.nextInt(height);
        	ImageArray[x_coord][y_coord][1] = 0;
        	ImageArray[x_coord][y_coord][2] = 0;
        	ImageArray[x_coord][y_coord][3] = 0;
    	}

        return convertToBimage(ImageArray);
    }

    // min filtering
    // good for removing salt noise
    // remove for positive outlier noise
    public BufferedImage min_filter(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();

        int[][][] ImageArray = convertToArray(image);   
        int[][][] newImageArray = new int[width][height][4];

        // track an index in the array
		int k;
		// make arrays to store all the 9-neighbour pixel values
        int[] rWindow = new int[9];
        int[] bWindow = new int[9];
        int[] gWindow = new int[9];

        for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if (y == 0 || y == height-1 || x == 0 || x == width-1){
                    newImageArray[x][y][1] = ImageArray[x][y][1];
                    newImageArray[x][y][2] = ImageArray[x][y][2];
                    newImageArray[x][y][3] = ImageArray[x][y][3];

                }else{
                	k = 0;
					// copy the 9-neighbour pixel values into each array (r, g, b)
					for(int s = -1; s <= 1; s++){
						for(int t = -1; t <= 1; t++){
							rWindow[k] = ImageArray[x+s][y+t][1];
							gWindow[k] = ImageArray[x+s][y+t][2];
							bWindow[k] = ImageArray[x+s][y+t][3];
							k++;
						}
					}
					// sort each array
					Arrays.sort(rWindow);
					Arrays.sort(gWindow);
					Arrays.sort(bWindow);
					// assign the smallest value 
					newImageArray[x][y][1] = rWindow[0];
					newImageArray[x][y][2] = gWindow[0];
					newImageArray[x][y][3] = bWindow[0];

					// set alpha to 255;
                	newImageArray[x][y][0] = 255;
				}
			}
		}

        return convertToBimage(newImageArray);
    }

    // max filtering
    // good for removing pepper noise
    // remove negatice outlier noise
    public BufferedImage max_filter(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();

        int[][][] ImageArray = convertToArray(image);   
        int[][][] newImageArray = new int[width][height][4];

        // track an index in the array
		int k;
		// make arrays to store all the 9-neighbour pixel values
        int[] rWindow = new int[9];
        int[] bWindow = new int[9];
        int[] gWindow = new int[9];

        for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if (y == 0 || y == height-1 || x == 0 || x == width-1){
                    newImageArray[x][y][1] = ImageArray[x][y][1];
                    newImageArray[x][y][2] = ImageArray[x][y][2];
                    newImageArray[x][y][3] = ImageArray[x][y][3];

                }else{
                	k = 0;
					// copy the 9-neighbour pixel values into each array (r, g, b)
					for(int s = -1; s <= 1; s++){
						for(int t = -1; t <= 1; t++){
							rWindow[k] = ImageArray[x+s][y+t][1];
							gWindow[k] = ImageArray[x+s][y+t][2];
							bWindow[k] = ImageArray[x+s][y+t][3];
							k++;
						}
					}
					// sort each array
					Arrays.sort(rWindow);
					Arrays.sort(gWindow);
					Arrays.sort(bWindow);
					// assign the largest value 
					newImageArray[x][y][1] = rWindow[8];
					newImageArray[x][y][2] = gWindow[8];
					newImageArray[x][y][3] = bWindow[8];

					// set alpha to 255;
                	newImageArray[x][y][0] = 255;
				}
			}
		}

        return convertToBimage(newImageArray);
    }

    // midpoint filtering
    // pepper noise becomes a bit brighter
    // salt noise becomes a bit darker
    // works best for Gaussian and uniform noise
    public BufferedImage midpoint_filter(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();

        int[][][] ImageArray = convertToArray(image);   
        int[][][] newImageArray = new int[width][height][4];

        // track an index in the array
		int k;
		// make arrays to store all the 9-neighbour pixel values
        int[] rWindow = new int[9];
        int[] bWindow = new int[9];
        int[] gWindow = new int[9];

        for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if (y == 0 || y == height-1 || x == 0 || x == width-1){
                    newImageArray[x][y][1] = ImageArray[x][y][1];
                    newImageArray[x][y][2] = ImageArray[x][y][2];
                    newImageArray[x][y][3] = ImageArray[x][y][3];

                }else{
                	k = 0;
					// copy the 9-neighbour pixel values into each array (r, g, b)
					for(int s = -1; s <= 1; s++){
						for(int t = -1; t <= 1; t++){
							rWindow[k] = ImageArray[x+s][y+t][1]; //r
							gWindow[k] = ImageArray[x+s][y+t][2]; //g
							bWindow[k] = ImageArray[x+s][y+t][3]; //b
							k++;
						}
					}
					// sort each array
					Arrays.sort(rWindow);
					Arrays.sort(gWindow);
					Arrays.sort(bWindow);
					// assign the average of smallest and largest values
					newImageArray[x][y][1] = (rWindow[0]+rWindow[8])/2; //r
					newImageArray[x][y][2] = (gWindow[0]+gWindow[8])/2; //g
					newImageArray[x][y][3] = (bWindow[0]+bWindow[8])/2; //b

					// set alpha to 255;
                	newImageArray[x][y][0] = 255;
				}
			}
		}

        return convertToBimage(newImageArray);  // Convert the array to BufferedImage
    }

    // median filtering
    // good for removing salt and pepper noise
    // reduce nosie and preserve edges simultaneously
    public BufferedImage median_filter(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();

        int[][][] ImageArray = convertToArray(image);   
        int[][][] newImageArray = new int[width][height][4];

        // track an index in the array
		int k;
		// make arrays to store all the 9-neighbour pixel values
        int[] rWindow = new int[9];
        int[] bWindow = new int[9];
        int[] gWindow = new int[9];

        for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if (y == 0 || y == height-1 || x == 0 || x == width-1){
                    newImageArray[x][y][1] = ImageArray[x][y][1];
                    newImageArray[x][y][2] = ImageArray[x][y][2];
                    newImageArray[x][y][3] = ImageArray[x][y][3];

                }else{
                	k = 0;
					// copy the 9-neighbour pixel values into each array (r, g, b)
					for(int s = -1; s <= 1; s++){
						for(int t = -1; t <= 1; t++){
							rWindow[k] = ImageArray[x+s][y+t][1]; //r
							gWindow[k] = ImageArray[x+s][y+t][2]; //g
							bWindow[k] = ImageArray[x+s][y+t][3]; //b
							k++;
						}
					}
					// sort each array
					Arrays.sort(rWindow);
					Arrays.sort(gWindow);
					Arrays.sort(bWindow);
					// assign the value in the middle of the array
					newImageArray[x][y][1] = rWindow[4]; //r
					newImageArray[x][y][2] = gWindow[4]; //g
					newImageArray[x][y][3] = bWindow[4]; //b

					// set alpha to 255;
                	newImageArray[x][y][0] = 255;
				}
			}
		}

        return convertToBimage(newImageArray);  // Convert the array to BufferedImage
    }

    // mean and standard deviation
    public void mean_sd (BufferedImage image){
    	int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image);

    	int[] HistogramR = new int[256];
    	int[] HistogramG = new int[256];
    	int[] HistogramB = new int[256];
        int r, g, b;
        // initialisation
	    for(int k = 0; k < 256; k++){ 
			HistogramR[k] = 0;
			HistogramG[k] = 0;
			HistogramB[k] = 0;
		}

        // histogram
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				r = ImageArray[x][y][1];
				g = ImageArray[x][y][2];
				b = ImageArray[x][y][3];
				HistogramR[r]++;
				HistogramG[g]++;
				HistogramB[b]++;
			}
		}

        // normalization
        float[] nHistogramR = new float[256];
        float[] nHistogramG = new float[256];
        float[] nHistogramB = new float[256];
        for(int k = 0; k < 256; k++){ 
            nHistogramR[k] = (float)HistogramR[k]/(float)(height*width);
            nHistogramG[k] = (float)HistogramG[k]/(float)(height*width);
            nHistogramB[k] = (float)HistogramB[k]/(float)(height*width);
        }

        float meanR = 0;
        float meanG = 0;
        float meanB = 0;

        // "Histogram Statistics" slide from Topic 3 
        // mean = Σ[from 0 to L-1] r(k)p(r(k))
            // r(k) = k: gray level from 0 to 255
            // p(r(k)) = nHistogramR[k]: percentage of the gray level
        for(int k = 0; k < 256; k++){
        	meanR += k * nHistogramR[k];
        	meanG += k * nHistogramG[k];
        	meanB += k * nHistogramB[k];
        }

        float varianceR = 0;
    	float varianceG = 0;
    	float varianceB = 0;
        
        // variance = Σ[from 0 to L-1] (r(k) - mean)^2 p(r(k))
        for(int k = 0; k < 256; k++){
        	varianceR += Math.pow((k-meanR), 2)*nHistogramR[k];
        	varianceG += Math.pow((k-meanG), 2)*nHistogramG[k];
        	varianceB += Math.pow((k-meanB), 2)*nHistogramB[k];
        }

        // standard deviation = sqrt(variance)
        float sdR = (float)Math.sqrt(varianceR);
        float sdG = (float)Math.sqrt(varianceG);
        float sdB = (float)Math.sqrt(varianceB);

        System.out.println("mean of red: "+meanR);
        System.out.println("mean of green: "+meanG);
        System.out.println("mean of blue: "+meanB);
        System.out.println("standard deviation of red: "+sdR);
        System.out.println("standard deviation of green: "+sdG);
        System.out.println("standard deviation of blue: "+sdB);
    }

    // simple thresholding
    // the range of threshold value is between 0 and 255 (user input)
    public BufferedImage simple_threshold(BufferedImage image, float threshold_value){
        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image); 

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
            	if (ImageArray[x][y][1] >= threshold_value){
            		ImageArray[x][y][1] = 255;
            	}else{
            		ImageArray[x][y][1] = 0;
            	}
            	if (ImageArray[x][y][2] >= threshold_value){
            		ImageArray[x][y][2] = 255;
            	}else{
            		ImageArray[x][y][2] = 0;
            	}
            	if (ImageArray[x][y][3] >= threshold_value){
            		ImageArray[x][y][3] = 255;
            	}else{
            		ImageArray[x][y][3] = 0;
            	}
            }
        }

        return convertToBimage(ImageArray);
    }

    // automated thresholding
    public BufferedImage automated_threshold(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        
        int[][][] ImageArray = convertToArray(image); 

        float backgroundR = 0;
        float backgroundG = 0;
        float backgroundB = 0;
        float objectR = 0;
        float objectG = 0;
        float objectB = 0;

        // find the initial threshold
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                // background: four corner pixels
                if ((y == 0 && x == 0) || (y == 0 && x == width-1) || (y == height-1 && x == 0) || (y == height-1 && x == width-1)){
                    backgroundR += ImageArray[x][y][1];
                    backgroundG += ImageArray[x][y][2];
                    backgroundB += ImageArray[x][y][3];
                // object: the other pixels
                }else{
                    objectR += ImageArray[x][y][1];
                    objectG += ImageArray[x][y][2];
                    objectB += ImageArray[x][y][3];
                }
            }
        }

        // mean of background and object (u(b), u(o))
        float mean_backgroundR = backgroundR/4;
        float mean_backgroundG = backgroundG/4;
        float mean_backgroundB = backgroundB/4;
        float mean_objectR = objectR/(width*height-4);
        float mean_objectG = objectG/(width*height-4);
        float mean_objectB = objectB/(width*height-4);

        // T(0): initial threshold values
        float initial_thresholdR = (mean_backgroundR + mean_objectR)/2;
        float initial_thresholdG = (mean_backgroundG + mean_objectG)/2;
        float initial_thresholdB = (mean_backgroundB + mean_objectB)/2;

        // count the number of pixels of background and object
        float count_backgroundR = 0;
        float count_backgroundG = 0;
        float count_backgroundB = 0;
        float count_objectR = 0;
        float count_objectG = 0;
        float count_objectB = 0;

        backgroundR = 0;
        backgroundG = 0;
        backgroundB = 0;
        objectR = 0;
        objectG = 0;
        objectB = 0;

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                if (ImageArray[x][y][1] < initial_thresholdR){
                    backgroundR += ImageArray[x][y][1];
                    count_backgroundR ++;
                }else{
                    objectR += ImageArray[x][y][1];
                    count_objectR++;
                }
                if (ImageArray[x][y][2] < initial_thresholdG){
                    backgroundG += ImageArray[x][y][2];
                    count_backgroundG ++;
                }else{
                    objectG += ImageArray[x][y][2];
                    count_objectG ++;
                }
                if (ImageArray[x][y][3] < initial_thresholdB){
                    backgroundB += ImageArray[x][y][3];
                    count_backgroundB ++;
                }else{
                    objectB += ImageArray[x][y][3];
                    count_objectB ++;
                }
            }
        }

        mean_backgroundR = backgroundR/count_backgroundR;
        mean_backgroundG = backgroundG/count_backgroundG;
        mean_backgroundB = backgroundB/count_backgroundB;
        mean_objectR = objectR/count_objectR;
        mean_objectG = objectG/count_objectG;
        mean_objectB = objectB/count_objectB;

        // T(1)
        float thresholdR = (mean_backgroundR + mean_objectR)/2;
        float thresholdG = (mean_backgroundG + mean_objectG)/2;
        float thresholdB = (mean_backgroundB + mean_objectB)/2;
        
        // keep previous threshold values (T(t))
        float prev_thresholdR = initial_thresholdR;
        float prev_thresholdG = initial_thresholdG;
        float prev_thresholdB = initial_thresholdB;

        // red
        while (thresholdR != prev_thresholdR){
            prev_thresholdR = thresholdR;
            backgroundR = 0;
            objectR = 0;
            count_backgroundR = 0;
            count_objectR = 0;

            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    if (ImageArray[x][y][1] < thresholdR){
                        backgroundR += ImageArray[x][y][1];
                        count_backgroundR ++;
                    }else{
                        objectR += ImageArray[x][y][1];
                        count_objectR++;
                    }
                }
            }

            mean_backgroundR = backgroundR/count_backgroundR;
            mean_objectR = objectR/count_objectR;

            thresholdR = (mean_backgroundR + mean_objectR)/2;
        }

        // green
        while (thresholdG != prev_thresholdG){
            prev_thresholdG = thresholdG;

            backgroundG = 0;
            objectG = 0;
            count_backgroundG = 0;
            count_objectG = 0;

            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    if (ImageArray[x][y][2] < thresholdG){
                        backgroundG += ImageArray[x][y][2];
                        count_backgroundG ++;
                    }else{
                        objectG += ImageArray[x][y][2];
                        count_objectG ++;
                    }
                }
            }

            mean_backgroundG = backgroundG/count_backgroundG;
            mean_objectG = objectG/count_objectG;

            thresholdG = (mean_backgroundG + mean_objectG)/2;
        }

        // blue
        while (thresholdB != prev_thresholdB){
            prev_thresholdB = thresholdB;

            backgroundB = 0;
            objectB = 0;
            count_backgroundB = 0;
            count_objectB = 0;

            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++){
                    if (ImageArray[x][y][3] < thresholdB){
                        backgroundB += ImageArray[x][y][3];
                        count_backgroundB ++;
                    }else{
                        objectB += ImageArray[x][y][3];
                        count_objectB ++;
                    }
                }
            }

            mean_backgroundB = backgroundB/count_backgroundB;
            mean_objectB = objectB/count_objectB;

            thresholdB = (mean_backgroundB + mean_objectB)/2;
        }

        System.out.println("threshold_R: "+thresholdR);
        System.out.println("threshold_G: "+thresholdG);
        System.out.println("threshold_B: "+thresholdB);

        // use the threshold value
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                if (ImageArray[x][y][1] >= thresholdR){
                    ImageArray[x][y][1] = 255;
                }else{
                    ImageArray[x][y][1] = 0;
                }
            }
        }
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                if (ImageArray[x][y][2] >= thresholdG){
                    ImageArray[x][y][2] = 255;
                }else{
                    ImageArray[x][y][2] = 0;
                }
            }
        }
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                if (ImageArray[x][y][3] >= thresholdB){
                    ImageArray[x][y][3] = 255;
                }else{
                    ImageArray[x][y][3] = 0;
                }
            }
        }

        return convertToBimage(ImageArray);
    }
    
    // undo operation
    public void undo(){
        if(opIndex > 0){
            opIndex = prevOp;
            repaint();
        };
        System.out.println("Undo button is for just one operation back (following Lab1 instruction)");
    }

    // select the second image
    public void selectImage(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int imageList = fileChooser.showOpenDialog(this);
        if (imageList == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            storeImage(selectedFile);
        }
    }

    public void Mask() {
        try {
            bi5 = ImageIO.read(new File("Baboon.bmp"));
            w = bi5.getWidth(null);
            h = bi5.getHeight(null);
            System.out.println(bi5.getType());
            if (bi5.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage bi6 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics big = bi6.getGraphics();
                big.drawImage(bi5, 0, 0, null);

                biFiltered_mask = bi5 = bi6;
            }
        } catch (IOException e) {      // deal with the situation that th image has problem;/
            System.out.println("Image could not be read");

            System.exit(1);
        }
    }

    // select a region
    public BufferedImage selectRegion(){
        Mask();

        // select the region
        Scanner s8 = new Scanner(System.in);
        System.out.println("Enter x coordinate of the top-left corner: ");
        int x_coord = s8.nextInt();
        Scanner s9 = new Scanner(System.in);
        System.out.println("Enter y coordinate of the top-left corner: ");
        int y_coord = s9.nextInt();
        Scanner s10 = new Scanner(System.in);
        System.out.println("Enter width of the region: ");
        int width = s10.nextInt();
        Scanner s11 = new Scanner(System.in);
        System.out.println("Enter height of the region: ");
        int height = s11.nextInt();

        int[][][] ImageArray = convertToArray(bi5); 
        // transparent the region
        for(int y=y_coord; y<height; y++){
            for(int x=x_coord; x<width; x++){
                ImageArray[x][y][0] = 0;
            }
        }

        region_selected = true;

        return convertToBimage(ImageArray);
    }

    //************************************
    //  You need to register your functioin here
    //************************************
    public void filterImage() {
        System.out.println(opIndex);
        System.out.println(lastOp);
        if (opIndex == lastOp) {
            return;
        }
        prevOp = lastOp;
        lastOp = opIndex;
        switch (opIndex) {
        /* original */
        case 0: biFiltered = bi;
                return; 

        /* Select ROI */
        case 1: biFiltered_mask = selectRegion();
                return;

        /* Image Negative */
        //case 12: biFiltered = ImageNegative(bi);
        case 2: biFiltered = ImageNegative(biFiltered);
                return;
        //************************************
        /* Re-scaling */
        case 3: Scanner s = new Scanner(System.in);
                System.out.println("Enter re-scaling factor (float: 0 to 2): ");
                float factor_rescaling = s.nextFloat();
                biFiltered = rescaling(biFiltered, factor_rescaling);
                return;

        /* Shifting */
        case 4: Scanner s2 = new Scanner(System.in);
                System.out.println("Enter shifting factor (int: -255 to 255): ");
                int factor_shifting = s2.nextInt();
                biFiltered = shifting(biFiltered, factor_shifting);
                return;

        /* Adding Noise and then Shifting and Re-scaling */
        case 5: Scanner s3 = new Scanner(System.in);
                System.out.println("Enter shifting factor (int: -255 to 255): ");
                int factor_shifting2 = s3.nextInt();
                Scanner s4 = new Scanner(System.in);
                System.out.println("Enter re-scaling factor (float: 0 to 2): ");
                float factor_rescaling2 = s4.nextFloat();
                biFiltered = noise_shifting_rescaling(biFiltered, factor_shifting2, factor_rescaling2);
                return;

        /* Addition */
        case 6: biFiltered = add(biFiltered, bi3);
                return;

        /* Subtraction */
        case 7: biFiltered = subtract(biFiltered, bi3);
                return;

        /* Multiplication */
        case 8: biFiltered = multiply(biFiltered, bi3);
                return;

        /* Division */
        case 9: biFiltered = divide(biFiltered, bi3);
                return;

        /* BitwiseNOT */
        case 10: biFiltered = bitwiseNOT(biFiltered);
                 return;

        /* AND */
        case 11: biFiltered = ANDop(biFiltered, bi3);
                 return;

        /* OR */
        case 12: biFiltered = ORop(biFiltered, bi3);
                 return;

        /* XOR */
        case 13: biFiltered = XORop(biFiltered, bi3);
                 return;

        /* ROI-Based operation */
        // originally biFiltered
        case 14: biFiltered = ROI_based_operation(biFiltered, bi3);
                 return;

        /* Logarithmic Function */
        case 15: biFiltered = logarithmic_function(biFiltered);
                 return;

        /* Power-Law */
        case 16: Scanner s5 = new Scanner(System.in);
                 System.out.println("Enter power-law factor (float: 0.01 to 25): ");
                 double p = s5.nextDouble();
                 biFiltered = power_law(biFiltered, p);
                 return;

        /* Random Look-Up Table */
        case 17: biFiltered = random_LUT(bi);
                return;


        /* Bit-Plane Slicing */
        case 18: Scanner s6 = new Scanner(System.in);
                 System.out.println("Select bit (int: 0-7): ");
                 int k = s6.nextInt();
                 biFiltered = bit_plane_slicing(biFiltered, k);
                 return;

        /* Histogram */
        case 19: biFiltered = construct_histogram(biFiltered);
                 return;

        /* Average */
        case 20: biFiltered = normal_average(biFiltered);
                 return;

        /* Weighted Average */
        case 21: biFiltered = weighted_average(biFiltered);
                 return;
        /* 4-neighbour Laplacian */
        case 22: biFiltered = four_neighbour_laplacian(biFiltered);
                 return;

        /* 8- neighbour Laplacian */
        case 23: biFiltered = eight_neighbour_laplacian(biFiltered);
                 return;

        /* 4-neighbour Laplacian Enhancement */
        case 24: biFiltered = four_neighbour_laplacian_enhancement(biFiltered);
                 return;

        /* 8-neighbour Laplacian Enhancement */
        case 25: biFiltered = eight_neighbour_laplacian_enhancement(biFiltered);
                 return;

        /* Sobel X */
        case 26: biFiltered = roberts(biFiltered);
                 return;

        /* Sobel X */
        case 27: biFiltered = sobel_x(biFiltered);
                 return;

        /* Sobel Y */
        case 28: biFiltered = sobel_y(biFiltered);
                 return;

        /* Salt-and-Pepper Noise */
        case 29: biFiltered = salt_pepper_noise(biFiltered);
                 return;

        /* Min Filtering */
        // originally biFiltered
        case 30: biFiltered = min_filter(biFiltered);
                 return;

        /* Max Filtering */
        // originally biFiltered
        case 31: biFiltered = max_filter(biFiltered);
                 return;

        /* Midpoint Filtering */
        // originally biFiltered
        case 32: biFiltered = midpoint_filter(biFiltered);
                 return;

        /* Median Filtering */
        // originally biFiltered
        case 33: biFiltered = median_filter(biFiltered);
                 return;
        /* Mean and Standard Deviation */
        case 34: mean_sd(biFiltered);
                 return;

       	/* Simple Thresholding */
        case 35: Scanner s7 = new Scanner(System.in);
                 System.out.println("Select threshold value (int: 0-255): ");
                 int t = s7.nextInt();
                 biFiltered = simple_threshold(biFiltered, t);
                 return;

        /* Automated Thresholding */
        case 36: biFiltered = automated_threshold(biFiltered);
                 return;

        //************************************
        }
    }

    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        
        if (cb.getActionCommand().equals("SetFilter")) {
            setOpIndex(cb.getSelectedIndex());
            repaint();
        } else if (cb.getActionCommand().equals("Formats")) {
            String format = (String)cb.getSelectedItem();
            File saveFile = new File("savedimage."+format);
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(saveFile);
            int rval = chooser.showSaveDialog(cb);
            if (rval == JFileChooser.APPROVE_OPTION) {
                saveFile = chooser.getSelectedFile();
                try {
                        ImageIO.write(biFiltered, format, saveFile);
                } catch (IOException ex) {
                }
            }
        }
    };
 
    public static void main(String s[]) {
        JFrame f = new JFrame("Image Processing Demo");

        // new window to show the original image
        JFrame f_original = new JFrame("Original Image");

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        Demo de = new Demo();
        Demo de_original = new Demo();
        f.add("Center", de);
        f_original.add("Center", de_original);
        JComboBox choices = new JComboBox(de.getDescriptions());
        choices.setActionCommand("SetFilter");
        choices.addActionListener(de);

        // undo button
        JButton undo_button = new JButton("Undo");
        undo_button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                de.undo();
            }
        });

        // select an image
        JButton file_button = new JButton("File");
        file_button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                de.selectImage();
            }
        });

        JComboBox formats = new JComboBox(de.getFormats());
        formats.setActionCommand("Formats");
        formats.addActionListener(de);
        JPanel panel = new JPanel();
        panel.add(choices);
        panel.add(undo_button);
        panel.add(file_button);
        panel.add(new JLabel("Save As"));
        panel.add(formats);
        f.add("North", panel);
        f.pack();
        f.setVisible(true);
        f_original.pack();
        f_original.setVisible(true);
    }
}
