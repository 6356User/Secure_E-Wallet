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

import com.example.secure_e_wallet.databinding.ActivityConfirmTransactionBinding;
import com.example.secure_e_wallet.model.Transaction;
import com.example.secure_e_wallet.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ConfirmTransactionActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityConfirmTransactionBinding binding;
    private Transaction transaction;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // initialize viewbinding
        binding = ActivityConfirmTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestore = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        transaction = (Transaction) intent.getSerializableExtra(Constants.KEY_TRANSACTION);

        if (transaction != null) {
            binding.tvSender.setText(transaction.senderName);
            binding.tvReceiver.setText(transaction.receiverName);
            binding.tvAmount.setText(transaction.amount + " VND");
            if (transaction.message.equals("") || transaction.message.isEmpty()) {
                String message = String.format(getString(R.string.default_message_transaction),
                        transaction.senderName, transaction.receiverName);
                binding.tvMessage.setText(message);
                transaction.message = message;
            } else {
                binding.tvMessage.setText(transaction.message);
            }
        }

        binding.btnCancel.setOnClickListener(this::onClick);
        binding.btnConfirm.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View view) {
        if (view == binding.btnCancel) {
            showToast("Giao dịch đã bị hủy!");
            finish();
        } else if (view == binding.btnConfirm) {
            processTransaction();
        }
    }

    private void processTransaction() {
        // Set initial transaction status to PENDING
        transaction.status = Constants.TRANSACTION_STATUS_PENDING;
        saveTransactionToFirestore();

        // Start transaction process
        updateSenderBalance();
    }

    private void saveTransactionToFirestore() {
        HashMap<String, Object> transactionMap = new HashMap<>();
        transactionMap.put(Constants.KEY_SENDER_ID, transaction.senderId);
        transactionMap.put(Constants.KEY_SENDER_NAME, transaction.senderName);
        transactionMap.put(Constants.KEY_RECEIVER_ID, transaction.receiverId);
        transactionMap.put(Constants.KEY_RECEIVER_NAME, transaction.receiverName);
        transactionMap.put(Constants.KEY_AMOUNT, transaction.amount);
        transactionMap.put(Constants.KEY_MESSAGE, transaction.message);
        transactionMap.put(Constants.KEY_TIMESTAMP, System.currentTimeMillis());
        transactionMap.put(Constants.KEY_STATUS, transaction.status);

        // Lưu giao dịch lần đầu lên Firestore
        firestore.collection(Constants.KEY_COLLECTION_TRANSACTIONS)
                .add(transactionMap)
                .addOnSuccessListener(documentReference -> {
                    String generatedTransactionId = documentReference.getId();

                    // Cập nhật transactionId với ID của document
                    documentReference.update(Constants.KEY_TRANSACTION_ID, generatedTransactionId)
                            .addOnSuccessListener(aVoid -> {
                                transaction.transactionId = generatedTransactionId; // Cập nhật lại đối tượng transaction
                                showToast("Giao dịch đã được lưu thành công!");
                            })
                            .addOnFailureListener(e -> showToast("Lỗi khi cập nhật transactionId: " + e.getMessage()));
                })
                .addOnFailureListener(e -> showToast("Lỗi khi lưu giao dịch: " + e.getMessage()));
    }


    private void updateSenderBalance() {
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(transaction.senderId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        double currentBalance = Double.parseDouble(document.getString(Constants.KEY_BALANCE));
                        double transactionAmount = Double.parseDouble(transaction.amount);

                        if (currentBalance >= transactionAmount) {
                            // Update sender's balance
                            double updatedBalance = currentBalance - transactionAmount;
                            firestore.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(transaction.senderId)
                                    .update(Constants.KEY_BALANCE, String.valueOf(updatedBalance))
                                    .addOnSuccessListener(aVoid -> updateReceiverBalance())
                                    .addOnFailureListener(e -> updateTransactionStatus(Constants.TRANSACTION_STATUS_FAILED, "Lỗi khi cập nhật số dư người gửi: " + e.getMessage()));
                        } else {
                            updateTransactionStatus(Constants.TRANSACTION_STATUS_FAILED, "Số dư không đủ để thực hiện giao dịch!");
                        }
                    }
                })
                .addOnFailureListener(e -> updateTransactionStatus(Constants.TRANSACTION_STATUS_FAILED, "Lỗi khi lấy dữ liệu người gửi: " + e.getMessage()));
    }

    private void updateReceiverBalance() {
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(transaction.receiverId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        double currentBalance = Double.parseDouble(document.getString(Constants.KEY_BALANCE));
                        double transactionAmount = Double.parseDouble(transaction.amount);

                        // Update receiver's balance
                        double updatedBalance = currentBalance + transactionAmount;
                        firestore.collection(Constants.KEY_COLLECTION_USERS)
                                .document(transaction.receiverId)
                                .update(Constants.KEY_BALANCE, String.valueOf(updatedBalance))
                                .addOnSuccessListener(aVoid -> updateTransactionStatus(Constants.TRANSACTION_STATUS_SUCCESS, "Giao dịch thành công!"))
                                .addOnFailureListener(e -> updateTransactionStatus(Constants.TRANSACTION_STATUS_FAILED, "Lỗi khi cập nhật số dư người nhận: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> updateTransactionStatus(Constants.TRANSACTION_STATUS_FAILED, "Lỗi khi lấy dữ liệu người nhận: " + e.getMessage()));
    }

    private void updateTransactionStatus(String status, String message) {
        transaction.status = status;

        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put(Constants.KEY_STATUS, status);

        firestore.collection(Constants.KEY_COLLECTION_TRANSACTIONS)
                .document(transaction.transactionId)
                .update(updateMap)
                .addOnCompleteListener(task -> {
                    if (status.equals(Constants.TRANSACTION_STATUS_SUCCESS)) {
                        showToast(message);
                        finish();
                    } else {
                        showToast(message);
                    }
                })
                .addOnFailureListener(e -> showToast("Lỗi khi cập nhật trạng thái giao dịch: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
