package de.smarthome.app.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.IntentSender;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.common.api.ResolvableApiException;

import de.smarthome.app.repository.Repository;
import de.smarthome.app.utility.ToastUtility;

public class LoginViewModel extends AndroidViewModel {
    private Repository repository;
    private static final String TAG = "LoginViewModel";
    private ToastUtility toastUtility;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        toastUtility = ToastUtility.getInstance();
    }

    public void registerUser(Credential userCredential) {
        repository.requestRegisterUser(userCredential);
    }

    public LiveData<Boolean> getLoginDataStatus(){
        return repository.getLoginDataStatus();
    }

    public void saveCredential(Activity activity, Credential userCredential){
        CredentialsOptions options = new CredentialsOptions.Builder()
                .forceEnableSaveDialog()
                .build();

        CredentialsClient credentialsClient = Credentials.getClient(activity, options);

        credentialsClient.save(userCredential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "SAVE: OK");
                toastUtility.prepareToast("Credentials saved");
                return;
            }

            Exception e = task.getException();
            if (e instanceof ResolvableApiException) {
                // Try to resolve the save request. This will prompt the user if
                // the credential is new.
                ResolvableApiException rae = (ResolvableApiException) e;
                try {
                    rae.startResolutionForResult(activity, 1);

                } catch (IntentSender.SendIntentException exception) {
                    // Could not resolve the request
                    Log.e(TAG, "Failed to send resolution.", exception);
                    toastUtility.prepareToast("Save failed");
                    e.printStackTrace();
                }
            } else {
                // Request has no resolution
                toastUtility.prepareToast("Save failed");
            }
        });
    }
}