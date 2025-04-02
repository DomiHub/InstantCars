package com.domingo.instantcars

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class PortalActivity : AppCompatActivity() {

    private fun ComprobarSesion() {
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

        ComprobarSesion()

        //Evento para ir a la pagina de registro
        val button_GoTo_create_account = findViewById<Button>(R.id.button_GoTo_create_account)

        button_GoTo_create_account.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        //Evento para logearse por hacer
        val auth = FirebaseAuth.getInstance()

        val button_login = findViewById<Button>(R.id.button_login)
        val editTextEmail = findViewById<EditText>(R.id.email_edit_text)
        val editTextPassword = findViewById<EditText>(R.id.password_edit_text)

        // Configurar el botón de login
        button_login.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            // Validar campos vacíos
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Iniciar sesión con FirebaseAuth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Usuario autenticado correctamente
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainPageActivity::class.java)
                        startActivity(intent)
                        finish() // Evita que vuelva a la pantalla de login con el botón atrás

                    } else {
                        // Error al iniciar sesión
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
        }
    }
}