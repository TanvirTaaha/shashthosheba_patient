package com.shasthosheba.doctor.ui.intermediary;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shasthosheba.doctor.app.IntentTags;
import com.shasthosheba.doctor.databinding.RcvIntermediaryPatientItemBinding;
import com.shasthosheba.doctor.model.Patient;
import com.shasthosheba.doctor.ui.prescription.PrescriptionListActivity;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import timber.log.Timber;

public class PatientListAdapter extends RecyclerView.Adapter<PatientListAdapter.PatientViewHolder> {

    private List<Patient> mList;
    private String intermediaryId;
    private boolean fetchDone = true;

    public PatientListAdapter(List<Patient> mList) {
        this.mList = mList;
    }

    public List<Patient> getList() {
        return mList;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PatientViewHolder(RcvIntermediaryPatientItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = mList.get(position);
        holder.binding.tvName.setText(patient.getName());
        Calendar calendar = new GregorianCalendar();
        int age = calendar.get(Calendar.YEAR) - patient.getBirthYear();
        holder.binding.tvAge.setText(String.valueOf(age));
        holder.binding.llRoot.setOnClickListener(v -> {
            if (!fetchDone) {
                return;
            }
            Intent intent = new Intent(v.getContext(), PrescriptionListActivity.class);
            intent.putExtra(IntentTags.PATIENT_NAME.tag, patient.getName());
            intent.putExtra(IntentTags.PATIENT_BIRTH_YEAR.tag, patient.getBirthYear());
            intent.putExtra(IntentTags.PATIENT_ID.tag, patient.getId());
            intent.putExtra(IntentTags.PATIENT_OBJ.tag, patient.toString());
            intent.putExtra(IntentTags.INTERMEDIARY_UID.tag, intermediaryId);
            v.getContext().startActivity(intent);
//            Toast.makeText(v.getContext(), patient.getName(), Toast.LENGTH_LONG).show();
            Timber.d("Patient clicked:%s", patient);
        });
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    public String getIntermediaryId() {
        return intermediaryId;
    }

    public void setIntermediaryId(String intermediaryId) {
        this.intermediaryId = intermediaryId;
    }

    public boolean isFetchDone() {
        return fetchDone;
    }

    public void setFetchDone(boolean fetchDone) {
        this.fetchDone = fetchDone;
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        public RcvIntermediaryPatientItemBinding binding;

        protected PatientViewHolder(@NonNull RcvIntermediaryPatientItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
