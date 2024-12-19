package com.example.secure_e_wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secure_e_wallet.databinding.ActivitySignUpBinding;
import com.example.secure_e_wallet.model.User;
import com.example.secure_e_wallet.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // initialize viewbinding
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //event
        binding.btnSignUp.setOnClickListener(this::onClick);
        binding.txtSignIn.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View view) {
        if(view == binding.txtSignIn){
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        }else if(view == binding.btnSignUp){
            //sign up action
            doSignUp();
        }
    }

    private void doSignUp() {
        String sPhoneNumber = String.valueOf(binding.edtPhoneNumber.getText());
        String sPassword = String.valueOf(binding.edtPassword.getText());
        String sConfirmPassword = String.valueOf(binding.edtConfirmPassword.getText());

        if(checkValidInput(sPhoneNumber, sPassword, sConfirmPassword)){
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_PHONE_NUMBER, sPhoneNumber)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                User newUser = new User();
                                newUser.phoneNumber = sPhoneNumber;
                                newUser.password = sPassword;
                                //go to edit user info screen
                                Intent intent = new Intent(this, EditInfoActivity.class);
                                intent.putExtra(Constants.KEY_USER, newUser);
                                startActivity(intent);
                                finish();
                            } else {
                                showToast("Số điện thoại đã tồn tại");
                            }
                        } else {
                        }
                    });
        }
    }

    private boolean checkValidInput(String sPhoneNumber, String sPassword, String sConfirmPassword) {
        if (sPhoneNumber == null || !sPhoneNumber.matches("\\d{10,15}")) {
            showToast("Invalid input: Phone number must be 10-15 digits.");
            return false;
        }
        if (sPassword == null || sPassword.length() < 6) {
            showToast("Invalid input: Password must be at least 6 characters.");
            return false;
        }
        if (!sPassword.equals(sConfirmPassword)) {
            showToast("Invalid input: Passwords do not match.");
            return false;
        }
        return true; // Hợp lệ
    }

    // Hàm hiển thị thông báo
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}