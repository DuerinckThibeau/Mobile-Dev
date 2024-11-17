package com.example.rentapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.text.method.PasswordTransformationMethod

class RegisterActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var togglePassword: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        registerButton = findViewById(R.id.registerButton)
        backButton = findViewById(R.id.backButton)
        togglePassword = findViewById(R.id.togglePassword)

        backButton.setOnClickListener {
            finish()
        }

        registerButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registration successful, navigate to profile setup
                        startActivity(Intent(this, ProfileSetupActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Set up password visibility toggles
        togglePassword.setOnClickListener {
            val isPasswordVisible = passwordInput.transformationMethod == null
            val newTransformation = if (isPasswordVisible) {
                togglePassword.setImageResource(R.drawable.ic_eye_off)
                PasswordTransformationMethod.getInstance()
            } else {
                togglePassword.setImageResource(R.drawable.ic_eye)
                null
            }
            
            // Apply to both password fields
            passwordInput.transformationMethod = newTransformation
            confirmPasswordInput.transformationMethod = newTransformation
            
            // Maintain cursor position
            passwordInput.setSelection(passwordInput.text.length)
            confirmPasswordInput.setSelection(confirmPasswordInput.text.length)
        }
    }
} 