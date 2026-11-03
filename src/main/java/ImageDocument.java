import org.opencv.core.Mat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.opencv.imgcodecs.Imgcodecs;

/** Owns the source image and the currently rendered preview. */
public final class ImageDocument implements AutoCloseable {

    private final Mat original;
    private final Path sourcePath;
    private final String sourceName;
    private Mat processed;
    private EditorState state;

    private ImageDocument(Mat original, Path sourcePath, String sourceName) {
        if (original == null || original.empty()) {
            throw new IllegalArgumentException("Original image must not be empty");
        }
        this.original = original;
        this.sourcePath = sourcePath;
        this.sourceName = sourceName;
        this.state = EditorState.defaults();
        this.processed = ImageProcessor.render(original, state);
    }

    public static ImageDocument open(Path path) throws IOException {
        return new ImageDocument(ImageProcessor.load(path), path, path.getFileName().toString());
    }

    public static ImageDocument open(InputStream input, String description) throws IOException {
        return new ImageDocument(ImageProcessor.load(input, description), null, fileName(description));
    }

    public Mat processedImage() {
        return processed;
    }

    public EditorState state() {
        return state;
    }

    public int originalWidth() {
        return original.cols();
    }

    public int originalHeight() {
        return original.rows();
    }

    public String suggestedEditFileName() {
        int extensionStart = sourceName.lastIndexOf('.');
        if (extensionStart <= 0) {
            return sourceName + "_edit";
        }
        return sourceName.substring(0, extensionStart)
                + "_edit"
                + sourceName.substring(extensionStart);
    }

    public Path suggestedEditPath() {
        if (sourcePath == null) {
            return null;
        }
        return sourcePath.resolveSibling(suggestedEditFileName());
    }

    public void saveTo(Path outputPath) throws IOException {
        if (outputPath == null) {
            throw new IllegalArgumentException("Output path must not be null");
        }
        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Imgcodecs.imwrite(outputPath.toString(), processed)) {
            throw new IOException("OpenCV could not write the edited image");
        }
    }

    public void updateState(EditorState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("Editor state must not be null");
        }
        if (state.hasSameProcessingValues(newState)) {
            state = newState;
            return;
        }

        Mat nextProcessed = ImageProcessor.render(original, newState);
        processed.release();
        processed = nextProcessed;
        state = newState;
    }

    public void reset() {
        updateState(EditorState.defaults());
    }

    @Override
    public void close() {
        processed.release();
        original.release();
    }

    private static String fileName(String description) {
        try {
            Path path = Path.of(description);
            Path name = path.getFileName();
            return name == null ? "image" : name.toString();
        } catch (RuntimeException exception) {
            return "image";
        }
    }
}
