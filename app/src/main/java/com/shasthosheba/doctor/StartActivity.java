package com.shasthosheba.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Observer;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Repo;
import com.shasthosheba.doctor.app.PreferenceManager;
import com.shasthosheba.doctor.app.PublicVariables;
import com.shasthosheba.doctor.databinding.ActivityStartBinding;
import com.shasthosheba.doctor.intermediary.IntermediaryListActivity;
import com.shasthosheba.doctor.model.User;
import com.shasthosheba.doctor.repo.Repository;
import com.shasthosheba.doctor.util.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;

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
                preferenceManager.setUser(
                        new User(firebaseAuth.getUid(),
                                firebaseAuth.getCurrentUser().getDisplayName(),
                                "offline"));
                Timber.d("calling handleAfterSignIn from AuthStateListener");
                handleAfterSignIn();
            }
        });


    }

    private void handleAfterSignIn() {
        Timber.d("handleAfterSignIn called");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        User user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), "online");
        Timber.d("User:%s", user);
        DatabaseReference dataRef = FirebaseDatabase.getInstance(PublicVariables.FIREBASE_DB).getReference(PublicVariables.DOCTOR_KEY);

        if (Repository.getInstance().getNetStatus().hasActiveObservers()) {
            // this is because AuthStateListener somehow gets callbacks multiple times.
            // this is to prevent multiple observer being registered as this on must be the first in this app.
            return;
        }
        showConnectedProgress(Repository.getInstance().isConnected());

        Repository.getInstance().getNetStatus().observe(StartActivity.this, netAvailable -> {
            Timber.d("In ConMan network callbacks:netAvailable:%s", netAvailable);
            if (netAvailable) {
                showConnectedProgress(true);
                dataRef.child(user.getuId()).setValue(user)
                        .addOnSuccessListener(unused -> {
                            Timber.d("launching list activity");
                            startActivity(new Intent(StartActivity.this, IntermediaryListActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            StartActivity.this.finish();
                        })
                        .addOnFailureListener(Timber::e);
//            Toast.makeText(this, "Signed in successfully", Toast.LENGTH_LONG).show();
                preferenceManager.setUser(user);
            } else { //gets called when data turned off while running the app
                showConnectedProgress(false);
            }
        });

//        DatabaseReference conRef = FirebaseDatabase.getInstance(PublicVariables.FIREBASE_DB).getReference(".info/connected");
//        conRef.addValueEventListener(new ValueEventListener() { //Moved from Utils.setStatusOnline to here because need to be done once
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Timber.d(".info/connected:%s", snapshot.getValue());
//                if (Boolean.FALSE.equals(snapshot.getValue(Boolean.class))) { //NOT CONNECTED
//                    user.setStatus("offline");
//                    dataRef.child(user.getuId()).onDisconnect().setValue(user);
//                }
//                new PreferenceManager(StartActivity.this).setConnected(Boolean.TRUE.equals(snapshot.getValue(Boolean.class)));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Timber.e(error.toException());
//            }
//        });

//        if (preferenceManager.isConnected()) {
//            showConnectedProgress(true);
//            FirebaseDatabase
//                    .getInstance(PublicVariables.FIREBASE_DB)
//                    .getReference(PublicVariables.DOCTOR_KEY).child(user.getuId()).setValue(user)
//                    .addOnSuccessListener(unused -> {
//                        Timber.d("launching list activity");
//                        startActivity(new Intent(StartActivity.this, IntermediaryListActivity.class));
//                        StartActivity.this.finish();
//                    })
//                    .addOnFailureListener(Timber::e);
////            Toast.makeText(this, "Signed in successfully", Toast.LENGTH_LONG).show();
//            preferenceManager.setUser(user);
//        } else {
//            showConnectedProgress(false);
//            new Handler().postDelayed(() -> {
//                if (!retried) {
//                    retried = true;
//                    handleAfterSignIn();
//                }
//            }, 1000);
//        }

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
            binding.tvConnecting.setText(R.string.not_connected);
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            //Successfully signed in
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                showConnectedProgress(true);
                preferenceManager.setUser(new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), "online"));
            }
            Timber.d("Logged in");
            Timber.d("Calling handleAfterSignIn from signInResult");
            handleAfterSignIn();
        } else {
            if (response != null) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                Timber.d(response.getError());
            }
        }
    }
}