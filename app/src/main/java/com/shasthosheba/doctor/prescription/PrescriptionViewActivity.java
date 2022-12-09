package com.shasthosheba.doctor.prescription;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.shasthosheba.doctor.R;
import com.shasthosheba.doctor.app.IntentTags;
import com.shasthosheba.doctor.databinding.ActivityPrescriptionViewBinding;
import com.shasthosheba.doctor.model.Prescription;

public class PrescriptionViewActivity extends AppCompatActivity {
    private Prescription prescription;
    private ActivityPrescriptionViewBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrescriptionViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!getIntent().hasExtra(IntentTags.PRESCRIPTION_OBJ.tag)) {
            Snackbar.make(binding.getRoot(), "Something went wrong", Snackbar.LENGTH_LONG).show();
            finish();
        }

        prescription = new Gson().fromJson(getIntent().getStringExtra(IntentTags.PRESCRIPTION_OBJ.tag), Prescription.class);

        binding.tvDocName.setText(prescription.getDoctorName());
        binding.tvPatientName.setText(prescription.getPatientName());
        binding.tvIllnessDesc.setText((prescription.getIllnessDescription()));
        binding.lvMedicineList.setAdapter(new ArrayAdapter<>(this, R.layout.med_and_test_view_item, R.id.tv_text, prescription.getMedicines()));
        binding.lvTestsList.setAdapter(new ArrayAdapter<>(this, R.layout.med_and_test_view_item, R.id.tv_text, prescription.getTests()));
        binding.tvAdvice.setText(prescription.getAdvice());

    }
}