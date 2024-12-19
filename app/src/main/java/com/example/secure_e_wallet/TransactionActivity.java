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

import com.example.secure_e_wallet.databinding.ActivityTransactionBinding;
import com.example.secure_e_wallet.model.Transaction;
import com.example.secure_e_wallet.utilities.Constants;
import com.example.secure_e_wallet.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class TransactionActivity extends AppCompatActivity implements View

        .OnClickListener {

    private ActivityTransactionBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // initialize viewbinding
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        preferenceManager = new PreferenceManager(getApplicationContext());

        binding.imgBtnBack.setOnClickListener(this::onClick);
        binding.btnTransaction.setOnClickListener(this::onClick);

        // Add FocusChangeListener to check receiver when EditText loses focus
        binding.edtReceiverAccount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) { // When user finishes editing and moves away
                String receiver = binding.edtReceiverAccount.getText().toString().trim();
                if (!receiver.isEmpty()) {
                    checkReceiverAccount(receiver);
                }
            }
        });

        // Add OnEditorActionListener to handle "Done" button on the keyboard
        binding.edtReceiverAccount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                String receiver = binding.edtReceiverAccount.getText().toString().trim();
                if (!receiver.isEmpty()) {
                    checkReceiverAccount(receiver);
                }
                return true; // Consume the event
            }
            return false;
        });
    }

    @Override
    public void onClick(View view) {
        if (view == binding.imgBtnBack) {
            finish();
        } else if (view == binding.btnTransaction) {
            //get input data
            String receiver = binding.edtReceiverAccount.getText().toString().trim();
            String amount = binding.edtAmount.getText().toString().trim();

            if (receiver.isEmpty() || amount.isEmpty()) {
                showToast(getString(R.string.please_enter_all_information));
            } else {
                checkUserBalance(amount);
            }
        }
    }

    private void checkUserBalance(String amount) {
        // Get the currently logged-in user
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        // Query the user's document in Firestore
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        // Get the balance field from the user's document
                        Double balance = Double.valueOf(document.getString(Constants.KEY_BALANCE));
                        Double compare = balance - Double.valueOf(amount);

                        if (compare < 0) {
                            showToast("Số dư không đủ để thực hiện giao dịch!");
                        } else {
                            String receiverPhone = binding.edtReceiverAccount.getText().toString().trim();
                            firestore.collection(Constants.KEY_COLLECTION_USERS)
                                    .whereEqualTo(Constants.KEY_PHONE_NUMBER, receiverPhone)
                                    .get()
                                    .addOnCompleteListener(receiverTask -> {
                                        if (receiverTask.isSuccessful() && !receiverTask.getResult().isEmpty()) {
                                            DocumentSnapshot receiverDoc = receiverTask.getResult().getDocuments().get(0);

                                            // Tạo đối tượng giao dịch
                                            Transaction transaction = new Transaction();
                                            transaction.senderId = userId;
                                            transaction.senderName = document.getString(Constants.KEY_NAME);
                                            transaction.receiverId = receiverDoc.getId();
                                            transaction.receiverName = receiverDoc.getString(Constants.KEY_NAME);
                                            transaction.amount = amount;
                                            transaction.message = binding.edtMessage.getText().toString().trim();
//                                            transaction.status = Constants.TRANSACTION_STATUS_PENDING;

                                            // Chuyển đến màn hình xác nhận giao dịch
                                            Intent intent = new Intent(this, ConfirmTransactionActivity.class);
                                            intent.putExtra(Constants.KEY_TRANSACTION, transaction);
                                            startActivity(intent);
                                        } else {
                                            showToast("Tài khoản người nhận không hợp lệ!");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        showToast("Lỗi khi kiểm tra tài khoản người nhận!");
                                    });
                        }
                    } else {
                        showToast("Lỗi khi truy vấn dữ liệu!");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast(e.getMessage());
                });
    }


    private void checkReceiverAccount(String receiver) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_PHONE_NUMBER, receiver)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // If the account exists, get the username and display it
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String username = document.getString(Constants.KEY_NAME);
                        binding.txtReceiverAccount.setVisibility(View.VISIBLE);
                        binding.txtReceiverAccount.setText(username);
                    } else {
                        // If the account does not exist, show an error message
                        binding.txtReceiverAccount.setVisibility(View.GONE);
                        Toast.makeText(this, "Người nhận không hợp lệ!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi kiểm tra tài khoản!", Toast.LENGTH_SHORT).show();
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}