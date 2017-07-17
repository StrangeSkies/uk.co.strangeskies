package uk.co.strangeskies.observable;

import java.util.function.Predicate;

public class FilteringObservation<M> extends PassthroughObservation<M, M> {
  private final Observer<? super M> observer;
  private final Predicate<? super M> condition;

  public FilteringObservation(
      Observable<? extends M> parentObservable,
      Observer<? super M> observer,
      Predicate<? super M> condition) {
    this.observer = observer;
    this.condition = condition;

    passthroughObservation(parentObservable);
  }

  @Override
  public void onObserve() {
    observer.onObserve(this);
  }

  @Override
  public void onNext(M message) {
    if (condition.test(message))
      observer.onNext(message);
  }

  @Override
  public void onComplete() {
    observer.onComplete();
  }

  @Override
  public void onFail(Throwable t) {
    observer.onFail(t);
  }
}
