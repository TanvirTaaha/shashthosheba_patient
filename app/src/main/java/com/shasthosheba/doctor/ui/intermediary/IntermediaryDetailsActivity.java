package com.shasthosheba.doctor.ui.intermediary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.facebook.react.modules.core.PermissionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.shasthosheba.doctor.app.IntentTags;
import com.shasthosheba.doctor.app.PreferenceManager;
import com.shasthosheba.doctor.app.PublicVariables;
import com.shasthosheba.doctor.databinding.ActivityIntermediaryDetailsBinding;
import com.shasthosheba.doctor.model.Call;
import com.shasthosheba.doctor.model.ChamberMember;
import com.shasthosheba.doctor.model.FCMNotificationObj;
import com.shasthosheba.doctor.model.Intermediary;
import com.shasthosheba.doctor.model.Patient;
import com.shasthosheba.doctor.model.User;
import com.shasthosheba.doctor.repo.DataOrError;
import com.shasthosheba.doctor.repo.Repository;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class IntermediaryDetailsActivity extends AppCompatActivity {

    private ActivityIntermediaryDetailsBinding binding;
    private DatabaseReference callRef = Repository.getFirebaseDatabase().getReference(PublicVariables.CALL_KEY);
    private PreferenceManager preferenceManager;
    private JitsiMeetConferenceOptions conferenceOptions;

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
            if (getIntent().hasExtra(IntentTags.CHAMBER_MEMBER_OBJ.tag)) {
                setupCall(new Gson().fromJson(getIntent().getStringExtra(IntentTags.CHAMBER_MEMBER_OBJ.tag), ChamberMember.class));
            }
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
            binding.llCallButtons.setVisibility(View.VISIBLE);
        } else {
            binding.llCallButtons.setVisibility(View.INVISIBLE);
        }
    }

    private void setupCall(ChamberMember chamberMember) {
        if (chamberMember == null) {
            return;
        }
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
            conferenceOptions = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(user.getuId())
                    .setVideoMuted(true)
                    .build();
            Call call = new Call(chamberMember.getIntermediaryId(), false, user.getuId(), user.getName());
//            callRef.child(intermediary.getId()).setValue(call)
//                    .addOnSuccessListener(unused -> JitsiMeetActivity.launch(v.getContext(), options))
//                    .addOnFailureListener(Timber::e);
            sendCallPushNotificationAndProceed(call, chamberMember);
        });

        binding.btnVideoCall.setOnClickListener(v -> {
            conferenceOptions = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(user.getuId())
                    .build();
            Call call = new Call(chamberMember.getIntermediaryId(), true, user.getuId(), user.getName());
//            callRef.child(intermediary.getId()).setValue(new Call(intermediary.getId(), true, user.getuId(), user.getName()))
//                    .addOnSuccessListener(unused -> JitsiMeetActivity.launch(v.getContext(), options))
//                    .addOnFailureListener(Timber::e);
            sendCallPushNotificationAndProceed(call, chamberMember);
        });

        binding.btnRemoveFromChamber.setOnClickListener(v -> Repository.getInstance().removeChamberMember(chamberMember.getIntermediaryId()).observe(this, booleanOrError -> {
            if (booleanOrError.data) {
                finish();
            }
            Timber.e(booleanOrError.error);
        }));

        Repository.getFirebaseDatabase().getReference(PublicVariables.CHAMBER_KEY).child(Long.toString(chamberMember.getTimestamp())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Timber.v("chamber member onDataChange:snapshot.exists:%s", snapshot.exists());
                if (!snapshot.exists()) {
                    binding.tvOutOfChamber.setVisibility(View.VISIBLE);
                    showCallButtons(false);
                } else {
                    binding.tvOutOfChamber.setVisibility(View.GONE);
                    showCallButtons(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Timber.w(error.toException());
            }
        });
    }

    private void sendCallPushNotificationAndProceed(Call call, ChamberMember member) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        FCMNotificationObj.Notification notification = new FCMNotificationObj.Notification();
        notification.setTitle(call.isVideo() ? "Video call" : "Audio call");
        notification.setBody("Call from " + call.getDoctor());

        FCMNotificationObj.Data data = new FCMNotificationObj.Data();
        data.setCall(call);

        FCMNotificationObj fcmNotification = new FCMNotificationObj();
        fcmNotification.setTo(member.getCallDeviceToken());
        fcmNotification.setNotification(notification);
        fcmNotification.setData(data);

        MediaType mediaType = MediaType.parse("application/json");
        Timber.d("the json sent as requ body:%s", fcmNotification.toString());
        RequestBody body = RequestBody.create(fcmNotification.toString(), mediaType);
        Request request = new Request.Builder()
                .url(PublicVariables.FCM_HTTP_URL)
                .post(body)
                .addHeader("Authorization", "key=" + PublicVariables.FCM_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Toast.makeText(IntermediaryDetailsActivity.this, "Call failed", Toast.LENGTH_SHORT).show();
                Timber.e(e);
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = Objects.requireNonNull(response.body()).string();
                        Timber.d("response is successful,resBody:%s", responseBody);
                        JSONObject jsonObject = new JSONObject(responseBody);
                        if (jsonObject.getInt("success") >= 1) {
                            Timber.v("Launching JitsiMeetActivity");
                            JitsiMeetActivity.launch(IntermediaryDetailsActivity.this, conferenceOptions);
                            return;
                        }
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                //if comes to here, then notification send failed
                Toast.makeText(IntermediaryDetailsActivity.this, "Call failed", Toast.LENGTH_SHORT).show();
                Timber.e("Notification send failed");
            }
        });

    }
}