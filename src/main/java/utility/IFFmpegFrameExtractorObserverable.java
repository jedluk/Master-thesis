package utility;

public interface IFFmpegFrameExtractorObserverable {

    void notifyObservers();
    void subscribeObserver(IFFmpegExtractorObserver observer);
    void unsubscribeObserver(IFFmpegExtractorObserver observer);
}
