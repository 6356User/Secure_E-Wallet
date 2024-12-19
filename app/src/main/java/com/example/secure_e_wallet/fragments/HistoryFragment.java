package com.example.secure_e_wallet.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secure_e_wallet.adapters.TransactionHistoryAdapter;
import com.example.secure_e_wallet.databinding.FragmentHistoryBinding;
import com.example.secure_e_wallet.model.Transaction;
import com.example.secure_e_wallet.utilities.Constants;
import com.example.secure_e_wallet.utilities.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private TransactionHistoryAdapter adapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(getContext());
        database = FirebaseFirestore.getInstance();

        // Khởi tạo Adapter
        adapter = new TransactionHistoryAdapter(getActivity().getApplicationContext());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // Thiết lập TabLayout và dữ liệu
        setupTabLayout();

        return binding.getRoot();
    }

    private void setupTabLayout() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Chuyển tiền"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Nạp tiền"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Nhận tiền"));

        // Mặc định: hiển thị tab "Chuyển tiền"
        loadTransferTransactions();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadTransferTransactions();
                        break;
                    case 1:
                        loadDepositTransactions();
                        break;
                    case 2:
                        loadReceiveTransactions();
                        break;
                }
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) { }
        });
    }

    private void loadTransferTransactions() {
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

        database.collection(Constants.KEY_COLLECTION_TRANSACTIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, currentUserId)
                .orderBy(Constants.KEY_TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Transaction> transferTransactions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Transaction transaction = document.toObject(Transaction.class);
                            transferTransactions.add(transaction);
                        }
                        adapter.setTransactionList(transferTransactions);
                        Log.d("Firestore", "Transfer Transactions Loaded: " + transferTransactions.size());
                    } else {
                        Log.e("Firestore", "Error fetching transfer transactions", task.getException());
                    }
                });
    }

    private void loadReceiveTransactions() {
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

        database.collection(Constants.KEY_COLLECTION_TRANSACTIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, currentUserId)
                .orderBy(Constants.KEY_TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Transaction> receiveTransactions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Transaction transaction = document.toObject(Transaction.class);
                            receiveTransactions.add(transaction);
                        }
                        adapter.setTransactionList(receiveTransactions);
                        Log.d("Firestore", "Receive Transactions Loaded: " + receiveTransactions.size());
                    } else {
                        Log.e("Firestore", "Error fetching receive transactions", task.getException());
                    }
                });
    }

    private void loadDepositTransactions() {
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

        database.collection(Constants.KEY_COLLECTION_DEPOSITS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, currentUserId)
                .orderBy(Constants.KEY_TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Transaction> depositTransactions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Transaction transaction = document.toObject(Transaction.class);
                            depositTransactions.add(transaction);
                        }
                        adapter.setTransactionList(depositTransactions);
                        Log.d("Firestore", "Deposit Transactions Loaded: " + depositTransactions.size());
                    } else {
                        Log.e("Firestore", "Error fetching deposit transactions", task.getException());
                    }
                });
    }
}
