package com.shasthosheba.doctor.prescription;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.shasthosheba.doctor.app.IntentTags;
import com.shasthosheba.doctor.app.PreferenceManager;
import com.shasthosheba.doctor.app.PublicVariables;
import com.shasthosheba.doctor.databinding.ActivityPrescriptionCreateBinding;
import com.shasthosheba.doctor.databinding.RcvMedAndTestItemBinding;
import com.shasthosheba.doctor.model.Intermediary;
import com.shasthosheba.doctor.model.Patient;
import com.shasthosheba.doctor.model.Prescription;
import com.shasthosheba.doctor.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import timber.log.Timber;

public class PrescriptionCreateActivity extends AppCompatActivity {

    private ActivityPrescriptionCreateBinding binding;
    private PreferenceManager preferenceManager;
    private AlertDialog alertDialog;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrescriptionCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (!getIntent().hasExtra(IntentTags.PRESC_PATIENT.tag)) {
            finish();
            Timber.e("No patient data found");
        }
        preferenceManager = new PreferenceManager(this);
        Patient patient = new Gson().fromJson(getIntent().getStringExtra(IntentTags.PRESC_PATIENT.tag), Patient.class);
        Timber.d("patient from intent:%s", patient);
        Intermediary intermediary = new Gson().fromJson(getIntent().getStringExtra(IntentTags.PRESC_INTERMEDIARY_OBJ.tag), Intermediary.class);
        Timber.d("intermediary from intent:%s", intermediary);
        User user = preferenceManager.getUser();

        binding.tvDocName.setText(user.getName());
        binding.tvPatientName.setText(patient.getName());

        MedAndTestAdapter medAdapter = new MedAndTestAdapter(new ArrayList<>());
        binding.rcvMedList.setAdapter(medAdapter);
        binding.rcvMedList.setLayoutManager(new LinearLayoutManager(this));
        binding.btnAddMed.setOnClickListener(v -> {
            if (binding.tietMedAdd.getText().toString().trim().isEmpty()) {
                binding.tilMedAdd.setError("Input med name and schedule");
            } else {
                binding.tilMedAdd.setErrorEnabled(false);
                medAdapter.add(binding.tietMedAdd.getText().toString().trim());
                binding.tietMedAdd.setText("");
                Timber.d("meds list item after adding:%s", medAdapter.mList.toString());
            }
        });

        MedAndTestAdapter testAdapter = new MedAndTestAdapter(new ArrayList<>());
        binding.rcvTestList.setAdapter(testAdapter);
        binding.rcvTestList.setLayoutManager(new LinearLayoutManager(this));
        binding.btnAddTest.setOnClickListener(v -> {
            if (binding.tietTestAdd.getText().toString().trim().isEmpty()) {
                binding.tilTestAdd.setError("Input test name");
            } else {
                binding.tilTestAdd.setErrorEnabled(false);
                testAdapter.add(binding.tietTestAdd.getText().toString().trim());
                binding.tietTestAdd.setText("");
                Timber.d("tests list item after adding:%s", testAdapter.mList.toString());
            }
        });


        binding.btnSave.setOnClickListener(v -> {
            Prescription prescription = new Prescription();
            if (medAdapter.mList.isEmpty()) {
                Snackbar.make(binding.getRoot(), "No medicines added", Snackbar.LENGTH_LONG).show();
                return;
            }
            alertDialog = new SpotsDialog.Builder()
                    .setContext(this)
                    .setMessage("Loading...")
                    .build();
            alertDialog.show();
            if (!preferenceManager.isConnected()) {
                alertDialog.dismiss();
                Snackbar.make(binding.getRoot(), "Connection lost", Snackbar.LENGTH_LONG).show();
            }
            prescription.setDoctorId(user.getuId());
            prescription.setDoctorName(user.getName());
            prescription.setPatientId(patient.getId());
            prescription.setPatientName(patient.getName());
            prescription.setIntermediaryId(intermediary.getId());
            prescription.setIntermediaryName(intermediary.getName());
            prescription.setIllnessDescription(binding.tietIllness.getText().toString().trim());
            prescription.setAdvice(binding.tietAdvice.getText().toString().trim());
            String title = user.getName().toLowerCase(Locale.ROOT).trim().replace(" ", "_");
            Date date = new Date();
            title += "_" + new SimpleDateFormat("MMM_dd_yyyy_HH_mm", Locale.getDefault()).format(date);
            prescription.setPrescriptionTitle(title);
            prescription.setDateUnix(date.getTime());
            prescription.setMedicines(medAdapter.mList);
            prescription.setTests(testAdapter.mList);


            firestore.collection(PublicVariables.PRESCRIPTION_KEY).add(prescription)
                    .addOnSuccessListener(documentReference -> {
                        Timber.d("prescription:%s", prescription);
                        Timber.d("added prescription without id:%s", documentReference.getId());
                        prescription.setId(documentReference.getId());
                        documentReference.set(prescription).addOnSuccessListener(unused -> {
                            Timber.d("Successfully saved presc with id:%s", prescription);
//                            if (patient.getPrescriptionIds() == null){
//                                patient.setPrescriptionIds(new ArrayList<>());
//                            }
//                            patient.getPrescriptionIds().add(prescription.getId());
//                            firestore.collection(PublicVariables.PATIENTS_KEY).document(patient.getId()).set(patient)
//                                    .addOnSuccessListener(unused1 -> {
//                                        Timber.d("Patient %s, updated with presc id:%s, new ids:%s",
//                                                patient.getId(), prescription.getId(), patient.getPrescriptionIds());
//                                        finish();
//                                    })
//                                    .addOnFailureListener(Timber::e);
                            firestore.collection(PublicVariables.PATIENTS_KEY).document(patient.getId())
                                    .update(PublicVariables.PATIENT_PRESCRIPTION_IDs, FieldValue.arrayUnion(prescription.getId()))
                                    .addOnSuccessListener(unused12 -> {
                                        Timber.d("Patient %s, updated with presc id:%s",
                                                patient.getId(), prescription.getId());
                                        finish();
                                    })
                                    .addOnFailureListener(Timber::e);
                        }).addOnFailureListener(Timber::e);
                    }).addOnFailureListener(Timber::e);
        });
    }


    private static class MedAndTestAdapter extends RecyclerView.Adapter<MedAndTestAdapter.MedAndTestViewHolder> {
        private List<String> mList;

        public MedAndTestAdapter(List<String> mList) {
            this.mList = mList;
        }

        public List<String> getList() {
            return mList;
        }

        public void add(String s) {
            mList.add(s);
            notifyItemInserted(mList.size() - 1);
        }

        @NonNull
        @Override
        public MedAndTestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MedAndTestViewHolder(RcvMedAndTestItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MedAndTestViewHolder holder, int position) {
            holder.binding.text1.setText(mList.get(position));
            holder.binding.btnClear.setOnClickListener(v -> {
                mList.remove(position);
                notifyItemRemoved(position);
                Timber.d("list items after removing:%s", mList.toString());
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class MedAndTestViewHolder extends RecyclerView.ViewHolder {
            RcvMedAndTestItemBinding binding;

            public MedAndTestViewHolder(@NonNull RcvMedAndTestItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}