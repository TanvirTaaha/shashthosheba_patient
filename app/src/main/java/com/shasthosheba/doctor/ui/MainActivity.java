package com.shasthosheba.doctor.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.shasthosheba.doctor.app.IntentTags;
import com.shasthosheba.doctor.app.PreferenceManager;
import com.shasthosheba.doctor.app.PublicVariables;
import com.shasthosheba.doctor.databinding.ActivityMainBinding;
import com.shasthosheba.doctor.databinding.RcvIntermediaryItemBinding;
import com.shasthosheba.doctor.model.Intermediary;
import com.shasthosheba.doctor.model.User;
import com.shasthosheba.doctor.repo.DataOrError;
import com.shasthosheba.doctor.repo.Repository;
import com.shasthosheba.doctor.ui.chamber.ChamberActivityDoctor;
import com.shasthosheba.doctor.ui.intermediary.IntermediaryDetailsActivity;
import com.shasthosheba.doctor.ui.profile.ProfileActivity;

import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseDatabase rtDB = FirebaseDatabase.getInstance(PublicVariables.FIREBASE_DB);
    private MainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnMenu.setOnClickListener(v -> {
            if (binding.llMenu.getVisibility() == View.GONE) {
                binding.llMenu.setVisibility(View.VISIBLE);
            } else {
                binding.llMenu.setVisibility(View.GONE);
            }
        });

        binding.rlSignOut.setOnClickListener(v -> {
            User user = new PreferenceManager(MainActivity.this).getUser();
            user.setStatus(PublicVariables.USER_STATUS_ONLINE);
            rtDB.getReference(PublicVariables.DOCTOR_KEY).child(user.getuId()).setValue(user)
                    .addOnCompleteListener(task -> {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this, StartActivity.class));
                    });
        });
        binding.rlProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)
                .putExtra(IntentTags.DOCTOR_ID.tag, new PreferenceManager(MainActivity.this).getUser().getuId())));
        binding.rlChangePass.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ChangePasswordActivity.class)));

        binding.btnGoToChamber.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ChamberActivityDoctor.class)));

        binding.rcvIntermediaryList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        binding.rcvIntermediaryList.setAdapter(mAdapter);
        mViewModel = new ViewModelProvider(MainActivity.this).get(MainActivityViewModel.class);
        mViewModel.getAllIntermediaries().observe(MainActivity.this, dataOrError -> {
            if (dataOrError.data != null) {
                mAdapter.submitList(dataOrError.data);
            } else {
                Timber.e(dataOrError.error);
            }
        });
    }

    private final IntermediaryListAdapter mAdapter = new IntermediaryListAdapter(new DiffUtil.ItemCallback<Intermediary>() {
        @Override
        public boolean areItemsTheSame(@NonNull Intermediary oldItem, @NonNull Intermediary newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Intermediary oldItem, @NonNull Intermediary newItem) {
            return oldItem.equals(newItem);
        }
    });

    public static class IntermediaryListAdapter extends ListAdapter<Intermediary, IntermediaryListAdapter.ViewHolder> {
        protected IntermediaryListAdapter(@NonNull DiffUtil.ItemCallback<Intermediary> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(RcvIntermediaryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Intermediary intermediary = getItem(position);
            holder.binding.tvStatus.setVisibility(View.GONE);
            holder.binding.tvName.setText(intermediary.getName());
            holder.binding.getRoot().setOnClickListener(v -> holder.binding.getRoot().getContext().startActivity(new Intent(holder.binding.getRoot().getContext(), IntermediaryDetailsActivity.class)
                    .putExtra(IntentTags.INTERMEDIARY_NAME.tag, intermediary.getName())
                    .putExtra(IntentTags.INTERMEDIARY_UID.tag, intermediary.getId())
                    .putExtra(IntentTags.INTERMEDIARY_STATUS.tag, PublicVariables.USER_STATUS_OFFLINE)
                    .putExtra(IntentTags.INTERMEDIARY_CALL_ENABLED.tag, false)));
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            public RcvIntermediaryItemBinding binding;

            public ViewHolder(@NonNull RcvIntermediaryItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    public static class MainActivityViewModel extends ViewModel {

        @NonNull
        public LiveData<DataOrError<List<Intermediary>, FirebaseFirestoreException>> getAllIntermediaries() {
            return Repository.getInstance().getAllIntermediaries();
        }
    }
}