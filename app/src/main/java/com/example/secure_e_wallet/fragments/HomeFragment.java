package com.example.secure_e_wallet.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.secure_e_wallet.DepositActivity;
import com.example.secure_e_wallet.R;
import com.example.secure_e_wallet.TransactionActivity;
import com.example.secure_e_wallet.adapters.MenuFunctionsAdapter;
import com.example.secure_e_wallet.databinding.FragmentHomeBinding;
import com.example.secure_e_wallet.interfaces.OnMenuItemClickListener;
import com.example.secure_e_wallet.model.MenuItem;
import com.example.secure_e_wallet.model.User;
import com.example.secure_e_wallet.utilities.Constants;
import com.example.secure_e_wallet.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements OnMenuItemClickListener {

    private FragmentHomeBinding binding;
    private PreferenceManager preferenceManager;
    private User currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        //
        preferenceManager = new PreferenceManager(getActivity().getApplicationContext());
        currentUser = new User();
        //
        setRecyclerView();
        loadUserInfo();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
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
        binding.txtUsername.setText(currentUser.username);
        binding.txtAccount.setText(currentUser.phoneNumber);
        binding.txtBalance.setText(currentUser.balance + " VND");
    }

    private void setRecyclerView() {
        binding.recyclerFunctions.setLayoutManager(new GridLayoutManager(getContext(), 3));

        List<MenuItem> menuItems = getMenuItems(getContext(), R.menu.menu_app_functions);
        MenuFunctionsAdapter adapter = new MenuFunctionsAdapter(getContext(), menuItems, this);
        binding.recyclerFunctions.setAdapter(adapter);
    }

    public static List<MenuItem> getMenuItems(Context context, int menuResId) {
        List<MenuItem> menuItems = new ArrayList<>();
        try (XmlResourceParser parser = context.getResources().getXml(menuResId)) {
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
                    int id = parser.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "id", 0);
                    int iconRes = parser.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "icon", 0);
                    String titleValue = parser.getAttributeValue("http://schemas.android.com/apk/res/android", "title");

                    String title = (titleValue != null && titleValue.startsWith("@")) ?
                            context.getString(Integer.parseInt(titleValue.substring(1))) : titleValue;

                    Drawable icon = (iconRes != 0) ? context.getDrawable(iconRes) : null;
                    menuItems.add(new MenuItem(id, title, icon));
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error parsing menu XML", e);
        }
        return menuItems;
    }

    @Override
    public void onMenuItemClick(MenuItem menuItem) {
        Intent intent;
        int itemId = menuItem.getId();

        if (itemId == R.id.item_banking) {
            intent = new Intent(getContext(), TransactionActivity.class);
        } else {
            intent = new Intent(getContext(), DepositActivity.class);
        }

        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}