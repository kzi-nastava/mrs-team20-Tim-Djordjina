package com.example.team20_tim_djordjina.activities;

import android.app.AlertDialog;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import android.net.Uri;
import android.widget.ImageView;
import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityRegisterBinding;
import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.RegistrationRequest;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private TextInputEditText etEmail, etPassword, etConfirmPassword, etFirstName, etLastName, etAddress;
    private EditText etPhoneNumber;
    private Spinner spinnerCountryCode;
    private Button btnSignUp;
    private ImageView ivProfileImage, ivEditPhoto;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<Intent> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) {
                                selectedImageUri = uri;
                                ivProfileImage.setImageURI(uri);
                            }
                        }
                    }
            );



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Team20_Tim_Djordjina", "Register Activity onCreate()");
        //EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
         */

        // Initialize views
        initViews();

        View.OnClickListener openImagePicker = v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            imagePicker.launch(intent);
        };
        ivProfileImage.setOnClickListener(openImagePicker);
        ivEditPhoto.setOnClickListener(openImagePicker);

        // Setup country code spinner
        setupCountryCodeSpinner();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSignUp();
            }
        });


    }

    private void handleSignUp(){
        // Get input values
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String countryCode = spinnerCountryCode.getSelectedItem().toString().trim();

        // Validate inputs
        if(!validateInputs(firstName, lastName, email, address, phoneNumber, password, confirmPassword)){
            return;
        }

        if (selectedImageUri != null) {
            Log.i("Register", "User selected image: " + selectedImageUri);
        } else {
            Log.i("Register", "Using default avatar");
        }


        // Check if passwords match
        if(!password.equals(confirmPassword)){
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create full phone number
        String fullPhoneNumber = countryCode + phoneNumber;

        // Create registration request
        RegistrationRequest request = new RegistrationRequest(
                firstName,
                lastName,
                email,
                password,
                confirmPassword,
                fullPhoneNumber,
                address
        );

        // Call API
        registerUser(request, email);

        // Sign Up logic implementation
        //Toast.makeText(this, "Sign Up Succesfull!", Toast.LENGTH_SHORT).show();
    }

    private void registerUser(RegistrationRequest request, String email){
        btnSignUp.setEnabled(false);

        RetrofitClient.getInstance(this).getApiService().register(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnSignUp.setEnabled(true);

                if (response.isSuccessful() && response.body() != null){
                    ApiResponse apiResponse = response.body();
                    if(apiResponse.isSuccess()){
                        // Registration successfull -> tell user to activate via email, then go to login
                        new AlertDialog.Builder(RegisterActivity.this)
                                .setTitle("Check your email")
                                .setMessage("Registration successful! We have sent an activation link to "
                                        + email + ". Please activate your account within 24 hours, " +
                                        "then log in.")
                                .setCancelable(false)
                                .setPositiveButton("Go to login", (dialog, which) -> {
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                })
                                .show();
                    } else{
                        // Registration failed
                        Toast.makeText(RegisterActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    String errorMessage = "Registration failed. Please try again.";
                    if (response.code() == 409){
                        errorMessage = "Email already exists.";
                    } else if (response.code() == 400) {
                        errorMessage = "Invalid input data.";
                    }
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnSignUp.setEnabled(true);

                // Network error
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs(String firstName, String lastName, String email, String address, String phoneNumber, String password, String confirmPassword) {
        if(TextUtils.isEmpty(firstName)){
            showError("Please enter first name");
            //Toast.makeText(this, "Please enter first name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(lastName)){
            showError("Please enter last name");
            //Toast.makeText(this, "Please enter last name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(email)){
            showError("Please enter email");
            //Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showError("Please enter a valid email");
            //Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(address)){
            showError("Please enter address");
            //Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(phoneNumber)){
            showError("Please enter phone number");
            //Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(password)){
            showError("Please enter password");
            //Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(password.length() < 8){
            showError("Password must be at least 8 characters");
            //Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(confirmPassword)){
            showError("Please confirm password");
            //Toast.makeText(this, "Please confirm password", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Everthing is alright - return true
        return true;
    }

    private void showError(String message){
        binding.tvRegisterError.setText(message);
        binding.tvRegisterError.setVisibility(View.VISIBLE);
    }
    private void setupCountryCodeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.country_codes,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapter);
        // Set default selection for +381
        spinnerCountryCode.setSelection(0);
    }

    private void initViews() {
        etEmail = binding.etEmail;
        etPassword = binding.etPassword;
        etConfirmPassword = binding.etConfirmPassword;
        etFirstName = binding.etFirstName;
        etLastName = binding.etLastName;
        etAddress = binding.etAddress;
        etPhoneNumber = binding.etPhoneNumber;
        spinnerCountryCode = binding.spinnerCountryCode;
        btnSignUp = binding.btnSignUp;
        ivProfileImage = binding.ivProfileImage;
        ivEditPhoto = binding.ivEditPhoto;
    }
}