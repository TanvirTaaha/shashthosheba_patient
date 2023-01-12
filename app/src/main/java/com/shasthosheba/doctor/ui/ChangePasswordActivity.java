package com.shasthosheba.doctor.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.shasthosheba.doctor.R;
import com.shasthosheba.doctor.databinding.ActivityChangePasswordBinding;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    private boolean timerFinished = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        binding.btnConfirmPass.setOnClickListener(v -> {
            v.setEnabled(false);
            if (!timerFinished) {
                Timber.d("Timer is not finished");
                binding.tvWaitForAWhile.setVisibility(View.VISIBLE);
                return;
            }
            String txOldPass = Objects.requireNonNull(binding.tietOldPass.getText()).toString();
            if (TextUtils.isEmpty(txOldPass)) {
                Timber.d("old pass empty");
                binding.tilOldPass.setError("Enter the current password");
                v.setEnabled(true);
                return;
            }
            binding.tilOldPass.setErrorEnabled(false);
            String txNewPass = Objects.requireNonNull(binding.tietNewPass.getText()).toString();
            String txNewPassConfirm = Objects.requireNonNull(binding.tietNewPassConfirm.getText()).toString();
            if (txNewPass.isEmpty() || txNewPass.length() < 6) {
                Timber.d("new pass empty or too short");
                binding.tilNewPass.setError("Password is too short(min. 6 characters)");
                v.setEnabled(true);
                return;
            }
            binding.tilNewPass.setErrorEnabled(false);
            if (!txNewPass.equals(txNewPassConfirm)) {
                Timber.d("confirm pass doesn't match");
                binding.tilNewPassConfirm.setError("Password doesn't match");
                v.setEnabled(true);
                return;
            }
            binding.tilNewPassConfirm.setErrorEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.tvTooManyRetries.setVisibility(View.GONE);
            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(firebaseUser.getEmail()), txOldPass);
            firebaseUser.reauthenticate(credential).addOnCompleteListener(ChangePasswordActivity.this, task -> {
                if (task.isSuccessful()) {
                    Timber.d("old password matched. reAuthentication done");
                    firebaseUser.updatePassword(txNewPass).addOnCompleteListener(ChangePasswordActivity.this, task1 -> {
                        v.setEnabled(true);
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        if (task1.isSuccessful()) {
                            Toast.makeText(ChangePasswordActivity.this.getApplicationContext(), "Password changed", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ChangePasswordActivity.this.getApplicationContext(), "Something went wrong. Password not updated", Toast.LENGTH_LONG).show();
                            Timber.e(task1.getException());
                        }
                        ChangePasswordActivity.this.finish();
                    });
                } else {
                    Timber.d("old password didn't match. reAuthentication failed");
                    Timber.e(task.getException());
                    v.setEnabled(true);
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    if (task.getException() instanceof FirebaseTooManyRequestsException) {
                        binding.tvTooManyRetries.setText(R.string.too_many_retries);
                        binding.tvTooManyRetries.setVisibility(View.VISIBLE);
                    } else if (task.getException() instanceof FirebaseAuthException) {
//                        binding.tvTooManyRetries.setText(R.string.this_email_and_password_does_not_match);
//                        binding.tvTooManyRetries.setVisibility(View.VISIBLE);
                        binding.tilOldPass.setError("Wrong current password.");
                    }
                }
                Timber.d("reAuth task completed starting timer");
                startTimer(5);
            });

        });

        binding.btnCancel.setOnClickListener(v -> finish());

    }

    private void startTimer(long seconds) {
        Timber.d("Starting timer for %s sec", seconds);
        if (!timerFinished) return;
        timerFinished = false;
        new CountDownTimer(TimeUnit.SECONDS.toMillis(seconds), 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerFinished = false;
                Timber.d("onTick:%s", ((millisUntilFinished / 500) + 1));
                binding.tvWaitForAWhile.setText(String.format(Locale.getDefault(), "wait for %s", ((millisUntilFinished / 500) + 1)));
            }

            @Override
            public void onFinish() {
                timerFinished = true;
                binding.tvWaitForAWhile.setText("");
                binding.tvWaitForAWhile.setVisibility(View.GONE);
                binding.btnConfirmPass.setEnabled(true);
            }
        }.start();
    }
}