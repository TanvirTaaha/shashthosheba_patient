package com.shasthosheba.doctor.ui.prescription;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.shasthosheba.doctor.app.IntentTags;
import com.shasthosheba.doctor.app.PublicVariables;
import com.shasthosheba.doctor.databinding.ActivityPrescriptionListBinding;
import com.shasthosheba.doctor.databinding.RcvPresTitleItemBinding;
import com.shasthosheba.doctor.model.Intermediary;
import com.shasthosheba.doctor.model.Patient;
import com.shasthosheba.doctor.model.Prescription;
import com.shasthosheba.doctor.repo.Repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import dmax.dialog.SpotsDialog;
import timber.log.Timber;

public class PrescriptionListActivity extends AppCompatActivity {

    private ActivityPrescriptionListBinding binding;
    private Patient patient;
    private PrescriptionTitleAdapter adapter;

    private String intermediaryId;
    private Intermediary intermediary;
    boolean fetchDone = true;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrescriptionListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        alertDialog = new SpotsDialog.Builder().setContext(this).setMessage("Loading...").build();


        adapter = new PrescriptionTitleAdapter(new ArrayList<>());
        binding.rcvPrescription.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvPrescription.setAdapter(adapter);


        patient = new Patient();
        if (getIntent().hasExtra(IntentTags.PATIENT_ID.tag)) {
            patient.setId(getIntent().getStringExtra(IntentTags.PATIENT_ID.tag));
            patient.setName(getIntent().getStringExtra(IntentTags.PATIENT_NAME.tag));
            patient.setBirthYear(getIntent().getIntExtra(IntentTags.PATIENT_BIRTH_YEAR.tag, 1971));
            patient = new Gson().fromJson(getIntent().getStringExtra(IntentTags.PATIENT_OBJ.tag), Patient.class);
            intermediaryId = getIntent().getStringExtra(IntentTags.INTERMEDIARY_UID.tag);
            Timber.d("got intermediaryID from prev act:%s", intermediaryId);
            Timber.d("got info about patient from prev act:%s", patient);
            int age = new GregorianCalendar().get(Calendar.YEAR) - patient.getBirthYear();
            binding.tvPatientAge.setText(String.valueOf(age));
            binding.tvPatientName.setText(patient.getName());
            fetchAllPrescriptions();
        } else {
            finish();
            Timber.e("intent extras blank");
        }

        binding.btnAddPrescription.setOnClickListener(v -> {
            if (!fetchDone) {
                return;
            }
            startActivity(new Intent(PrescriptionListActivity.this, PrescriptionCreateActivity.class)
                    .putExtra(IntentTags.PRESC_PATIENT.tag, new Gson().toJson(patient))
                    .putExtra(IntentTags.PRESC_INTERMEDIARY_OBJ.tag, new Gson().toJson(intermediary)));
        });
        Repository.getFireStore().collection(PublicVariables.INTERMEDIARY_KEY).document(intermediaryId).get()
                .addOnSuccessListener(
                        documentSnapshot -> {
                            intermediary = documentSnapshot.toObject(Intermediary.class);
                            Timber.d("intermediary for id:%s loaded from cloud:%s", intermediaryId, intermediary);
                        })
                .addOnFailureListener(Timber::e);
    }

    public void showEmptyListIcon(boolean empty) {
        if (empty) {
            binding.ivEmptyListIcon.setVisibility(View.VISIBLE);
            binding.rcvPrescription.setVisibility(View.INVISIBLE);
        } else {
            binding.ivEmptyListIcon.setVisibility(View.GONE);
            binding.rcvPrescription.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchAllPrescriptions() {
        alertDialog.show();
        Repository.getFireStore().collection(PublicVariables.PATIENTS_KEY).document(patient.getId()).get()
                .addOnSuccessListener(documentSnapshotPatient -> {
                    List<String> prescriptionIds = Objects.requireNonNull(documentSnapshotPatient.toObject(Patient.class)).getPrescriptionIds();
                    if (prescriptionIds == null || prescriptionIds.isEmpty()) {
                        Timber.d("prescription list empty");
                        alertDialog.dismiss();
                        showEmptyListIcon(true);
                        return;
                    }
                    showEmptyListIcon(false);
                    fetchDone = false;


                    adapter.mList.clear();
                    Timber.d("adapter cleared:%s", adapter.getItemCount());
                    for (String presId : prescriptionIds) {
                        Timber.d("prescription id list:%s", prescriptionIds);
                        Repository.getFireStore().collection(PublicVariables.PRESCRIPTION_KEY).document(presId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    Prescription fetchedPrescription = documentSnapshot.toObject(Prescription.class);
                                    Timber.d("fetched prescription:%s", fetchedPrescription);
                                    if (!adapter.mList.contains(fetchedPrescription)) {
                                        adapter.mList.add(fetchedPrescription);
                                        adapter.notifyItemInserted(adapter.getItemCount() - 1);
                                        fetchDone = true;
                                        alertDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(Timber::e);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(Timber::e);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAllPrescriptions();
    }

    private class PrescriptionTitleAdapter extends RecyclerView.Adapter<PrescriptionTitleAdapter.PresTitleViewHolder> {
        private List<Prescription> mList;

        public PrescriptionTitleAdapter(List<Prescription> mList) {
            this.mList = mList;
        }

        public List<Prescription> getList() {
            return mList;
        }

        @NonNull
        @Override
        public PresTitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PresTitleViewHolder(RcvPresTitleItemBinding.inflate(getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PresTitleViewHolder holder, int position) {
            Prescription prescription = mList.get(position);
            holder.binding.tvPresTitle.setText(prescription.getPrescriptionTitle());
            holder.binding.llRoot.setOnClickListener(v -> {
                if (fetchDone) {
//                    Toast.makeText(v.getContext(), prescription.getPrescriptionTitle(), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(v.getContext(), PrescriptionViewActivity.class)
                            .putExtra(IntentTags.PRESCRIPTION_OBJ.tag, prescription.toString()));
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class PresTitleViewHolder extends RecyclerView.ViewHolder {
            RcvPresTitleItemBinding binding;

            public PresTitleViewHolder(@NonNull RcvPresTitleItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}