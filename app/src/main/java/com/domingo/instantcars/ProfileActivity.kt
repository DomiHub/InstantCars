package com.domingo.instantcars

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var backButton: ImageView
    private lateinit var cardViewUpload: CardView
    private lateinit var cardViewFav: CardView
    private lateinit var fullname: TextView
    private lateinit var emailInput: TextView
    private lateinit var imageView: ImageView
    private lateinit var usernameInput: TextInputEditText
    private lateinit var updateButton: Button
    private lateinit var logoutButton: Button
    private lateinit var favLabel: TextView

    private var imageBase64: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_user)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicialización de las vistas
        backButton = findViewById(R.id.volver_button)
        cardViewUpload = findViewById(R.id.cardViewUpload)
        cardViewFav = findViewById(R.id.cardViewFav)
        fullname = findViewById(R.id.full_name)
        emailInput = findViewById(R.id.email_input)
        imageView = findViewById(R.id.profile_image)
        usernameInput = findViewById(R.id.username_input)
        updateButton = findViewById(R.id.update_button)
        logoutButton = findViewById(R.id.logout_button)
        favLabel = findViewById(R.id.fav_label)

        cargarDatosUsuario()
        contarFavoritos()

        imageView.setOnClickListener {
            seleccionarImagenDesdeGaleria()
        }

        backButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }

        cardViewFav.setOnClickListener {
            val intent = Intent(this, FavoritosActivity::class.java)
            startActivity(intent)
        }

        updateButton.setOnClickListener {
            actualizarDatosUsuario()
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, PortalActivity::class.java)
            Toast.makeText(this, "Has cerrrado sesión", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
    }

    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                fullname.text = document.getString("username")
                usernameInput.setText(document.getString("username"))
                emailInput.text = auth.currentUser?.email

                val base64 = document.getString("profile_image")
                base64?.let {
                    val imageBytes = Base64.decode(it.substringAfter(","), Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun actualizarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return
        val nuevoUsername = usernameInput.text.toString().trim()

        updateButton.isEnabled = false

        if (nuevoUsername.isEmpty()) {
            Toast.makeText(this, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
            updateButton.isEnabled = true
            return
        }

        db.collection("users").whereEqualTo("username", nuevoUsername).get()
            .addOnSuccessListener { usernameResult ->
                val usernameOcupadoPorOtro = usernameResult.any { it.id != uid }

                if (usernameOcupadoPorOtro) {
                    Toast.makeText(this, "Este nombre de usuario ya está en uso", Toast.LENGTH_SHORT).show()
                    updateButton.isEnabled = true
                } else {
                    val updates = mutableMapOf<String, Any>()
                    updates["username"] = nuevoUsername
                    imageBase64?.let { updates["profile_image"] = it }

                    db.collection("users").document(uid).update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                            cargarDatosUsuario()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show()
                        }
                    updateButton.isEnabled = true
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar el nombre de usuario", Toast.LENGTH_SHORT).show()
                updateButton.isEnabled = true
            }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val imageBytes = outputStream.toByteArray()
            val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            imageBase64 = "data:image/jpeg;base64,$encodedImage"
        }
    }

    private fun seleccionarImagenDesdeGaleria() {
        launcher.launch("image/*")
    }

    private fun contarFavoritos() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("favoritos")
            .whereEqualTo("subidoPor", userId)
            .get()
            .addOnSuccessListener { documentos ->
                val totalFavoritos = documentos.size()
                favLabel.text = String.format(totalFavoritos.toString())
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al contar favoritos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        contarFavoritos()  // Actualiza el contador al volver de FavoritosActivity
    }
}
