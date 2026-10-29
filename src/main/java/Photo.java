import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;

public class Photo {

    protected Mat image;
    protected Mat processedImage = new Mat();
    String filePath;

    String filePathProcessed = filePath + "processed.png"; //Split out the extension.


    public Photo(String filepath) {
        this.filePath = filepath;
        this.image = Imgcodecs.imread(this.filePath); //read the image

        if (image.empty()) {
            System.err.println("Error: Could not load image from " + this.filePath);
            return;
        }
    }

    public Mat getImage() {
        return image;
    }

    public Mat getProcessedImage() {
        return processedImage;
    }

    public void setProcessedImage(Mat processedImage) {
        this.processedImage = processedImage;
    }

    public void setImage(Mat newImage) {
        this.image = newImage;

    }

    public void displayImage() {
         //This function was primarily made by Google Gemini

        // 1. Get the host screen resolution using Java AWT
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double maxWidth = screenSize.getWidth() * 0.9;   // Using 80% of screen to leave room for window borders
        double maxHeight = screenSize.getHeight() * 0.9;

        // 2. Get original image dimensions
        double origWidth = image.cols();
        double origHeight = image.rows();

        // 3. Calculate scaling factor to maintain aspect ratio
        double scale = Math.min(maxWidth / origWidth, maxHeight / origHeight);

        // Only scale down if the image is actually larger than the screen limit
        if (scale > 1.0) {
            scale = 1.0; // Keeps small images at their original size
        }

        int newWidth = (int) (origWidth * scale);
        int newHeight = (int) (origHeight * scale);

        // 4. Resize the image Mat using OpenCV
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new Size(newWidth, newHeight), 0, 0, Imgproc.INTER_AREA);


        HighGui.imshow("My OpenCV Window", resizedImage);

        HighGui.waitKey(0);

        HighGui.destroyAllWindows();
        //
    }

    public boolean isLoaded() {
        return !image.empty();
    }


}


