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

    public EditorState withZoom(double newZoom) {
        return new EditorState(newZoom, cropX, cropY, cropWidth, cropHeight, exposure);
    }

    public EditorState withCrop(int newCropX, int newCropY, int newCropWidth, int newCropHeight) {
        return new EditorState(zoom, newCropX, newCropY, newCropWidth, newCropHeight, exposure);
    }

    public EditorState withExposure(double newExposure) {
        return new EditorState(zoom, cropX, cropY, cropWidth, cropHeight, newExposure);
    }

    public boolean hasSameProcessingValues(EditorState other) {
        return cropX == other.cropX
                && cropY == other.cropY
                && cropWidth == other.cropWidth
                && cropHeight == other.cropHeight
                && Double.compare(exposure, other.exposure) == 0;
    }
}
