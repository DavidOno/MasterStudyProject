package de.smarthome.command.impl;


import android.util.Log;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import de.smarthome.model.configs.ChannelConfig;
import de.smarthome.model.repository.Repository;
import de.smarthome.utility.ToastUtility;

public class ResponseReactorChannelConfig implements ResponseReactor{
    private final String TAG = "ResponseReactorChannelConfig";
    private ChannelConfig responseChannelConfig;
    private Repository parentRepository;

    private ToastUtility toastUtility;

    public ResponseReactorChannelConfig(Repository parentRepository) {
        this.parentRepository = parentRepository;

        this.toastUtility = ToastUtility.getInstance();
    }

    @Override
    public void react(ResponseEntity responseEntity) {
        try {
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                System.out.println("response received ChannelConfig");
                System.out.println(responseEntity.getBody());

                responseChannelConfig = (ChannelConfig) responseEntity.getBody();
                sendChannelConfigToRepo(responseChannelConfig);

                Log.d(TAG, "Communication with Server possible.\nStatus: " + responseEntity.getStatusCode());

                toastUtility.prepareToast("ChannelConfig successfully retrieved!");
            } else {
                System.out.println("error occurred");
                System.out.println(responseEntity.getStatusCode());

                Log.d(TAG, "Problem when trying to reach Server.\nStatus: " + responseEntity.getStatusCode());

                toastUtility.prepareToast("Unable to retrieve ChannelConfig!");
            }
        }catch(Exception e){
            Log.d(TAG, "Exception: " + e.toString());

            toastUtility.prepareToast("Exception: Unable to retrieve ChannelConfig!");
        }
    }

    public ChannelConfig getResponseChannelConfig() {
        return responseChannelConfig;
    }

    public void sendChannelConfigToRepo(ChannelConfig newChannelConfig){
        parentRepository.setChannelConfig(newChannelConfig);
    }
}