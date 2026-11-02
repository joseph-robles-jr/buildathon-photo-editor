/**
 * Immutable values that describe how the current image should be previewed.
 * Crop and exposure fields are included now so later UI controls can update one
 * state object instead of passing unrelated values through the application.
 */
public record EditorState(
        double zoom,
        int cropX,
        int cropY,
        int cropWidth,
        int cropHeight,
        double exposure) {

    public EditorState {
        if (!Double.isFinite(zoom) || zoom <= 0) {
            throw new IllegalArgumentException("Zoom must be a positive finite number");
        }
        if (!Double.isFinite(exposure)) {
            throw new IllegalArgumentException("Exposure must be finite");
        }
        if (cropX < 0 || cropY < 0 || cropWidth < 0 || cropHeight < 0) {
            throw new IllegalArgumentException("Crop values cannot be negative");
        }
    }

    public static EditorState defaults() {
        return new EditorState(1.0, 0, 0, 0, 0, 0.0);
    }

    public boolean hasCrop() {
        return cropWidth > 0 && cropHeight > 0;
    }
}
