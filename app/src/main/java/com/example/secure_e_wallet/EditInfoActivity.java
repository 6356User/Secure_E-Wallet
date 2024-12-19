package com.example.secure_e_wallet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.secure_e_wallet.databinding.ActivityEditInfoBinding;
import com.example.secure_e_wallet.model.User;
import com.example.secure_e_wallet.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private User user;
    private ActivityEditInfoBinding binding;
    private String encodeAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // initialize viewbinding
        binding = ActivityEditInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra(Constants.KEY_USER);
        if (user.id == null) {
            binding.imgBtnBack.setVisibility(View.GONE);
        }

        binding.btnSave.setOnClickListener(this::onClick);
        binding.imgBtnBack.setOnClickListener(this::onClick);
        binding.imgCalendar.setOnClickListener(this::onClick);
        binding.imgViewAvatar.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View view) {
        if (view == binding.imgCalendar) {
            showDatePicker();
        } else if (view == binding.imgBtnBack) {
            finish();
        } else if (view == binding.imgViewAvatar) {
            openGallery();
        } else if (view == binding.btnSave) {
            saveUserData();
        }
    }

    private void saveUserData() {
        // Lấy dữ liệu từ EditText (tên người dùng)
        String username = binding.edtUsername.getText().toString().trim();
        if (username.isEmpty()) {
            binding.edtUsername.setError("Username is required");
            return;
        }

        // Lấy ngày sinh từ TextView
        String dob = binding.txtDob.getText().toString().trim();
        if (dob.isEmpty()) {
            binding.txtDob.setError("Date of birth is required");
            return;
        }

        // Lấy giới tính từ RadioGroup
        int selectedGenderId = binding.radioGender.getCheckedRadioButtonId();
        String gender = selectedGenderId == R.id.radio_male ? "Male" : "Female";

        // Chuyển đổi hình ảnh sang Base64
        String base64Avatar = encodeAvatar;

        user.username = username;
        user.dob = dob;
        user.gender = gender;
        user.avatar = base64Avatar;

        if (user.id == null) {
            createNewUser();
        } else {
            updateUserInfo();
        }
    }

    private void updateUserInfo() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.id)
                .update(
                        Constants.KEY_NAME, user.username,
                        Constants.KEY_BIRTHDAY, user.dob,
                        Constants.KEY_GENDER, user.gender,
                        Constants.KEY_AVATAR, user.avatar
                )
                .addOnSuccessListener(unused -> {
                    showToast("Cập nhật thông tin thành công!");
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Cập nhật thông tin thất bại, vui lòng thử lại!");
                });
    }


    private void createNewUser() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        // Tạo một HashMap để lưu thông tin user
        Map<String, Object> userMap = new HashMap<>();
        userMap.put(Constants.KEY_NAME, user.username);
        userMap.put(Constants.KEY_PASSWORD, user.password);
        userMap.put(Constants.KEY_AVATAR, user.avatar);
        userMap.put(Constants.KEY_PHONE_NUMBER, user.phoneNumber);
        userMap.put(Constants.KEY_BIRTHDAY, user.dob);
        userMap.put(Constants.KEY_GENDER, user.gender);
        userMap.put(Constants.KEY_BALANCE, "0"); // Giá trị mặc định cho balance

        // Thêm tài liệu mới vào Firestore
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .add(userMap)
                .addOnSuccessListener(documentReference -> {
                    // Lấy documentId và cập nhật userID
                    String documentId = documentReference.getId();
                    documentReference.update(Constants.KEY_USER_ID, documentId)
                            .addOnSuccessListener(unused -> {
                                // Cập nhật user.id trong đối tượng User để sử dụng trong ứng dụng
                                user.id = documentId;

                                showToast("Tạo người dùng thành công!");
                                //
                                Intent intent = new Intent(this, SignInActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showToast("Cập nhật userID thất bại!");
                            });
                })
                .addOnFailureListener(e -> {
                    showToast("Không thể tạo người dùng. Vui lòng thử lại!");
                });
    }




    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickImage.launch(intent);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imgViewAvatar.setImageBitmap(bitmap);
                            encodeAvatar = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 1000;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitMap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitMap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void showDatePicker() {
        // Lấy ngày hiện tại
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Hiển thị DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Định dạng ngày/tháng/năm
                    String formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    // Hiển thị ngày đã chọn lên TextView
                    binding.txtDob.setText(formattedDate);
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    // Hàm hiển thị thông báo
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}