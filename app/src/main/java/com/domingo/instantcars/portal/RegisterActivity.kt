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
import com.google.firebase.firestore.FirebaseFirestore


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page)

        val buttonGoTologinaccount = findViewById<Button>(R.id.button_GoTo_login_account)

        buttonGoTologinaccount.setOnClickListener {
            val intent = Intent(this, PortalActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()

        val editTextEmail = findViewById<EditText>(R.id.email_edit_text)
        val userEditText = findViewById<EditText>(R.id.user_edit_text)
        val editTextPassword = findViewById<EditText>(R.id.password_edit_text)
        val btnRegister = findViewById<Button>(R.id.button_register)

        btnRegister.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val userName = userEditText.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            // Validar campos
            if (email.isEmpty() || password.isEmpty() || userName.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            // Crear usuario con Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        // Guarda datos adicionales en Firestore
                        val db = FirebaseFirestore.getInstance()
                        val userData = hashMapOf(
                            "username" to userName,
                            "email" to email,
                            "uid" to user?.uid,
                        )

                        db.collection("users").document(user!!.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, MainPageActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error guardando datos: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }

                }

        }
    }
}