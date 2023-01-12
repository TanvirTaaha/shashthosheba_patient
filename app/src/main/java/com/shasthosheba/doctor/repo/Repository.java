package com.shasthosheba.doctor.repo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.text.BoringLayout;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.shasthosheba.doctor.app.App;
import com.shasthosheba.doctor.app.PublicVariables;
import com.shasthosheba.doctor.model.ChamberMember;
import com.shasthosheba.doctor.model.DoctorProfile;
import com.shasthosheba.doctor.model.Intermediary;

import java.util.List;

public final class Repository {
    private static Repository mInstance;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore fireStore;

    public static void initialize() {
        mInstance = new Repository();
    }

    private Repository() {
        firebaseDatabase = FirebaseDatabase.getInstance(PublicVariables.FIREBASE_DB);
        fireStore = FirebaseFirestore.getInstance();
        ConnectivityManager conMan = (ConnectivityManager) App.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        //https://stackoverflow.com/q/25678216
        // https://stackoverflow.com/q/70324348
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            //https://stackoverflow.com/q/25678216
            @Override
            public void onAvailable(@NonNull Network network) {
                netAvailable.postValue(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                // https://stackoverflow.com/q/70324348
                netAvailable.postValue(false);
            }

            @Override
            public void onUnavailable() {
                netAvailable.postValue(false);
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conMan.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            conMan.registerNetworkCallback(request, networkCallback);
        }
    }

    public static Repository getInstance() {
        return mInstance;
    }


    private final MutableLiveData<Boolean> netAvailable = new MutableLiveData<>();

    public LiveData<Boolean> getNetStatus() {
        return netAvailable;
    }

    public boolean isConnected() {
        return getConnectionType(App.getAppContext()) != 0;
    }

    /**
     * https://stackoverflow.com/a/53243938
     *
     * @param context Application context
     * @return 0: No Internet available (maybe on airplane mode, or in the process of joining an wi-fi).
     * 1: Cellular (mobile data, 3G/4G/LTE whatever).
     * 2: Wi-fi.
     * 3: VPN
     */
    @SuppressLint("ObsoleteSdkInt")
    @IntRange(from = 0, to = 3)
    public static int getConnectionType(Context context) {
        int result = 0; // Returns connection type. 0: none; 1: mobile data; 2: wifi; 3: vpn
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        result = 3;
                    }
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        result = 2;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        result = 1;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_VPN) {
                        result = 3;
                    }
                }
            }
        }
        return result;
    }

    public static FirebaseFirestore getFireStore() {
        return mInstance.fireStore;
    }

    public static FirebaseDatabase getFirebaseDatabase() {
        return mInstance.firebaseDatabase;
    }

    public LiveData<DataOrError<List<Intermediary>, FirebaseFirestoreException>> getAllIntermediaries() {
        return new FirestoreCollectionLiveData<>(fireStore.collection(PublicVariables.INTERMEDIARY_KEY), Intermediary.class);
    }

    private final MutableLiveData<DataOrError<Boolean, Exception>> setDocProfileLD = new MutableLiveData<>();

    public LiveData<DataOrError<Boolean, Exception>> setDoctorProfile(DoctorProfile doctorProfile) {
        fireStore.collection(PublicVariables.DOCTOR_KEY).document(doctorProfile.getDocId()).set(doctorProfile)
                .addOnCompleteListener(task ->
                        setDocProfileLD.postValue(new DataOrError<>(
                                task.isSuccessful(), task.getException())));
        return setDocProfileLD;
    }

    private FirestoreDocumentLiveData<DoctorProfile> getDoctorProfileLD;
    private String doctorId;

    public LiveData<DataOrError<DoctorProfile, FirebaseFirestoreException>> getDoctorProfile(String docId) {
        if (this.doctorId == null || !this.doctorId.equals(docId)) {
            this.doctorId = docId;
            getDoctorProfileLD = new FirestoreDocumentLiveData<>(
                    fireStore.collection(PublicVariables.DOCTOR_KEY).document(docId),
                    DoctorProfile.class);
        }
        return getDoctorProfileLD;
    }

    private FirebaseRealtimeListLiveData<ChamberMember> allChamberMembersLD;
    public LiveData<DataOrError<List<ChamberMember>, DatabaseException>> getAllChamberMembers() {
        if (allChamberMembersLD == null) {
            allChamberMembersLD = new FirebaseRealtimeListLiveData<>(firebaseDatabase.getReference(PublicVariables.CHAMBER_KEY), ChamberMember.class);
        }
        return allChamberMembersLD;
    }
}
