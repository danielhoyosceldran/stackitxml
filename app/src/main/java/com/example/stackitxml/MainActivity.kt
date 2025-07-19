package com.example.stackitxml

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.stackitxml.ui.auth.LoginActivity
import com.example.stackitxml.ui.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Comprova si l'usuari ja ha iniciat sessió
        if (auth.currentUser != null) {
            // L'usuari ha iniciat sessió, ves a la pantalla Home
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // Cap usuari ha iniciat sessió, ves a la pantalla de Login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish() // Tanca MainActivity perquè l'usuari no pugui tornar enrere a aquesta pantalla innecessària
    }
}
