package com.shasthosheba.doctor.ui.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestoreException;
import com.shasthosheba.doctor.app.IntentTags;
import com.shasthosheba.doctor.app.PreferenceManager;
import com.shasthosheba.doctor.databinding.ActivityEditProfileBinding;
import com.shasthosheba.doctor.databinding.ActivityProfileBinding;
import com.shasthosheba.doctor.model.DoctorProfile;
import com.shasthosheba.doctor.repo.DataOrError;

import timber.log.Timber;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private ProfileViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        mViewModel.mDocId = getIntent().getStringExtra(IntentTags.DOCTOR_ID.tag);
        mViewModel.getDoctorProfile().observe(this, doctorProfileOrError -> {
            if (doctorProfileOrError.data != null) {
                DoctorProfile docProf = doctorProfileOrError.data;
                binding.tietDoctorName.setText(docProf.getName());
                binding.tietSpeciality.setText(docProf.getSpeciality());
                binding.tietContactNo.setText(docProf.getContactNo());
                binding.tietBkash.setText(docProf.getBkash());
            } else {
                Timber.e(doctorProfileOrError.error);
            }
        });
        binding.btnSave.setOnClickListener(v -> {
            DoctorProfile docProf = new DoctorProfile();
            docProf.setDocId(mViewModel.mDocId);
            docProf.setName(binding.tietDoctorName.getText().toString().trim());
            docProf.setSpeciality(binding.tietSpeciality.getText().toString().trim());
            docProf.setContactNo(binding.tietContactNo.getText().toString().trim());
            docProf.setBkash(binding.tietBkash.getText().toString().trim());
            mViewModel.setDoctorProfile(docProf).observe(EditProfileActivity.this, successOrError -> {
                if (successOrError.data) {
                    Toast.makeText(EditProfileActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Server error. Check connections", Toast.LENGTH_LONG).show();
                    Timber.e(successOrError.error);
                }
            });
        });
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Timber.d("onOptionsItemSelected: android.R.id.home");
            onBackPressed();
        }
        return true;
    }
}