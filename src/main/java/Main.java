import nu.pattern.OpenCV;
import org.opencv.photo.AlignExposures;

final String[] URIS = {
        "src/main/resources/testPhotos/FlowerTestPic.jpg",
        "src/main/resources/testPhotos/FlowerFieldTestPic.jpg"
};
List<Photo> photos = new ArrayList<>();

void main() {
    OpenCV.loadLocally();

    loadImages(URIS);
    displayImages(photos);

    System.exit(0); // Ensures a clean exit of the program.
}


private void loadImages(String[] URIs) {

    for (String uri : URIs) {
        Photo photo = new Photo(uri);
        if (photo.isLoaded()) {
            photos.add(photo);
        }
    }
}

private void displayImages(List<Photo> photos) {
    for (Photo photo : photos) {
        photo.displayImage();
    }
}


//private List<Photo> AlignExposure(List<Photo> photos) {
//
//    List<Mat> mats = new ArrayList<>();
//    List<Mat> results= new ArrayList<>();
//
//    for(Photo photo : photos) {
//        mats.add(photo.getImage());
//    }
//
//    if(!mats.isEmpty()) {
//        AlignExposures.process(mats, results, 10, photos.size());
//
//    }
//
//
//    for (int i=0;  i<results.size(); i++) {
//        // Join old photo to new photo
//    }
//
//
//
//    return results;
//
//}
