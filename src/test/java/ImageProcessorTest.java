import nu.pattern.OpenCV;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageProcessorTest {

    @BeforeAll
    static void loadOpenCv() {
        OpenCV.loadLocally();
    }

    @Test
    void renderCropsToRequestedDimensions() {
        Mat original = new Mat(10, 20, CvType.CV_8UC3, new Scalar(10, 20, 30));
        try {
            EditorState state = new EditorState(1.0, 3, 2, 8, 5, 0.0);

            Mat result = ImageProcessor.render(original, state);
            try {
                assertEquals(8, result.cols());
                assertEquals(5, result.rows());
            } finally {
                result.release();
            }
        } finally {
            original.release();
        }
    }

    @Test
    void positiveExposureIncreasesPixelValues() {
        Mat original = new Mat(2, 2, CvType.CV_8UC3, new Scalar(20, 20, 20));
        try {
            Mat result = ImageProcessor.render(original, new EditorState(1.0, 0, 0, 0, 0, 40.0));
            try {
                double[] pixel = result.get(0, 0);
                assertTrue(pixel[0] > 20.0);
            } finally {
                result.release();
            }
        } finally {
            original.release();
        }
    }

    @Test
    void renderRejectsCropOutsideImageBounds() {
        Mat original = new Mat(10, 20, CvType.CV_8UC3, new Scalar(10, 20, 30));
        try {
            EditorState state = new EditorState(1.0, 15, 0, 8, 5, 0.0);

            assertThrows(IllegalArgumentException.class, () -> ImageProcessor.render(original, state));
        } finally {
            original.release();
        }
    }

    @Test
    void suggestedEditNamePreservesExtensionAndSaveWritesFile() throws IOException {
        Mat original = new Mat(2, 2, CvType.CV_8UC3, new Scalar(10, 20, 30));
        MatOfByte encoded = new MatOfByte();
        Path temporaryDirectory = Files.createTempDirectory("photo-editor-test");
        try {
            Imgcodecs.imencode(".jpg", original, encoded);

            try (ImageDocument document = ImageDocument.open(
                    new ByteArrayInputStream(encoded.toArray()), "holiday.jpg")) {
                assertEquals("holiday_edit.jpg", document.suggestedEditFileName());

                Path output = temporaryDirectory.resolve(document.suggestedEditFileName());
                document.saveTo(output);
                assertTrue(Files.size(output) > 0);
            }
        } finally {
            encoded.release();
            original.release();
            Files.deleteIfExists(temporaryDirectory.resolve("holiday_edit.jpg"));
            Files.deleteIfExists(temporaryDirectory);
        }
    }
}
