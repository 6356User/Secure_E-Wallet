package com.example.secure_e_wallet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secure_e_wallet.R;
import com.example.secure_e_wallet.model.Transaction;
import com.example.secure_e_wallet.utilities.Constants;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList; // Danh sách giao dịch
    private final Context context; // Context để inflate layout

    // Constructor
    public TransactionHistoryAdapter(Context context) {
        this.context = context;
    }

    // Cập nhật dữ liệu cho adapter
    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
        notifyDataSetChanged(); // Thông báo dữ liệu thay đổi
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Set data cho các TextView
        holder.tvSender.setText("Sender: " + transaction.senderName);
        holder.tvReceiver.setText("Receiver: " + transaction.receiverName);
        holder.tvAmount.setText("Amount: " + transaction.amount);
        holder.tvStatus.setText(transaction.status);

        // Thay đổi màu status theo trạng thái
        if (transaction.status.equalsIgnoreCase(Constants.TRANSACTION_STATUS_SUCCESS)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else if (transaction.status.equalsIgnoreCase(Constants.TRANSACTION_STATUS_PENDING)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        } else if (transaction.status.equalsIgnoreCase(Constants.TRANSACTION_STATUS_FAILED)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    // ViewHolder class để ánh xạ các thành phần trong layout
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvReceiver, tvAmount, tvStatus;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvReceiver = itemView.findViewById(R.id.tvReceiver);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
