package com.example.secure_e_wallet.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.secure_e_wallet.R;
import com.example.secure_e_wallet.SignInActivity;
import com.example.secure_e_wallet.databinding.FragmentPersonBinding;
import com.example.secure_e_wallet.model.User;
import com.example.secure_e_wallet.utilities.Constants;
import com.example.secure_e_wallet.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonFragment extends Fragment implements View.OnClickListener {

    private FragmentPersonBinding binding;
    private PreferenceManager preferenceManager;
    private User currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getActivity().getApplicationContext());
        currentUser = new User();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPersonBinding.inflate(inflater, container, false);
        //
        loadUserInfo();
        //
        binding.btnLogOut.setOnClickListener(this::onClick);
        //
        return binding.getRoot();
    }

    private void loadUserInfo() {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);

        if (userId != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection(Constants.KEY_COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            //
                            currentUser.id = documentSnapshot.getId();
                            currentUser.phoneNumber = documentSnapshot.getString(Constants.KEY_PHONE_NUMBER);
                            currentUser.username = documentSnapshot.getString(Constants.KEY_NAME);
                            currentUser.dob = documentSnapshot.getString(Constants.KEY_BIRTHDAY);
                            currentUser.gender = documentSnapshot.getString(Constants.KEY_GENDER);
                            currentUser.avatar = documentSnapshot.getString(Constants.KEY_AVATAR);
                            currentUser.balance = documentSnapshot.getString(Constants.KEY_BALANCE);

                            showDataOnViews(currentUser);
                        } else {
                            showToast(getString(R.string.failed_to_load_user_info));
                        }
                    })
                    .addOnFailureListener(e -> showToast(getString(R.string.failed_to_load_user_info)));
        }
    }

    private void showDataOnViews(User currentUser) {
        binding.imgViewAvatar.setImageBitmap(getImageFromEncodeString(currentUser.avatar));

        binding.txtUsername.setText(currentUser.username);

        binding.txtPhoneNumber.setText(currentUser.phoneNumber);
    }

    private Bitmap getImageFromEncodeString(String encodeImageString) {
        if (encodeImageString != null) {
            byte[] bytes = Base64.decode(encodeImageString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    @Override
    public void onClick(View view) {
        if (view == binding.btnLogOut) {
            // xoa du lieu phien nguoi dung
            preferenceManager.clear();

            //di chuyen toi activity signin va xoa toan bo cac activity trong stack
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void showToast(String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}