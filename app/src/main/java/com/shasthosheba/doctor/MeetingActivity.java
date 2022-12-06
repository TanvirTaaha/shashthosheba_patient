package com.shasthosheba.doctor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.shasthosheba.doctor.app.IntentTag;
import com.shasthosheba.doctor.app.PublicVariable;
import com.shasthosheba.doctor.databinding.ActivityMeetingBinding;
import com.shasthosheba.doctor.model.Call;
import com.shasthosheba.doctor.model.Doctor;
import com.shasthosheba.doctor.model.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

public class MeetingActivity extends AppCompatActivity {

    private ActivityMeetingBinding binding;
    private Doctor mDoctor;

    DatabaseReference callRef = FirebaseDatabase.getInstance(PublicVariable.FIREBASE_DB).getReference("call");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMeetingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDoctor = new Doctor();
        mDoctor.setName(getIntent().getStringExtra(IntentTag.DOCTOR_NAME.tag));
        mDoctor.setuId(getIntent().getStringExtra(IntentTag.DOCTOR_UID.tag));
        mDoctor.setStatus(getIntent().getStringExtra(IntentTag.DOCTOR_STATUS.tag));

        binding.tvName.setText(mDoctor.getName());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Patient patient = new Patient(firebaseUser.getUid(), firebaseUser.getDisplayName(), "online");

        URL serverUrl;
        try {
            serverUrl = new URL("https://meet.jit.si");
            JitsiMeetConferenceOptions defaultOptions = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverUrl)
                    .setFeatureFlag("welcomepage.enabled", false)
                    .build();
            JitsiMeet.setDefaultConferenceOptions(defaultOptions);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        binding.btnAudioCall.setOnClickListener(v -> {
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(patient.getuId())
                    .setVideoMuted(true)
                    .build();
            callRef.child(patient.getuId()).setValue(new Call(mDoctor.getuId(), false, patient.getuId(), patient.getName()))
                    .addOnSuccessListener(unused -> JitsiMeetActivity.launch(v.getContext(), options))
                    .addOnFailureListener(Timber::e);
        });

        binding.btnVideoCall.setOnClickListener(v -> {
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(patient.getuId())
                    .build();
            callRef.child(patient.getuId()).setValue(new Call(mDoctor.getuId(), true, patient.getuId(), patient.getName()))
                    .addOnSuccessListener(unused -> JitsiMeetActivity.launch(v.getContext(), options))
                    .addOnFailureListener(Timber::e);
        });


        DatabaseReference conRef = FirebaseDatabase.getInstance(PublicVariable.FIREBASE_DB).getReference(".info/connected");
        conRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Timber.d(".info/connected:%s", snapshot.getValue());
                callRef.child(patient.getuId()).onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Timber.e(error.toException());
            }
        });
    }
}