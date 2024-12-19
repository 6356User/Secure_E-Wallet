package com.example.secure_e_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secure_e_wallet.databinding.ActivitySignInBinding;
import com.example.secure_e_wallet.utilities.Constants;
import com.example.secure_e_wallet.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // initialize viewbinding
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        preferenceManager = new PreferenceManager(getApplicationContext());
        //event
        binding.btnSignIn.setOnClickListener(this::onClick);
        binding.txtSignUp.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View view) {
        if (view == binding.txtSignUp) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            finish();
        } else if (view == binding.btnSignIn) {
            //log in action
            ActionSignIn();
        }
    }

    private void ActionSignIn() {
        String phoneNumber = binding.edtPhoneNumber.getText().toString();
        String password = binding.edtPassword.getText().toString();

        if (isValidInputs(phoneNumber, password)) {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_PHONE_NUMBER, phoneNumber)
                    .whereEqualTo(Constants.KEY_PASSWORD, password)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            doSignIn(documentSnapshot);
                        } else {
                            showToast(getString(R.string.wrong_phone_or_password));
                        }
                    });
        }
    }


    private void doSignIn(DocumentSnapshot documentSnapshot) {
        // Lưu thông tin người dùng vào SharedPreferences
        preferenceManager.putBoolean(Constants.KEY_IS_SIGN_IN, true);
        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());

        // Điều hướng tới màn hình chính
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private boolean isValidInputs(String phoneNumber, String password) {
        if (phoneNumber.isEmpty()) {
            showToast(getString(R.string.enter_phone_number));
            return false;
        }
        if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            showToast(getString(R.string.invalid_phone_number));
            return false;
        }
        if (password.isEmpty()) {
            showToast(getString(R.string.enter_password));
            return false;
        }
        if (password.length() < 6) {
            showToast(getString(R.string.password_too_short));
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}