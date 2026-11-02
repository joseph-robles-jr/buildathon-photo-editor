import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** JavaFX application for the photo editor. */
public class Main extends Application {

    // Codex was used for the initial javafx implementation.
    private static final String DEFAULT_IMAGE = "/testPhotos/FlowerTestPic.jpg";

    private final ImageView imageView = new ImageView();
    private final Label statusLabel = new Label("Ready");
    private final Button saveButton = new Button("Save image");
    private ImageDocument document;

    @Override
    public void start(Stage stage) {
        stage.setTitle("OpenCV Photo Editor");

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        // We should look at grabbing screen size from OS and then make these a ratio instead of hardcoded.
        imageView.setFitWidth(760);
        imageView.setFitHeight(560);

        VBox preview = new VBox(imageView);
        preview.setAlignment(Pos.CENTER);
        preview.setPadding(new Insets(24));
        preview.setStyle("-fx-background-color: #20242b;");

        Button openButton = new Button("Open image");
        openButton.setOnAction(event -> openImage(stage));

        saveButton.setDisable(true);
        saveButton.setOnAction(event -> saveImage(stage));

        Slider zoomSlider = new Slider(0.5, 2.0, 1.0);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setDisable(true);

        TextField cropWidth = new TextField();
        cropWidth.setPromptText("Width");
        cropWidth.setPrefColumnCount(6);
        cropWidth.setDisable(true);

        TextField cropHeight = new TextField();
        cropHeight.setPromptText("Height");
        cropHeight.setPrefColumnCount(6);
        cropHeight.setDisable(true);

        Slider exposureSlider = new Slider(-100, 100, 0);
        exposureSlider.setShowTickLabels(true);
        exposureSlider.setShowTickMarks(true);
        //exposureSlider.setDisable(true);

        Slider redactionSlider = new Slider(-100, 100, 0);
        redactionSlider.setShowTickMarks(true);
        redactionSlider.setShowTickLabels(true);
        redactionSlider.setSnapToTicks(true);


        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox controls = new VBox(14,
                section("File", new HBox(8, openButton, saveButton)),
                section("Zoom", zoomSlider),
                section("Crop dimensions", new HBox(8, cropWidth, cropHeight)),
                section("Exposure", exposureSlider),
                section("Redaction",  redactionSlider),
                spacer,
                statusLabel);
        controls.setPadding(new Insets(20));
        controls.setPrefWidth(260);

        BorderPane root = new BorderPane(preview);
        root.setRight(controls);
        root.setStyle("-fx-font-size: 13px;");

        loadDefaultImage();

        Scene scene = new Scene(root, 1120, 720);
        stage.setScene(scene);
        stage.show();
    }

    private VBox section(String title, Node content) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold;");
        return new VBox(6, label, content);
    }

    private void loadDefaultImage() {
        try (var input = Main.class.getResourceAsStream(DEFAULT_IMAGE)) {
            if (input == null) {
                statusLabel.setText("Default image not found");
                return;
            }

            replaceDocument(ImageDocument.open(input, DEFAULT_IMAGE));
            statusLabel.setText("Loaded sample image");
        } catch (IOException | RuntimeException exception) {
            statusLabel.setText("Could not load sample image: " + exception.getMessage());
        }
    }

    private void replaceDocument(ImageDocument nextDocument) {
        if (document != null) {
            document.close();
        }
        document = nextDocument;
        saveButton.setDisable(false);
        refreshPreview();
    }

    private void refreshPreview() {
        if (document == null) {
            statusLabel.setText("Default image not found");
            return;
        }

        imageView.setImage(ImageProcessor.toFxImage(document.processedImage()));
    }

    private void openImage(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.bmp"));

        File selectedFile = chooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        try {
            replaceDocument(ImageDocument.open(selectedFile.toPath()));
            statusLabel.setText("Loaded " + selectedFile.getName());
        } catch (IOException | RuntimeException exception) {
            statusLabel.setText("Could not load image: " + exception.getMessage());
        }
    }

    private void saveImage(Stage stage) {
        if (document == null) {
            statusLabel.setText("No image is loaded");
            return;
        }

        Path outputPath = document.suggestedEditPath();
        if (outputPath == null) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save edited image");
            chooser.setInitialFileName(document.suggestedEditFileName());
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.bmp"));

            File selectedFile = chooser.showSaveDialog(stage);
            if (selectedFile == null) {
                return;
            }
            outputPath = selectedFile.toPath();
        }

        if (Files.exists(outputPath) && !confirmOverwrite(stage, outputPath)) {
            statusLabel.setText("Save cancelled");
            return;
        }

        try {
            document.saveTo(outputPath);
            statusLabel.setText("Saved " + outputPath.getFileName());
        } catch (IOException | RuntimeException exception) {
            statusLabel.setText("Could not save image: " + exception.getMessage());
        }
    }

    private boolean confirmOverwrite(Stage owner, Path outputPath) {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "The file " + outputPath.getFileName() + " already exists. Overwrite it?",
                ButtonType.CANCEL,
                ButtonType.OK);
        alert.initOwner(owner);
        alert.setTitle("Confirm overwrite");
        alert.setHeaderText("Edited image already exists");
        Optional<ButtonType> result = alert.showAndWait();
        return result.orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
