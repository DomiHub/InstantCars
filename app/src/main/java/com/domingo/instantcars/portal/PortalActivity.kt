package com.domingo.instantcars.portal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.domingo.instantcars.home.MainPageActivity
import com.domingo.instantcars.R
import com.google.firebase.auth.FirebaseAuth

class PortalActivity : AppCompatActivity() {

//Metodo que comprueba si el usuario ya tiene una sesion iniciada
    private fun comprobarSesion() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        comprobarSesion()

        val buttonGoToCreateAccount = findViewById<Button>(R.id.button_GoTo_create_account)

        buttonGoToCreateAccount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        //Evento para logearse por hacer
        val auth = FirebaseAuth.getInstance()

        val buttonLogin = findViewById<Button>(R.id.button_login)
        val editTextEmail = findViewById<EditText>(R.id.email_edit_text)
        val editTextPassword = findViewById<EditText>(R.id.password_edit_text)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Iniciar sesión con FirebaseAuth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainPageActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
        }
    }
}