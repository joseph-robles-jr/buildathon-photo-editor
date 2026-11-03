import javafx.scene.image.Image;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** OpenCV operations used by the photo editor. */
public final class ImageProcessor {

    private ImageProcessor() {
    }

    public static Mat load(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            return decode(input, path.toString());
        }
    }

    public static Mat load(InputStream input, String description) throws IOException {
        return decode(input, description);
    }

    public static Mat render(Mat original, EditorState state) {
        requireImage(original, "original image");

        Mat result = original.clone();
        try {
            if (state.hasCrop()) {
                validateCrop(result, state);
                Mat cropped = new Mat(result, new Rect(
                        state.cropX(), state.cropY(), state.cropWidth(), state.cropHeight()));
                Mat croppedCopy = cropped.clone();
                cropped.release();
                result.release();
                result = croppedCopy;
            }

            if (state.exposure() != 0.0) {
                Mat adjusted = new Mat();
                result.convertTo(adjusted, -1, 1.0, state.exposure());
                result.release();
                result = adjusted;
            }

            if (state.red() != 0.0 || state.green() != 0.0 || state.blue() != 0.0) {
                Mat adjusted = new Mat();
                // OpenCV stores 3-channel images as BGR, so the scalar is blue, green, red.
                Core.add(result, new Scalar(state.blue(), state.green(), state.red()), adjusted);
                result.release();
                result = adjusted;
            }

            return result;
        } catch (RuntimeException exception) {
            result.release();
            throw exception;
        }
    }

    public static Image toFxImage(Mat bgrImage) {
        requireImage(bgrImage, "preview image");

        byte[] bytes = encodeForPreview(bgrImage);
        Image image = new Image(new ByteArrayInputStream(bytes));
        if (image.isError()) {
            throw new IllegalStateException("JavaFX could not decode the preview image", image.getException());
        }
        return image;
    }

    static byte[] encodeForPreview(Mat bgrImage) {
        requireImage(bgrImage, "preview image");

        Mat normalizedBgr = new Mat();
        MatOfByte encoded = new MatOfByte();
        try {
            normalizeToBgr(bgrImage, normalizedBgr);
            if (!Imgcodecs.imencode(".png", normalizedBgr, encoded)) {
                throw new IllegalStateException("OpenCV could not encode the preview image");
            }
            return encoded.toArray();
        } finally {
            encoded.release();
            normalizedBgr.release();
        }
    }

    private static Mat decode(InputStream input, String description) throws IOException {
        byte[] bytes = input.readAllBytes();
        MatOfByte encoded = new MatOfByte(bytes);
        try {
            Mat image = Imgcodecs.imdecode(encoded, Imgcodecs.IMREAD_COLOR);
            if (image.empty()) {
                image.release();
                throw new IOException("Could not decode image: " + description);
            }
            return image;
        } finally {
            encoded.release();
        }
    }

    private static void normalizeToBgr(Mat source, Mat destination) {

        //Uses The color streasms to determine if we need to convert to RGB (3 color)
        // This method was made by ChatGPT Codex.
        // This method was made by ChatGPT Codex.
        switch (source.channels()) {
            case 1 -> Imgproc.cvtColor(source, destination, Imgproc.COLOR_GRAY2BGR);
            case 3 -> source.copyTo(destination);
            case 4 -> Imgproc.cvtColor(source, destination, Imgproc.COLOR_BGRA2BGR);
            default -> throw new IllegalArgumentException(
                    "Unsupported image channel count: " + source.channels());
        }
    }

    private static void validateCrop(Mat image, EditorState state) {
        long right = (long) state.cropX() + state.cropWidth();
        long bottom = (long) state.cropY() + state.cropHeight();
        if (right > image.cols() || bottom > image.rows()) {
            throw new IllegalArgumentException("Crop rectangle is outside the image bounds");
        }
    }

    private static void requireImage(Mat image, String name) {
        if (image == null || image.empty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
    }
}
