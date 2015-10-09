package com.hardsoftstudio.rxflux.dispatcher;

import android.support.v4.util.ArrayMap;
import com.hardsoftstudio.rxflux.action.RxAction;
import com.hardsoftstudio.rxflux.store.RxStoreChange;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by marcel on 13/08/15.
 */
public class Dispatcher {

  private static Dispatcher instance;
  private final RxBus bus;
  private ArrayMap<String, Subscription> rxActionMap;
  private ArrayMap<String, Subscription> rxStoreMap;

  private Dispatcher(RxBus bus) {
    this.bus = bus;
    this.rxActionMap = new ArrayMap<>();
    this.rxStoreMap = new ArrayMap<>();
  }

  public static synchronized Dispatcher getInstance(RxBus rxBus) {
    if (instance == null) instance = new Dispatcher(rxBus);
    return instance;
  }

  public <T extends RxActionDispatch> void registerRxAction(final T object) {
    String tag = object.getClass().getSimpleName();
    Subscription subscription = rxActionMap.get(tag);
    if (subscription == null || subscription.isUnsubscribed()) {
      rxActionMap.put(tag, bus.get().filter(new Func1<Object, Boolean>() {
        @Override
        public Boolean call(Object o) {
          return o instanceof RxAction;
        }
      }).subscribe(new Action1<Object>() {
        @Override
        public void call(Object o) {
          object.onRxAction((RxAction) o);
        }
      }));
    }
  }

  public <T extends RxStoreDispatch> void registerRxStore(final T object) {
    String tag = object.getClass().getSimpleName();
    Subscription subscription = rxStoreMap.get(tag);
    if (subscription == null || subscription.isUnsubscribed()) {
      rxStoreMap.put(tag, bus.get().filter(new Func1<Object, Boolean>() {
        @Override
        public Boolean call(Object o) {
          return o instanceof RxStoreChange;
        }
      }).subscribe(new Action1<Object>() {
        @Override
        public void call(Object o) {
          object.onRxStoreChanged((RxStoreChange) o);
        }
      }));
    }
  }

  public <T extends RxActionDispatch> void unregisterRxAction(final T object) {
    String tag = object.getClass().getSimpleName();
    Subscription subscription = rxActionMap.get(tag);
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
      rxActionMap.remove(tag);
    }
  }

  public <T extends RxStoreDispatch> void unregisterRxStore(final T object) {
    String tag = object.getClass().getSimpleName();
    Subscription subscription = rxStoreMap.get(tag);
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
      rxStoreMap.remove(tag);
    }
  }

  public synchronized void unregisterAll() {
    for (Subscription subscription : rxActionMap.values()) {
      subscription.unsubscribe();
    }

    for (Subscription subscription : rxStoreMap.values()) {
      subscription.unsubscribe();
    }

    rxActionMap.clear();
    rxStoreMap.clear();
  }

  public void postRxAction(final RxAction action) {
    bus.send(action);
  }

  public void postRxStoreChange(final RxStoreChange storeChange) {
    bus.send(storeChange);
  }

}