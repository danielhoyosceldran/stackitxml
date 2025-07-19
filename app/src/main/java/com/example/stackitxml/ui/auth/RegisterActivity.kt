package com.example.stackitxml.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stackitxml.R
import com.example.stackitxml.data.repository.FirestoreRepository
import com.example.stackitxml.ui.home.HomeActivity // Encara no existeix, la crearem aviat
import kotlinx.coroutines.launch

class    RegisterActivity : AppCompatActivity() {

    // Objecte per a recuperar dades de Firestore
    // Conté totes les funcions necessàries
    private val firestoreRepository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Carreguem els elements de la UI
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val registerButton: Button = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Si us plau, omple tots els camps.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // De forma asíncrona (corutina) registrem l'usuari
            lifecycleScope.launch {
                val result = firestoreRepository.registerUser(email, password, username)
                result.onSuccess { user ->
                    // Utilitzem this@RegisterActivity per indicar el context corrent (no podem fer this perquè estem a un altre fil d'execució)
                    Toast.makeText(this@RegisterActivity, "Registre exitós per a ${user.username}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                    finish()
                }.onFailure { exception ->
                    Toast.makeText(this@RegisterActivity, "Error de registre: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
