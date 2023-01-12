package com.shasthosheba.doctor.ui.profile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;

import com.shasthosheba.doctor.R;
import com.shasthosheba.doctor.app.IntentTags;
import com.shasthosheba.doctor.databinding.ActivityProfileBinding;
import com.shasthosheba.doctor.model.DoctorProfile;

import timber.log.Timber;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ProfileViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        mViewModel.mDocId = getIntent().getStringExtra(IntentTags.DOCTOR_ID.tag);
//        if (mViewModel.mDocId == null) {
//            Timber.d("onCreate:savedInstanceState docId:%s", savedInstanceState.getString(IntentTags.DOCTOR_ID.tag));
//            mViewModel.mDocId = savedInstanceState.getString(IntentTags.DOCTOR_ID.tag);
//        }
        mViewModel.getDoctorProfile()
                .observe(this, doctorProfileOrError -> {
                    if (doctorProfileOrError.data != null) {
                        updateViews(doctorProfileOrError.data);
                    } else {
                        Timber.e(doctorProfileOrError.error);
//                        if (doctorProfileOrError.error)
                    }
                });

        ClipboardManager clipboardManager = ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE));
        binding.tvContactNo.setOnLongClickListener(v -> {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(
                    "doctor_contact_no", binding.tvContactNo.getText().toString()));
            Toast.makeText(this, "Contact number copied.", Toast.LENGTH_SHORT).show();
            return true;
        });
        binding.tvBkashNo.setOnLongClickListener(v -> {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(
                    "doctor_bkash_no", binding.tvBkashNo.getText().toString()));
            Toast.makeText(this, "bKash account copied.", Toast.LENGTH_SHORT).show();
            return true;
        });
        binding.ibEditProfile.setOnClickListener(v -> {
            Timber.d("Edit button clicked");
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)
                    .putExtra(IntentTags.DOCTOR_ID.tag, mViewModel.mDocId));
        });
    }

    private void updateViews(DoctorProfile profile) {
        binding.tvDocName.setText(profile.getName());
        binding.tvSpeciality.setText(profile.getSpeciality());
        binding.tvContactNo.setText(HtmlCompat.fromHtml(getString(R.string.contact_number_lnk, profile.getContactNo()), HtmlCompat.FROM_HTML_MODE_COMPACT));
        binding.tvBkashNo.setText(HtmlCompat.fromHtml(getString(R.string.contact_number_lnk, profile.getBkash()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    }

//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        Timber.d("onRestoreInstanceState:docId:%s", savedInstanceState.getString(IntentTags.DOCTOR_ID.tag));
//        mViewModel.mDocId = savedInstanceState.getString(IntentTags.DOCTOR_ID.tag);
//    }
}