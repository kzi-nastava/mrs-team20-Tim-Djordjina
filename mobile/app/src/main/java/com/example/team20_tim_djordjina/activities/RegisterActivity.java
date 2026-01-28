package com.example.team20_tim_djordjina.activities;

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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.databinding.ActivityRegisterBinding;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private TextInputEditText etEmail, etPassword, etConfirmPassword, etFirstName, etLastName, etAddress;
    private EditText etPhoneNumber;
    private Spinner spinnerCountryCode;
    private Button btnSignUp;

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

        // Check if passwords match
        if(!password.equals(confirmPassword)){
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create full phone number
        String fullPhoneNumber = countryCode + phoneNumber;

        // Sign Up logic implementation
        Toast.makeText(this, "Sign Up Succesfull!", Toast.LENGTH_SHORT).show();
    }

    private boolean validateInputs(String firstName, String lastName, String email, String address, String phoneNumber, String password, String confirmPassword) {
        if(TextUtils.isEmpty(firstName)){
            Toast.makeText(this, "Please enter first name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(lastName)){
            Toast.makeText(this, "Please enter last name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(address)){
            Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(password.length() < 8){
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "Please confirm password", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Everthing is alright - return true
        return true;
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
    }
}