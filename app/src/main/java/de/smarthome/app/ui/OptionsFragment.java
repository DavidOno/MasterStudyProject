package de.smarthome.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import de.smarthome.R;
import de.smarthome.app.viewmodel.OptionsViewModel;
import de.smarthome.app.utility.ToastUtility;

public class OptionsFragment extends Fragment {
    private final String TAG = "OptionsFragment";

    private Button buttonLogout;

    private OptionsViewModel optionsViewModel;
    private final ToastUtility toastUtility = ToastUtility.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        optionsViewModel = new ViewModelProvider(requireActivity()).get(OptionsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        findViewsByID(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonLogout.setOnClickListener(v -> test());
    }


    private void findViewsByID(View view) {
        buttonLogout = view.findViewById(R.id.button_logout);
    }

    private void test(){
        toastUtility.prepareToast("User Credential were deleted!");
        navigateToLoginFragment();
        deleteCredential(optionsViewModel.getUserCredential());
    }

    private void navigateToLoginFragment() {
        NavController navController = NavHostFragment.findNavController(this);

        navController.navigate(R.id.loginFragment);
    }

    public void deleteCredential(Credential credential){
        CredentialsClient credentialsClient = Credentials.getClient(this.getActivity());

        credentialsClient.delete(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    toastUtility.prepareToast("Login data deleted");
                }
            }
        });

    }
}