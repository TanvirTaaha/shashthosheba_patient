package com.shasthosheba.doctor.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestoreException;
import com.shasthosheba.doctor.model.DoctorProfile;
import com.shasthosheba.doctor.repo.DataOrError;
import com.shasthosheba.doctor.repo.Repository;

public class ProfileViewModel extends ViewModel {
    public String mDocId;

    public LiveData<DataOrError<DoctorProfile, FirebaseFirestoreException>> getDoctorProfile() {
        return Repository.getInstance().getDoctorProfile(mDocId);
    }

    public LiveData<DataOrError<Boolean, Exception>> setDoctorProfile(DoctorProfile doctorProfile) {
        return Repository.getInstance().setDoctorProfile(doctorProfile);
    }
}
