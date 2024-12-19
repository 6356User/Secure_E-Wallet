package com.example.secure_e_wallet.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Transaction implements Serializable {
    public String transactionId, senderId, senderName, receiverId, receiverName, amount, message, status;
    public Timestamp timestamp;

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", amount='" + amount + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
