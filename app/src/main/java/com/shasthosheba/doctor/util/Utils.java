package com.shasthosheba.doctor.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shasthosheba.doctor.app.PreferenceManager;
import com.shasthosheba.doctor.app.PublicVariables;
import com.shasthosheba.doctor.model.User;

import timber.log.Timber;

public class Utils {
    public static void setStatusOnline(Context context) {
        User user = new PreferenceManager(context).getDoctor();
        if (user == null || user.getuId() == null ||user.getuId().isEmpty()) {
            return;
        }
        user.setStatus("online");
        DatabaseReference dataRef = FirebaseDatabase.getInstance(PublicVariables.FIREBASE_DB).getReference(PublicVariables.DOCTOR_KEY);
        DatabaseReference conRef = FirebaseDatabase.getInstance(PublicVariables.FIREBASE_DB).getReference(".info/connected");
        dataRef.child(user.getuId()).setValue(user)
                .addOnSuccessListener(unused ->
                        Timber.i("util:updated status online"))
                .addOnFailureListener(Timber::e);

        conRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Timber.d(".info/connected:%s", snapshot.getValue());
                if (Boolean.FALSE.equals(snapshot.getValue(Boolean.class))) { //NOT CONNECTED
                    user.setStatus("offline");
                    dataRef.child(user.getuId()).onDisconnect().setValue(user);
                }
                new PreferenceManager(context).setConnected(Boolean.TRUE.equals(snapshot.getValue(Boolean.class)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Timber.e(error.toException());
            }
        });
    }

}
