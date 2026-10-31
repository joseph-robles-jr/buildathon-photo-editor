import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

/** JavaFX shell for the photo editor. Image processing is added in later steps. */
public class Main extends Application {

    // Codex was used for the initial javafx implementation.
    private static final String DEFAULT_IMAGE = "/testPhotos/FlowerTestPic.jpg";

    private final ImageView imageView = new ImageView();
    private final Label statusLabel = new Label("Ready");

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

        Button saveButton = new Button("Save image");
        //saveButton.setDisable(true);
        saveButton.setOnAction(event -> System.out.println("Save Image Pressed"));

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
        URL resource = Main.class.getResource(DEFAULT_IMAGE);
        if (resource == null) {
            statusLabel.setText("Default image not found");
            return;
        }

        imageView.setImage(new Image(resource.toExternalForm()));
        statusLabel.setText("Loaded sample image");
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

        imageView.setImage(new Image(selectedFile.toURI().toString()));
        statusLabel.setText("Loaded " + selectedFile.getName());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
