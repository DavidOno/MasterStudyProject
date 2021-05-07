package de.smarthome.app.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.credentials.Credential;

import java.util.List;
import java.util.Map;

import de.smarthome.app.model.configs.BoundaryDataPoint;
import de.smarthome.beacons.BeaconObserverImplementation;
import de.smarthome.beacons.BeaconObserverSubscriber;
import de.smarthome.app.model.configs.ChannelConfig;
import de.smarthome.app.model.Datapoint;
import de.smarthome.app.model.Function;
import de.smarthome.app.model.Location;
import de.smarthome.app.model.responses.CallBackServiceInput;
import de.smarthome.app.model.responses.CallbackValueInput;
import de.smarthome.app.model.responses.ServiceEvent;
import de.smarthome.server.CallbackSubscriber;
import de.smarthome.server.MyFirebaseMessagingService;

public class Repository implements CallbackSubscriber, BeaconObserverSubscriber {
    private final String TAG = "Repository";
    private static Repository instance;

    private ConfigContainer configContainer;
    private ServerCommunicator serverCommunicator;

    private BeaconObserverImplementation beaconObserver;

    //Needed for beaconObserver
    private Application parentApplication;

    //Needed for BeaconDialog in Application, After confirm set null
    private Location beaconLocation = null;
    //Have beacon been updated? Yes ==> Start dialog
    private MutableLiveData<Boolean> beaconCheck = new MutableLiveData<>();

    public static Repository getInstance(@Nullable Application application) {
        if (instance == null) {
            instance = new Repository();
            instance.configContainer = ConfigContainer.getInstance();
            instance.serverCommunicator = ServerCommunicator.getInstance();
            if(application != null){
                instance.parentApplication = application;
            }
        }
        return instance;
    }


    public void setSelectedFunction(Function function) {
        configContainer.setSelectedFunction(function);
    }

    public Function getSelectedFunction() {
        return configContainer.getSelectedFunction();
    }

    public Credential getUserCredential() {
        return serverCommunicator.getUserCredential();
    }

    public Location getSelectedLocation() {
        return configContainer.getSelectedLocation();
    }

    public void setSelectedLocation(Location newLocation) {
        configContainer.setSelectedLocation(newLocation);
    }

    public void initBeaconCheck() {
        beaconCheck.postValue(false);
    }

    public LiveData<Boolean> checkBeacon() {
        return beaconCheck;
    }

    public Location getBeaconLocation() {
        return beaconLocation;
    }

    public LiveData<Boolean> getLoginDataStatus() {
        return serverCommunicator.getLoginDataStatus();
    }

    public ChannelConfig getSmartHomeChannelConfig() {
        return configContainer.getSmartHomeChannelConfig();
    }

    public void confirmBeacon() {
        setSelectedLocation(beaconLocation);
        beaconLocation = null;
    }

    public void initBeaconObserver() {
        beaconObserver = new BeaconObserverImplementation(parentApplication, parentApplication.getApplicationContext(),
                configContainer.getSmartHomeUiConfig(), configContainer.getSmartHomeBeaconLocations());
        beaconObserver.subscribe(this);
        beaconObserver.init();
        initBeaconCheck();
    }

    public void requestRegisterUser(Credential credential) {
        serverCommunicator.requestRegisterUser(credential);
        subscribeToMyFirebaseMessagingService();
    }

    private void subscribeToMyFirebaseMessagingService() {
        MyFirebaseMessagingService.getValueObserver().subscribe(this);
        MyFirebaseMessagingService.getServiceObserver().subscribe(this);
    }

    public void requestSetValue(String ID, String value) {
        serverCommunicator.requestSetValue(ID, value);
    }

    public void requestGetValue(List<String> IDs) {
        serverCommunicator.requestGetValue(IDs);
    }

    public MutableLiveData<List<Location>> getLocationList() {
        return configContainer.getLocationList();
    }

    public MutableLiveData<Map<Function, Function>> getFunctionList() {
        return configContainer.getFunctionList();
    }

    public LiveData<Map<Datapoint, BoundaryDataPoint>> getBoundaryList() {
        return configContainer.getBoundaryList();
    }

    public MutableLiveData<Map<Datapoint, Datapoint>> getDataPoints() {
        return configContainer.getDataPoints();
    }

    public MutableLiveData<Map<String, String>> getStatusList() {
        return configContainer.getStatusList();
    }

    public MutableLiveData<Map<String, String>> getStatusList2() {
        return configContainer.getStatusList2();
    }

    @Override
    public void update(CallbackValueInput input) {
        if (input.getEvent() == null && input.getValue() != null) {
            String value = String.valueOf(input.getValue());
            String uID = input.getUid();
            configContainer.updateStatusList(uID, value);
        }
    }

    @Override
    public void update(CallBackServiceInput input) {
        if (input.getServiceEvents() != null) {
            for (ServiceEvent serviceEvent : input.getServiceEvents()) {
                switch (serviceEvent.getEvent()) {
                    case STARTUP:
                        Log.d(TAG, "Server has started.");
                        System.out.println("Server has started.");
                        serverCommunicator.initialisationOfApplicationAfterRestart();
                        break;
                    case RESTART:
                        Log.d(TAG, "Server got restarted.");
                        System.out.println("Server got restarted.");
                        break;
                    case UI_CONFIG_CHANGED:
                        Log.d(TAG, "UI_CONFIG_CHANGED");
                        System.out.println("UI_CONFIG_CHANGED");
                        serverCommunicator.getUIConfigAfterRestart();
                        break;
                    case PROJECT_CONFIG_CHANGED:
                        Log.d(TAG, "PROJECT_CONFIG_CHANGED");
                        System.out.println("PROJECT_CONFIG_CHANGED");
                        serverCommunicator.getAdditionalConfigsAfterRestart();
                        break;
                    default:
                        Log.d(TAG, "Unknown CallBackServiceInputEvent");
                        System.out.println("Unknown CallBackServiceInputEvent");
                        break;
                }
            }
        }
    }

    @Override
    public void update(Location newLocation) {
        beaconLocation = newLocation;
        beaconCheck.postValue(true);
    }

    public void unsubscribeFromEverything() {
        MyFirebaseMessagingService.getValueObserver().unsubscribe(this);
        MyFirebaseMessagingService.getServiceObserver().unsubscribe(this);
        beaconObserver.unsubscribe();
        serverCommunicator.unsubscribeFromEverything();
    }
}