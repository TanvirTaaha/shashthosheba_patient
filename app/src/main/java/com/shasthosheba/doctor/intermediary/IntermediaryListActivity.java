package com.shasthosheba.doctor.intermediary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shasthosheba.doctor.StartActivity;
import com.shasthosheba.doctor.app.PreferenceManager;
import com.shasthosheba.doctor.app.PublicVariables;
import com.shasthosheba.doctor.databinding.ActivityIntermediaryListBinding;
import com.shasthosheba.doctor.model.User;

import timber.log.Timber;

public class IntermediaryListActivity extends AppCompatActivity {

    FirebaseDatabase rtDB = FirebaseDatabase.getInstance(PublicVariables.FIREBASE_DB);
    private ActivityIntermediaryListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntermediaryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        IntermediaryListAdapter adapter = new IntermediaryListAdapter(this);
        binding.rcvIntermediaryList.setAdapter(adapter);
        binding.rcvIntermediaryList.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference dataRefIntermediary = rtDB.getReference(PublicVariables.INTERMEDIARY_KEY);
        dataRefIntermediary.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Timber.d("Changed in intermediary list");
                adapter.clear();
                adapter.notifyDataSetChanged();
                for (DataSnapshot data : snapshot.getChildren()) {
                    adapter.add(data.getValue(User.class));
                    adapter.notifyItemInserted(adapter.getItemCount() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Timber.w(error.toException(), "Couldn't load doctors");
            }
        });

        binding.ibSignOut.setOnClickListener(v -> {
            User user = new PreferenceManager(IntermediaryListActivity.this).getDoctor();
            user.setStatus("offline");
            rtDB.getReference(PublicVariables.DOCTOR_KEY).child(user.getuId()).setValue(user)
                    .addOnCompleteListener(task -> {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(IntermediaryListActivity.this, StartActivity.class));
                    });
        });
    }
}