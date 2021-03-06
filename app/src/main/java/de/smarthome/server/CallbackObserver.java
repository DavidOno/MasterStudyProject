package de.smarthome.server;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.smarthome.app.model.responses.CallBackServiceInput;
import de.smarthome.app.model.responses.CallbackValueInput;

public class CallbackObserver {

    private static final String TAG = "CallbackObserver";

    private final List<CallbackSubscriber> subscribers = new ArrayList<>();

    public void subscribe(CallbackSubscriber subscriber){
        subscribers.add(subscriber);
    }

    public void unsubscribe(CallbackSubscriber subscriber){
        subscribers.remove(subscriber);
    }

    public void notify(CallbackValueInput input){
        Log.d(TAG, input.toString());
        subscribers.forEach(s -> s.update(input));
    }

    //TODO: Check if really unsed:
//    public void notify(CallBackServiceInput input){
//        subscribers.forEach(s -> s.update(input));
//    }

}
