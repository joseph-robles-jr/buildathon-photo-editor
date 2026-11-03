import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Cursor; // This allows for thr dragging.

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** JavaFX application for the photo editor. MUST be in Main. */
public class Main extends Application {

    // Codex was used for the initial javafx implementation.
    private static final String DEFAULT_IMAGE = "/testPhotos/FlowerTestPic.jpg";

    private final ImageView imageView = new ImageView();
    private final Label statusLabel = new Label("Ready");
    private final Button saveButton = new Button("Save image");
    private final Slider zoomSlider = new Slider(0.5, 4.0, 1.0);
    private final Label zoomValueLabel = new Label("100%");
    private final TextField cropWidthField = new TextField();
    private final TextField cropHeightField = new TextField();
    private final Button applyCropButton = new Button("Apply crop");
    private final Slider exposureSlider = new Slider(-100, 100, 0);
    private final Label exposureValueLabel = new Label("0");
    private final Slider redSlider = new Slider(-100, 100, 0);
    private final Slider greenSlider = new Slider(-100, 100, 0);
    private final Slider blueSlider = new Slider(-100, 100, 0);
    private final Label redValueLabel = new Label("0");
    private final Label greenValueLabel = new Label("0");
    private final Label blueValueLabel = new Label("0");
    private final Button resetButton = new Button("Reset");
    private ImageDocument document;

    @Override
    public void start(Stage stage) {
        stage.setTitle("OpenCV Photo Editor");

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        StackPane imageCanvas = new StackPane(imageView);
        imageCanvas.setAlignment(Pos.CENTER);
        imageCanvas.setStyle("-fx-background-color: #20242b;");

        ScrollPane scrollPane = new ScrollPane(imageCanvas);
        scrollPane.setPannable(true);
        scrollPane.setCursor(Cursor.MOVE);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        //Scrollbar on when scale large.
        // scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        //Dragable Image Logic

        scrollPane.setOnMousePressed (event ->
                scrollPane.setCursor(Cursor.CLOSED_HAND));
        scrollPane.setOnMouseReleased (event ->
                scrollPane.setCursor(Cursor.MOVE));




        // We should look at grabbing screen size from OS and then make these a ratio instead of hardcoded.

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

        double screenWidth = visualBounds.getWidth();
        double screenHeight = visualBounds.getHeight();


        imageView.setFitWidth(screenWidth);
        imageView.setFitHeight(screenHeight);

        /* //The old way of displaying. Has image bound problems. Was replaced with scrollPane.
        VBox preview = new VBox(imageView);
        preview.setAlignment(Pos.CENTER);
        preview.setPadding(new Insets(24));
        preview.setStyle("-fx-background-color: #20242b;");

        */
        Button openButton = new Button("Open image");
        openButton.setOnAction(event -> openImage(stage));

        saveButton.setDisable(true);
        saveButton.setOnAction(event -> saveImage(stage));

        zoomSlider.setBlockIncrement(0.1);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        exposureSlider.setShowTickLabels(true);
        exposureSlider.setShowTickMarks(true);
        configureColorSlider(redSlider);
        configureColorSlider(greenSlider);
        configureColorSlider(blueSlider);

        cropWidthField.setPromptText("Width");
        cropWidthField.setPrefColumnCount(6);
        cropHeightField.setPromptText("Height");
        cropHeightField.setPrefColumnCount(6);
        applyCropButton.setOnAction(event -> applyCrop());

        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> updateZoom(newValue.doubleValue()));
        exposureSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateExposure(newValue.doubleValue()));
        redSlider.valueProperty().addListener((observable, oldValue, newValue) -> updateColorAdjustments());
        greenSlider.valueProperty().addListener((observable, oldValue, newValue) -> updateColorAdjustments());
        blueSlider.valueProperty().addListener((observable, oldValue, newValue) -> updateColorAdjustments());
        resetButton.setOnAction(event -> resetEditor());

        Slider redactionSlider = new Slider(-100, 100, 0);
        redactionSlider.setShowTickMarks(true);
        redactionSlider.setShowTickLabels(true);
        redactionSlider.setSnapToTicks(true);
        redactionSlider.setDisable(true);


        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox controls = new VBox(14,
                section("File", new HBox(8, openButton, saveButton)),
                section("Zoom", new HBox(8, zoomSlider, zoomValueLabel)),
                section("Crop dimensions", new VBox(8,
                        new HBox(8, cropWidthField, cropHeightField), applyCropButton)),
                section("Exposure", new HBox(8, exposureSlider, exposureValueLabel)),
                section("Color", new VBox(8,
                        colorControl("Red", redSlider, redValueLabel),
                        colorControl("Green", greenSlider, greenValueLabel),
                        colorControl("Blue", blueSlider, blueValueLabel))),
                section("Redaction",  redactionSlider),
                spacer,
                new HBox(8, resetButton),
                statusLabel);
        controls.setPadding(new Insets(20));
        controls.setPrefWidth(260);

        /* Builds the preview window into the borderPane. Dead Code.*/
        // BorderPane root = new BorderPane(preview);
        // root.setCenter(scrollPane); //This is where the image lives. Using scrollPlane prevents image from overflowing into the control section when zoomed.
        // root.setRight(controls);
        // root.setStyle("-fx-font-size: 13px;");

        BorderPane root = new BorderPane();
        root.setCenter(scrollPane); //This is where the image lives. Using scrollPlane prevents image from overflowing into the control section when zoomed.
        root.setRight(controls);
        root.setStyle("-fx-font-size: 13px;");

        setEditorControlsEnabled(false);
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
        setEditorControlsEnabled(true);
        resetControls();
        refreshPreview();
    }

    private void refreshPreview() {
        if (document == null) {
            statusLabel.setText("Default image not found");
            return;
        }

        imageView.setImage(ImageProcessor.toFxImage(document.processedImage()));
    }

    private void updateZoom(double zoom) {
        imageView.setScaleX(zoom);
        imageView.setScaleY(zoom);
        zoomValueLabel.setText(String.format("%.0f%%", zoom * 100));
        if (document != null) {
            document.updateState(document.state().withZoom(zoom));
        }
    }

    private void updateExposure(double exposure) {
        exposureValueLabel.setText(String.format("%.0f", exposure));
        if (document == null) {
            return;
        }

        try {
            document.updateState(document.state().withExposure(exposure));
            refreshPreview();
        } catch (RuntimeException exception) {
            statusLabel.setText("Could not apply exposure: " + exception.getMessage());
        }
    }

    private void updateColorAdjustments() {
        double red = redSlider.getValue();
        double green = greenSlider.getValue();
        double blue = blueSlider.getValue();
        redValueLabel.setText(String.format("%.0f", red));
        greenValueLabel.setText(String.format("%.0f", green));
        blueValueLabel.setText(String.format("%.0f", blue));

        if (document == null) {
            return;
        }

        try {
            document.updateState(document.state().withColorAdjustments(red, green, blue));
            refreshPreview();
        } catch (RuntimeException exception) {
            statusLabel.setText("Could not apply color adjustment: " + exception.getMessage());
        }
    }

    private void applyCrop() {
        if (document == null) {
            statusLabel.setText("No image is loaded");
            return;
        }

        try {
            int width = parsePositiveInteger(cropWidthField, "Crop width");
            int height = parsePositiveInteger(cropHeightField, "Crop height");
            if (width > document.originalWidth() || height > document.originalHeight()) {
                throw new IllegalArgumentException("Crop dimensions cannot exceed the original image");
            }

            int x = (document.originalWidth() - width) / 2;
            int y = (document.originalHeight() - height) / 2;
            document.updateState(document.state().withCrop(x, y, width, height));
            refreshPreview();
            statusLabel.setText("Applied centered crop: " + width + " x " + height);
        } catch (IllegalArgumentException exception) {
            statusLabel.setText(exception.getMessage());
        }
    }

    private int parsePositiveInteger(TextField field, String fieldName) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(fieldName + " must be greater than zero");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a whole number");
        }
    }

    private void resetEditor() {
        if (document == null) {
            statusLabel.setText("No image is loaded");
            return;
        }

        document.reset();
        resetControls();
        refreshPreview();
        statusLabel.setText("Reset to original image");
    }

    private void resetControls() {
        zoomSlider.setValue(1.0);
        exposureSlider.setValue(0.0);
        redSlider.setValue(0.0);
        greenSlider.setValue(0.0);
        blueSlider.setValue(0.0);
        cropWidthField.clear();
        cropHeightField.clear();
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
        zoomValueLabel.setText("100%");
        exposureValueLabel.setText("0");
        redValueLabel.setText("0");
        greenValueLabel.setText("0");
        blueValueLabel.setText("0");
    }

    private void setEditorControlsEnabled(boolean enabled) {
        zoomSlider.setDisable(!enabled);
        cropWidthField.setDisable(!enabled);
        cropHeightField.setDisable(!enabled);
        applyCropButton.setDisable(!enabled);
        exposureSlider.setDisable(!enabled);
        redSlider.setDisable(!enabled);
        greenSlider.setDisable(!enabled);
        blueSlider.setDisable(!enabled);
        resetButton.setDisable(!enabled);
    }

    private void configureColorSlider(Slider slider) {
        slider.setBlockIncrement(10);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
    }

    private HBox colorControl(String name, Slider slider, Label valueLabel) {
        Label nameLabel = new Label(name);
        nameLabel.setPrefWidth(45);
        return new HBox(6, nameLabel, slider, valueLabel);
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
