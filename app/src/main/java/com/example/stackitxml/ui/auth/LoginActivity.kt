package com.example.stackitxml.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stackitxml.R
import com.example.stackitxml.data.repository.FirestoreRepository
import com.example.stackitxml.ui.home.HomeActivity
import kotlinx.coroutines.launch
import androidx.core.app.ActivityOptionsCompat

class LoginActivity : AppCompatActivity() {
    // Objecte per a recuperar dades de Firestore
    // Conté totes les funcions necessàries
    private val firestoreRepository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Carreguem els elements de la UI
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val registerTextView: TextView = findViewById(R.id.registerTextView)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Si us plau, introdueix el correu electrònic i la contrasenya.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // De forma asíncrona (corutina) registrem l'usuari
            lifecycleScope.launch {
                val result = firestoreRepository.loginUser(email, password)
                result.onSuccess { user ->
                    Toast.makeText(this@LoginActivity, "Inici de sessió exitós per a ${user.username}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                }.onFailure { exception ->
                    Toast.makeText(this@LoginActivity, "Error d'autenticació: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Carregar la vista de registre
        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Afegim opcions per a desactivar l'animació d'entrada
            val options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                0, // Animació per a la nova activitat
                0  // Animació per a l'activitat que surt
            )
            startActivity(intent, options.toBundle())
            finish()
        }
    }
}
