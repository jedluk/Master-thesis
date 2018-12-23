package calibration;

public interface ICalibrationObservable {
    void notifyObservers();
    void subscribeObserver(ICalibrationObserver observer);
    void unsubscribeObserver(ICalibrationObserver observer);
}
