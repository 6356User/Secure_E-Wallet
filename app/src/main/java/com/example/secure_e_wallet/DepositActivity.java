package com.example.secure_e_wallet;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secure_e_wallet.databinding.ActivityDepositBinding;
import com.example.secure_e_wallet.utilities.Constants;
import com.example.secure_e_wallet.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class DepositActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityDepositBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize view binding
        binding = ActivityDepositBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preferenceManager = new PreferenceManager(getApplicationContext());
        firestore = FirebaseFirestore.getInstance();

        binding.imgBtnBack.setOnClickListener(this::onClick);
        binding.btnDeposit.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View view) {
        if (view == binding.imgBtnBack) {
            finish();
        } else if (view == binding.btnDeposit) {
            String amount = binding.edtAmount.getText().toString().trim();

            if (amount.isEmpty()) {
                showToast("Vui lòng nhập số tiền muốn nạp!");
                return;
            }

            createDepositTransaction(amount);
        }
    }

    private void createDepositTransaction(String amount) {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        String userName = preferenceManager.getString(Constants.KEY_NAME);

        HashMap<String, Object> depositData = new HashMap<>();
        depositData.put(Constants.KEY_RECEIVER_ID, userId);
        depositData.put(Constants.KEY_AMOUNT, amount);
        depositData.put(Constants.KEY_MESSAGE, "Nạp tiền vào ví");
        depositData.put(Constants.KEY_TIMESTAMP, System.currentTimeMillis());
        depositData.put(Constants.KEY_STATUS, Constants.TRANSACTION_STATUS_PENDING);

        // Save transaction to Firestore
        firestore.collection(Constants.KEY_COLLECTION_DEPOSITS)
                .add(depositData)
                .addOnSuccessListener(documentReference -> {
                    // Update transaction ID
                    String transactionId = documentReference.getId();
                    firestore.collection(Constants.KEY_COLLECTION_DEPOSITS)
                            .document(transactionId)
                            .update(Constants.KEY_TRANSACTION_ID, transactionId)
                            .addOnSuccessListener(aVoid -> updateUserBalance(amount, transactionId))
                            .addOnFailureListener(e -> updateTransactionStatus(transactionId, Constants.TRANSACTION_STATUS_FAILED, "Lỗi khi lưu transactionId: " + e.getMessage()));
                })
                .addOnFailureListener(e -> showToast("Lỗi khi tạo giao dịch: " + e.getMessage()));
    }

    private void updateUserBalance(String amount, String transactionId) {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);

        // Get current balance of the user
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        double currentBalance = Double.parseDouble(document.getString(Constants.KEY_BALANCE));
                        double depositAmount = Double.parseDouble(amount);

                        // Update balance
                        double updatedBalance = currentBalance + depositAmount;
                        firestore.collection(Constants.KEY_COLLECTION_USERS)
                                .document(userId)
                                .update(Constants.KEY_BALANCE, String.valueOf(updatedBalance))
                                .addOnSuccessListener(aVoid -> updateTransactionStatus(transactionId, Constants.TRANSACTION_STATUS_SUCCESS, null))
                                .addOnFailureListener(e -> updateTransactionStatus(transactionId, Constants.TRANSACTION_STATUS_FAILED, "Lỗi khi cập nhật số dư: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> updateTransactionStatus(transactionId, Constants.TRANSACTION_STATUS_FAILED, "Lỗi khi lấy dữ liệu người dùng: " + e.getMessage()));
    }

    private void updateTransactionStatus(String transactionId, String status, String errorMessage) {
        firestore.collection(Constants.KEY_COLLECTION_DEPOSITS)
                .document(transactionId)
                .update(Constants.KEY_STATUS, status)
                .addOnSuccessListener(aVoid -> {
                    if (status.equals(Constants.TRANSACTION_STATUS_SUCCESS)) {
                        showToast("Nạp tiền thành công!");
                        finish();
                    } else {
                        showToast(errorMessage != null ? errorMessage : "Giao dịch thất bại!");
                    }
                })
                .addOnFailureListener(e -> showToast("Lỗi khi cập nhật trạng thái giao dịch: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
