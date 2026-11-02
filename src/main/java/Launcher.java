import javafx.application.Application;
import nu.pattern.OpenCV;

/** Starts the JavaFX application without triggering the JAR launcher check. */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        OpenCV.loadLocally();
        Application.launch(Main.class, args);
    }
}
