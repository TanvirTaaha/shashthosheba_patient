package com.shasthosheba.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.shasthosheba.doctor.app.PreferenceManager;
import com.shasthosheba.doctor.app.PublicVariables;
import com.shasthosheba.doctor.databinding.ActivityStartBinding;
import com.shasthosheba.doctor.intermediary.IntermediaryListActivity;
import com.shasthosheba.doctor.model.User;
import com.shasthosheba.doctor.util.Utils;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class StartActivity extends AppCompatActivity {

    private ActivityStartBinding binding;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );


    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) { // Not Signed in
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build()
                );
                Intent signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAlwaysShowSignInMethodScreen(true)
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build();

                signInLauncher.launch(signInIntent);

            } else { //Signed in
                showConnectedProgress(true);
                preferenceManager.setDoctor(
                        new User(firebaseAuth.getUid(),
                                firebaseAuth.getCurrentUser().getDisplayName(),
                                "offline"));
                handleAfterSignIn();
            }
        });


    }

    private void handleAfterSignIn() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), "online");
        Timber.d("User:%s", user);

        if (preferenceManager.isConnected()) {
            showConnectedProgress(true);
            FirebaseDatabase
                    .getInstance(PublicVariables.FIREBASE_DB)
                    .getReference(PublicVariables.DOCTOR_KEY).child(user.getuId()).setValue(user)
                    .addOnSuccessListener(unused -> {
                        Timber.d("launching list activity");
                        startActivity(new Intent(StartActivity.this, IntermediaryListActivity.class));
                    })
                    .addOnFailureListener(Timber::e);
            Toast.makeText(this, "Signed in successfully", Toast.LENGTH_LONG).show();
            preferenceManager.setDoctor(user);
        } else {
            showConnectedProgress(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setStatusOnline(this);
    }

    private void showConnectedProgress(boolean connected) {
        if (connected) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.tvConnecting.setText(R.string.connecting);
        } else { // Show connection lost
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.tvConnecting.setText(R.string.connection_lost);
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            //Successfully signed in
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                showConnectedProgress(true);
                preferenceManager.setDoctor(new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), "online"));
            }
            handleAfterSignIn();
            Timber.d("Logged in");
        } else {
            if (response != null) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                Timber.d(response.getError());
            }
        }
    }
}