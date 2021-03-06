package de.smarthome.app.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.smarthome.SmartHomeApplication;
import de.smarthome.app.model.responses.GetValueResponse;
import de.smarthome.app.repository.responsereactor.ResponseReactorBeaconConfig;
import de.smarthome.app.repository.responsereactor.ResponseReactorBoundariesConfig;
import de.smarthome.app.repository.responsereactor.ResponseReactorCallbackServer;
import de.smarthome.app.repository.responsereactor.ResponseReactorChannelConfig;
import de.smarthome.app.repository.responsereactor.ResponseReactorCheckAvailability;
import de.smarthome.app.repository.responsereactor.ResponseReactorClient;
import de.smarthome.app.repository.responsereactor.ResponseReactorGiraCallbackServer;
import de.smarthome.app.repository.responsereactor.ResponseReactorUIConfig;
import de.smarthome.app.utility.ToastUtility;
import de.smarthome.command.AdditionalConfigs;
import de.smarthome.command.AsyncCommand;
import de.smarthome.command.Command;
import de.smarthome.command.gira.HomeServerCommandInterpreter;
import de.smarthome.command.impl.AdditionalConfigCommand;
import de.smarthome.command.impl.ChangeValueCommand;
import de.smarthome.command.impl.CheckAvailabilityCommand;
import de.smarthome.command.impl.GetValueCommand;
import de.smarthome.command.impl.MultiReactorCommandChainImpl;
import de.smarthome.command.impl.RegisterCallback;
import de.smarthome.command.impl.RegisterCallbackServerAtGiraServer;
import de.smarthome.command.impl.RegisterClientCommand;
import de.smarthome.command.impl.SingleReactorCommandChainImpl;
import de.smarthome.command.impl.UIConfigCommand;
import de.smarthome.command.impl.UnRegisterCallback;
import de.smarthome.command.impl.UnRegisterCallbackServerAtGiraServer;
import de.smarthome.server.NoSSLRestTemplateCreator;
import de.smarthome.server.ServerHandler;
import de.smarthome.server.gira.GiraServerHandler;

public class ServerCommunicator {
    private static final String TAG = "ServerCommunicator";
    private static ServerCommunicator instance;
    private ToastUtility toastUtility;

    private static final String IP_OF_CALLBACK_SERVER = "192.168.132.219:8443";
    private final ServerHandler serverHandler = new GiraServerHandler(new HomeServerCommandInterpreter(new NoSSLRestTemplateCreator()));

    //Needed for Relogin after Update/Delete of Logindata in Optionfragment
    private MutableLiveData<Boolean> loginRequestStatus = new MutableLiveData<>();

    //Needed for beaconObserver
    private Application parentApplication;

    private int statusListSize = 0;
    private Map<String, String> newStatusValuesMap = new LinkedHashMap<>();

    public static ServerCommunicator getInstance(@Nullable Application application) {
        if (instance == null) {
            instance = new ServerCommunicator();
            if(application != null){
                instance.parentApplication = application;
            }
            instance.toastUtility = ToastUtility.getInstance();
        }
        return instance;
    }

    public void updateLoginDataStatus(Boolean status) {
        loginRequestStatus.postValue(status);
    }

    public LiveData<Boolean> getLoginRequestStatus() {
        return loginRequestStatus;
    }

    private void addToExecutorService(Thread newThread) {
        SmartHomeApplication.EXECUTOR_SERVICE.execute(newThread);
    }

    public void requestRegisterUser(Credential credential) {
        initialisationOfApplication(credential.getId(), credential.getPassword());
    }

    private void initialisationOfApplication(String userName, String pwd) {
        MultiReactorCommandChainImpl multiCommandChain = new MultiReactorCommandChainImpl();
        registerAppAtGiraServer(userName, pwd, multiCommandChain);
        getAllConfigs(multiCommandChain);
        serverHandler.sendRequest(multiCommandChain);
    }

    public void initialisationOfApplicationAfterRestart(Credential credential) {
        MultiReactorCommandChainImpl multiCommandChain = new MultiReactorCommandChainImpl();
        registerAppAtGiraServerAfterRestart(credential.getId(), credential.getPassword(), multiCommandChain);
        serverHandler.sendRequest(multiCommandChain);
    }

    private void getAllConfigs(MultiReactorCommandChainImpl multiCommandChain) {
        multiCommandChain.add(new UIConfigCommand(), new ResponseReactorUIConfig());
        getAdditionalConfigs(multiCommandChain);
    }

    private void getAdditionalConfigs(MultiReactorCommandChainImpl multiCommandChain) {
        multiCommandChain.add(new AdditionalConfigCommand(IP_OF_CALLBACK_SERVER, AdditionalConfigs.CHANNEL), new ResponseReactorChannelConfig());
        multiCommandChain.add(new AdditionalConfigCommand(IP_OF_CALLBACK_SERVER, AdditionalConfigs.LOCATION), new ResponseReactorBeaconConfig());
        multiCommandChain.add(new AdditionalConfigCommand(IP_OF_CALLBACK_SERVER, AdditionalConfigs.BOUNDARIES), new ResponseReactorBoundariesConfig());
    }

    public void getUIConfigAfterRestart(){
        SingleReactorCommandChainImpl singleCommandChain = new SingleReactorCommandChainImpl(new ResponseReactorUIConfig());
        singleCommandChain.add(new UIConfigCommand());
        serverHandler.sendRequest(singleCommandChain);
    }

    public void getAdditionalConfigsAfterRestart() {
        MultiReactorCommandChainImpl multiCommandChain = new MultiReactorCommandChainImpl();
        getAdditionalConfigs(multiCommandChain);
        serverHandler.sendRequest(multiCommandChain);
    }

    private void registerAppAtGiraServer(String userName, String pwd, MultiReactorCommandChainImpl multiCommandChain) {
        multiCommandChain.add(new RegisterClientCommand(userName, pwd), new ResponseReactorClient());
        multiCommandChain.add(new CheckAvailabilityCommand(), new ResponseReactorCheckAvailability());
        multiCommandChain.add(new RegisterCallbackServerAtGiraServer(IP_OF_CALLBACK_SERVER), new ResponseReactorGiraCallbackServer());
        multiCommandChain.add(new RegisterCallback(IP_OF_CALLBACK_SERVER), new ResponseReactorCallbackServer());
    }

    private void registerAppAtGiraServerAfterRestart(String userName, String pwd, MultiReactorCommandChainImpl multiCommandChain) {
        multiCommandChain.add(new RegisterClientCommand(userName, pwd), new ResponseReactorClient());
        multiCommandChain.add(new RegisterCallbackServerAtGiraServer(IP_OF_CALLBACK_SERVER), new ResponseReactorGiraCallbackServer());
    }

    public void requestUnregisterClient(String ipOfServer) {
        Thread requestUnregisterClientThread = new Thread(() -> {
            AsyncCommand register = new UnRegisterCallback(ipOfServer);
            serverHandler.sendRequest(register);
        });
        addToExecutorService(requestUnregisterClientThread);
    }

    public void requestSetValue(String id, String value) {
        Thread requestSetValueThread = new Thread(() -> {
            Command setValueCommand = new ChangeValueCommand(id, Float.parseFloat(value));
            serverHandler.sendRequest(setValueCommand);
        });
        addToExecutorService(requestSetValueThread);
    }

    public void requestGetValue(List<String> ids) {
        statusListSize = ids.size();
        newStatusValuesMap.clear();
        for(String id :ids){
            Thread requestGetValueThread = new Thread(() -> {
                Command getValueCommand = new GetValueCommand(id);
                handleResponseGetValue(serverHandler.sendRequest(getValueCommand));
            });
            addToExecutorService(requestGetValueThread);
        }
    }

    public void requestUnRegisterCallbackServerAtGiraServer() {
        Thread requestUnRegisterCallbackServerAtGiraServerThread = new Thread(() -> {
            Command unregisterAtGira = new UnRegisterCallbackServerAtGiraServer();
            serverHandler.sendRequest(unregisterAtGira);
        });
        addToExecutorService(requestUnRegisterCallbackServerAtGiraServerThread);
    }

    public void handleResponseGetValue(ResponseEntity responseEntity) {
        try {
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                Log.d(TAG, "response received");
                Log.d(TAG, responseEntity.getBody().toString());
                GetValueResponse valueReponse = (GetValueResponse) responseEntity.getBody();

                String value = valueReponse.getValues().get(0).getValue();
                String uID = valueReponse.getValues().get(0).getUid();

                newStatusValuesMap.put(uID, value);
                if(statusListSize == newStatusValuesMap.size()) {
                    newStatusValuesMap.put(uID, value);
                    ConfigContainer.getInstance().updateStatusGetValueMap(newStatusValuesMap);
                }
            } else {
                Log.d(TAG, "error occurred");
                Log.d(TAG, responseEntity.getStatusCode().toString());
            }
        } catch (Exception e) {
            Log.d(TAG, "handleResponseEntities, Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void unsubscribeFromEverything() {
        requestUnregisterClient(IP_OF_CALLBACK_SERVER);
        requestUnRegisterCallbackServerAtGiraServer();
    }

    public void getSavedCredentialsForLoginAfterRestart() {
        CredentialRequest credentialRequest = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();

        CredentialsClient credentialsClient = Credentials.getClient(parentApplication);

        credentialsClient.request(credentialRequest).addOnCompleteListener(new OnCompleteListener<CredentialRequestResponse>() {
            @Override
            public void onComplete(@NonNull Task<CredentialRequestResponse> task) {

                if (task.isSuccessful()) {
                    // See "Handle successful credential requests"
                    initialisationOfApplicationAfterRestart(task.getResult().getCredential());
                }else{
                    // See "Handle unsuccessful and incomplete credential requests"
                    toastUtility.prepareToast("Not able to retrieve Login data");
                }
            }
        });
    }
}