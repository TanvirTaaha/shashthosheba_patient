package com.shasthosheba.doctor.intermediary;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.shasthosheba.doctor.app.IntentTags;
import com.shasthosheba.doctor.databinding.RcvIntermediaryItemBinding;
import com.shasthosheba.doctor.model.User;

import java.util.ArrayList;
import java.util.List;

public class IntermediaryListAdapter extends RecyclerView.Adapter<IntermediaryListAdapter.DoctorViewHolder> {

    private List<User> mList = new ArrayList<>();
    private Context mContext;

    public IntermediaryListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public List<User> getmList() {
        return mList;
    }

    public User getDoc(int pos) {
        return mList.get(pos);
    }

    public void addAll(List<User> list) {
        this.mList.addAll(list);
    }

    public void clear() {
        this.mList.clear();
    }

    public void add(User user) {
        this.mList.add(user);
    }

    public void add(int pos, User user) {
        this.mList.add(pos, user);
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DoctorViewHolder(RcvIntermediaryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        User user = mList.get(position);
        holder.binding.tvName.setText(user.getName());
        holder.binding.tvStatus.setText(user.getStatus());
        int colorInt;
        if (user.getStatus().equals("online")) {
            colorInt = android.R.color.holo_green_light;
        } else {
            colorInt = android.R.color.holo_red_light;
        }
        holder.binding.tvStatus.setTextColor(ResourcesCompat.getColor(mContext.getResources(), colorInt, mContext.getTheme()));
        holder.binding.getRoot().setOnClickListener(v -> {
            if (user.getStatus().equals("online")) {
                mContext.startActivity(new Intent(mContext, IntermediaryDetailsActivity.class)
                        .putExtra(IntentTags.INTERMEDIARY_NAME.tag, user.getName())
                        .putExtra(IntentTags.INTERMEDIARY_UID.tag, user.getuId())
                        .putExtra(IntentTags.INTERMEDIARY_STATUS.tag, user.getStatus()));
            } else {
                Toast.makeText(mContext, "This org is not online right now", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        RcvIntermediaryItemBinding binding;

        public DoctorViewHolder(@NonNull RcvIntermediaryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
