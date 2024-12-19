package com.example.secure_e_wallet.model;

import java.io.Serializable;

public class User implements Serializable {
    public String id, phoneNumber, password, username, dob, gender, avatar, balance;

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", dob='" + dob + '\'' +
                ", gender='" + gender + '\'' +
                ", avatar='" + avatar + '\'' +
                ", balance='" + balance + '\'' +
                '}';
    }
}
