package Rcvr_AAMQ;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
 
import javax.imageio.ImageIO;

import Rcvr_AAMQ.Utils;

public class ImageConvertor 
{

	 /**
     * Converts an image to another format
     *
     * @param inputImagePath Path of the source image
     * @param outputImagePath Path of the destination image
     * @param formatName the format to be converted to, one of: jpeg, png,
     * bmp, wbmp, and gif
     * @return true if successful, false otherwise
     * @throws IOException if errors occur during writing
     */
    private  boolean convertFormat(String inputImagePath,
            String outputImagePath, String formatName) throws IOException 
    {
        FileInputStream inputStream = new FileInputStream(inputImagePath);
        FileOutputStream outputStream = new FileOutputStream(outputImagePath);
         
        // reads input image from file
        BufferedImage inputImage = ImageIO.read(inputStream);
         
        // writes to the output image in specified format
        boolean result = ImageIO.write(inputImage, formatName, outputStream);
         
        // needs to close the streams
        outputStream.close();
        inputStream.close();
         
        return result;
    }
    
    public boolean ConvertImageTo(String inputImagePath, String outputImagePath, String imageFormat)
    {
    		Utils utils = new Utils();

        try {
        	
        		if(utils.FileExists(inputImagePath))
        		{
	            boolean result = convertFormat(inputImagePath,
	            		outputImagePath, imageFormat);
	            if (result)
	            		return true;
	            else
	            		return false;
        		}
        		else
        		{
        			System.out.println("Error during exporting bmp image.");
        			return false;
        		}
        		
        } catch (IOException ex) {
            System.out.println("Exception: Error during exporting bmp image.");
         //   ex.printStackTrace();
            return false;
        }
		
    }
    
    
    public static void main(String[] args) 
    {
    		ImageConvertor imageconvt = new ImageConvertor();
        String inputImage = "/Users/yuvaraj/Desktop/NewFolder/BARONY-68183881-1.jpg";
        String oututImage = "/Users/yuvaraj/Desktop/NewFolder/BARONY-68183881-1.bmp";
        String formatName = "BMP";
        try {
            boolean result = imageconvt.convertFormat(inputImage,
                    oututImage, formatName);
            if (result) {
                System.out.println("Image converted successfully.");
            } else {
                System.out.println("Could not convert image.");
            }
        } catch (IOException ex) {
            System.out.println("Error during converting image.");
            ex.printStackTrace();
        }
    }
    
}
