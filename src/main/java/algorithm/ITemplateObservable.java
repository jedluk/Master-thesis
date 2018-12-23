package algorithm;

public interface ITemplateObservable {
    void notifyObservers();
    void subscribeObserver(ITemplateObserver observer);
    void unsubscribeObserver(ITemplateObserver observer);
}
