package com.shasthosheba.doctor.ui.chamber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseException;
import com.shasthosheba.doctor.R;
import com.shasthosheba.doctor.app.App;
import com.shasthosheba.doctor.app.PreferenceManager;
import com.shasthosheba.doctor.databinding.ActivityChamberDoctorBinding;
import com.shasthosheba.doctor.databinding.RcvChamberMemberListItemBinding;
import com.shasthosheba.doctor.model.ChamberMember;
import com.shasthosheba.doctor.repo.DataOrError;
import com.shasthosheba.doctor.repo.Repository;

import java.util.List;

import timber.log.Timber;

public class ChamberActivityDoctor extends AppCompatActivity {

    private ActivityChamberDoctorBinding binding;
    private ChamberViewModel mViewModel;
    private ChamberMemberAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChamberDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mViewModel = new ViewModelProvider(this).get(ChamberViewModel.class);
        mViewModel.getAllChamberMembers().observe(this, dataOrError -> {
            if (dataOrError.data != null) {
                Timber.d("inside getAllChamberMembers observer:data:%s", dataOrError.data);
                mAdapter.submitList(dataOrError.data);
            }
            Timber.e(dataOrError.error);
            if (binding.rcvChamberMemberList.getChildCount() == 0 && mAdapter.getItemCount() == 0) {
                binding.rcvChamberMemberList.setVisibility(View.GONE);
                binding.llEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rcvChamberMemberList.setVisibility(View.VISIBLE);
                binding.llEmpty.setVisibility(View.GONE);
            }
        });

        mAdapter = new ChamberMemberAdapter(
                new PreferenceManager(this).getUser().getuId(),
                new DiffUtil.ItemCallback<ChamberMember>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull ChamberMember oldItem, @NonNull ChamberMember newItem) {
                        Timber.d("areItemsTheSame:%s", oldItem.equals(newItem));
                        return oldItem.equals(newItem);
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull ChamberMember oldItem, @NonNull ChamberMember newItem) {
                        Timber.d("areContentsTheSame:%s", oldItem.equals(newItem));
                        return oldItem.equals(newItem);
                    }
                });
        binding.rcvChamberMemberList.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvChamberMemberList.setAdapter(mAdapter);
    }

    public static class ChamberMemberAdapter extends ListAdapter<ChamberMember, ChamberMemberAdapter.ViewHolder> {
        private String uId;

        protected ChamberMemberAdapter(String uId, @NonNull DiffUtil.ItemCallback<ChamberMember> diffCallback) {
            super(diffCallback);
            this.uId = uId;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(RcvChamberMemberListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChamberMember member = getItem(position);
            int backgroundDrawableId = R.drawable.rounded_rect_bg_selector;
            if (member.getIntermediaryId().equals(uId)) {
                backgroundDrawableId = R.drawable.rounded_rect_bg_selector_marked;
            }
            holder.binding.flRoot.setBackground(ContextCompat.getDrawable(holder.binding.getRoot().getContext(), backgroundDrawableId));
            holder.binding.tvName.setText(member.getName());
            if (member.isWithPayment()) {
                holder.binding.tvTransactionId.setVisibility(View.VISIBLE);
                holder.binding.tvBkashNo.setVisibility(View.VISIBLE);
                holder.binding.tvTransactionId.setText(App.getAppContext().getString(R.string.txn, member.getTransactionId()));
                holder.binding.tvBkashNo.setText(App.getAppContext().getString(R.string.bkash_with_param, member.getMemberBKashNo()));
            } else {
                holder.binding.tvTransactionId.setVisibility(View.GONE);
                holder.binding.tvBkashNo.setVisibility(View.GONE);
            }

        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            RcvChamberMemberListItemBinding binding;

            public ViewHolder(@NonNull RcvChamberMemberListItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Timber.d("onOptionsItemSelected: android.R.id.home");
            onBackPressed();
        }
        return true;
    }

    public static class ChamberViewModel extends ViewModel {
        public LiveData<DataOrError<List<ChamberMember>, DatabaseException>> getAllChamberMembers() {
            return Repository.getInstance().getAllChamberMembers();
        }
    }
}