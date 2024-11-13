package com.example.rentapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class LoginActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var forgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        forgotPassword = findViewById(R.id.forgotPassword)

        // Set click listeners
        loginButton.setOnClickListener {
            // Handle login
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            // Implement your login logic here
        }

        registerButton.setOnClickListener {
            // Handle registration navigation
            // Implement your registration navigation here
        }

        forgotPassword.setOnClickListener {
            // Handle forgot password
            // Implement your forgot password logic here
        }
    }
} 