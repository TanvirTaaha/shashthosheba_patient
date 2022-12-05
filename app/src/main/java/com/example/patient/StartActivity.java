package com.example.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.patient.app.PublicVariable;
import com.example.patient.databinding.ActivityStartBinding;
import com.example.patient.doctor.DoctorListAdapter;
import com.example.patient.model.Doctor;
import com.example.patient.model.Patient;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class StartActivity extends AppCompatActivity {

    private ActivityStartBinding binding;
    private List<Doctor> doctors;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    FirebaseDatabase fireDB = FirebaseDatabase.getInstance(PublicVariable.FIREBASE_DB);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) { // Not Signed in
                binding.rlSignIn.setVisibility(View.VISIBLE);
                binding.llDocList.setVisibility(View.GONE);


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

                binding.btnSignIn.setOnClickListener(v -> signInLauncher.launch(signInIntent));

            } else { //Signed in
                binding.rlSignIn.setVisibility(View.GONE);
                binding.llDocList.setVisibility(View.VISIBLE);

                DatabaseReference dataRefDoctor = fireDB.getReference("doctors");

                DatabaseReference dataRefPatient = fireDB.getReference("patients");

                Patient patient = new Patient(firebaseUser.getUid(), firebaseUser.getDisplayName(), "online");
                dataRefPatient.child(firebaseUser.getUid()).setValue(patient);

                binding.btnSignOut.setOnClickListener(v -> {
                    patient.setStatus("offline");
                    dataRefPatient.child(firebaseUser.getUid()).setValue(patient)
                            .addOnCompleteListener(task -> FirebaseAuth.getInstance().signOut());
                });

                dataRefDoctor.get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Timber.e(task.getException());
                    } else {
                        Timber.d(Objects.requireNonNull(task.getResult().getValue(Doctor.class)).toString());
                    }
                });

                DoctorListAdapter adapter = new DoctorListAdapter(this);
                binding.rcvDocs.setAdapter(adapter);
                binding.rcvDocs.setLayoutManager(new LinearLayoutManager(this));

                dataRefDoctor.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Timber.d("Changed");
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            adapter.add(data.getValue(Doctor.class));
                            adapter.notifyItemInserted(adapter.getItemCount() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Timber.w(error.toException(), "Couldn't load doctors");
                    }
                });


            }
        });


    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            //Successfully signed in
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            Patient patient = new Patient(firebaseUser.getUid(), firebaseUser.getDisplayName(), "online");
            FirebaseDatabase
                    .getInstance(PublicVariable.FIREBASE_DB)
                    .getReference("patients").child(patient.getuId()).setValue(patient);
            Toast.makeText(this, "Signed in successfully", Toast.LENGTH_LONG).show();
            Timber.d("Logged in");
        } else {
            if (response != null) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                Timber.d(response.getError());
            }
        }
    }
}