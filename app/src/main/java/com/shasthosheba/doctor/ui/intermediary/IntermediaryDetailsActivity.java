package com.shasthosheba.doctor.ui.intermediary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shasthosheba.doctor.app.IntentTags;
import com.shasthosheba.doctor.app.PreferenceManager;
import com.shasthosheba.doctor.app.PublicVariables;
import com.shasthosheba.doctor.databinding.ActivityIntermediaryDetailsBinding;
import com.shasthosheba.doctor.model.Call;
import com.shasthosheba.doctor.model.Intermediary;
import com.shasthosheba.doctor.model.Patient;
import com.shasthosheba.doctor.model.User;
import com.shasthosheba.doctor.repo.Repository;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import timber.log.Timber;

public class IntermediaryDetailsActivity extends AppCompatActivity {

    private ActivityIntermediaryDetailsBinding binding;
    private DatabaseReference callRef = Repository.getFirebaseDatabase().getReference("call");
    private PreferenceManager preferenceManager;
    private Intermediary mIntermediary;

    private PatientListAdapter adapter;
    private boolean fetchDone = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntermediaryDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);
        binding.progressBar.setVisibility(View.VISIBLE);
        adapter = new PatientListAdapter(new ArrayList<>());
        binding.rcvIntermediaryPatientsList.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvIntermediaryPatientsList.setAdapter(adapter);

        if (!Repository.getInstance().isConnected()) {
            Toast.makeText(this, "Not connected to server", Toast.LENGTH_LONG).show();
            Timber.e("firebase is not connected");
            finish();
        } else if (getIntent().hasExtra(IntentTags.INTERMEDIARY_UID.tag)) {
            adapter.setFetchDone(false);
            String intermediaryId = getIntent().getStringExtra(IntentTags.INTERMEDIARY_UID.tag);
            adapter.setIntermediaryId(intermediaryId);
            binding.tvIntermediaryName.setText(getIntent().getStringExtra(IntentTags.INTERMEDIARY_NAME.tag));
            showCallButtons(getIntent().getBooleanExtra(IntentTags.INTERMEDIARY_CALL_ENABLED.tag, false));
            Repository.getFireStore().collection(PublicVariables.INTERMEDIARY_KEY).document(intermediaryId)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Timber.e(error);
                            return;
                        }
                        if (value != null && value.exists()) {
                            Intermediary intermediary;
                            intermediary = value.toObject(Intermediary.class);
                            Timber.d("onSnapshot method:got value:%s", intermediary);
                            setupCall(intermediary);
                            assert intermediary != null;
                            fetchPatients(intermediary);
                        }
                    });
        } else {
            Toast.makeText(this, "Something wrong", Toast.LENGTH_LONG).show();
            Timber.e("no intermediary id found on intent");
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    // Separated in a method just to reduce clutter
    private void fetchPatients(Intermediary intermediary) {
        assert intermediary != null;
        if (intermediary.getPatients() != null && !intermediary.getPatients().isEmpty()) {
            Timber.i("patient list is not empty:%s, contents:%s", intermediary.getPatients().size(), intermediary.getPatients());
            adapter.getList().clear();
            Timber.i("adapter cleared:%s", adapter.getItemCount());
            for (String id : intermediary.getPatients()) {
                Timber.d("fetching for patient id:%s", id);
                Repository.getFireStore().collection(PublicVariables.PATIENTS_KEY).document(id).get()
                        .addOnSuccessListener(documentSnapshot1 -> {
                            Patient fetchedPatient = documentSnapshot1.toObject(Patient.class);
                            Timber.d("fetched fetchedPatient:%s", fetchedPatient);
                            if (!adapter.getList().contains(fetchedPatient)) {
                                adapter.getList().add(fetchedPatient);
                                adapter.notifyItemInserted(adapter.getItemCount() - 1);
                                binding.progressBar.setVisibility(View.INVISIBLE); // triggers the first time,still have to wait for the consecutive times
                                adapter.setFetchDone(true);
                            }
                        }).addOnFailureListener(Timber::e);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void showCallButtons(boolean show) {
        if (show) {
            binding.btnVideoCall.setVisibility(View.VISIBLE);
            binding.btnAudioCall.setVisibility(View.VISIBLE);
        } else {
            binding.btnVideoCall.setVisibility(View.INVISIBLE);
            binding.btnAudioCall.setVisibility(View.INVISIBLE);
        }
    }

    private void setupCall(Intermediary intermediary) {
        User user = preferenceManager.getUser();
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
                    .setRoom(user.getuId())
                    .setVideoMuted(true)
                    .build();
            callRef.child(intermediary.getId()).setValue(new Call(intermediary.getId(), false, user.getuId(), user.getName()))
                    .addOnSuccessListener(unused -> JitsiMeetActivity.launch(v.getContext(), options))
                    .addOnFailureListener(Timber::e);
        });

        binding.btnVideoCall.setOnClickListener(v -> {
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(user.getuId())
                    .build();
            callRef.child(intermediary.getId()).setValue(new Call(intermediary.getId(), true, user.getuId(), user.getName()))
                    .addOnSuccessListener(unused -> JitsiMeetActivity.launch(v.getContext(), options))
                    .addOnFailureListener(Timber::e);
        });
    }
}