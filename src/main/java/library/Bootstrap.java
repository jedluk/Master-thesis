package library;

public class Bootstrap {

    static {
        System.out.println("Loading dll...");
        try {
            nu.pattern.OpenCV.loadShared();
        } catch (UnsatisfiedLinkError e){
            System.out.println("Could not load native library");
        }
    }
}
