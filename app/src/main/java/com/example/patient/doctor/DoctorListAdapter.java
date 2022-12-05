package com.example.patient.doctor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patient.MeetingActivity;
import com.example.patient.app.IntentTag;
import com.example.patient.databinding.DoctorRcvItemBinding;
import com.example.patient.model.Doctor;

import java.util.ArrayList;
import java.util.List;

public class DoctorListAdapter extends RecyclerView.Adapter<DoctorListAdapter.DoctorViewHolder> {

    private List<Doctor> mList = new ArrayList<>();
    private Context mContext;

    public DoctorListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public List<Doctor> getmList() {
        return mList;
    }

    public Doctor getDoc(int pos) {
        return mList.get(pos);
    }

    public void addAll(List<Doctor> list) {
        this.mList.addAll(list);
    }

    public void clear() {
        this.mList.clear();
    }

    public void add(Doctor doctor) {
        this.mList.add(doctor);
    }

    public void add(int pos, Doctor doctor) {
        this.mList.add(pos, doctor);
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DoctorViewHolder(DoctorRcvItemBinding.inflate(LayoutInflater.from(parent.getContext())));
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = mList.get(position);
        holder.binding.tvName.setText(doctor.getName());
        holder.binding.tvStatus.setText(doctor.getStatus());
        int colorInt;
        if (doctor.getStatus().equals("online")) {
            colorInt = android.R.color.holo_green_light;
        } else {
            colorInt = android.R.color.holo_red_light;
        }
        holder.binding.tvStatus.setTextColor(ResourcesCompat.getColor(mContext.getResources(), colorInt, mContext.getTheme()));
        holder.binding.getRoot().setOnClickListener(v -> {
            mContext.startActivity(new Intent(mContext, MeetingActivity.class)
                    .putExtra(IntentTag.DOCTOR_NAME.tag, doctor.getName())
                    .putExtra(IntentTag.DOCTOR_UID.tag, doctor.getuId())
                    .putExtra(IntentTag.DOCTOR_STATUS.tag, doctor.getStatus()));

        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        DoctorRcvItemBinding binding;

        public DoctorViewHolder(@NonNull DoctorRcvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
