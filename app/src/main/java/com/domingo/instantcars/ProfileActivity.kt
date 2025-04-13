package com.domingo.instantcars

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var backButton: ImageView
    private lateinit var fullname: TextView
    private lateinit var email_input: TextView
    private lateinit var imageView: ImageView
    private lateinit var usernameInput: TextInputEditText
    private lateinit var updateButton: Button
    private lateinit var logoutButton: Button

    private var imageBase64: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_user)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicialización de las vistas
        backButton = findViewById(R.id.volver_button)
        fullname = findViewById(R.id.full_name)
        email_input = findViewById(R.id.email_input)
        imageView = findViewById(R.id.profile_image)
        usernameInput = findViewById(R.id.username_input)
        updateButton = findViewById(R.id.update_button)
        logoutButton = findViewById(R.id.logout_button)

        cargarDatosUsuario()

        imageView.setOnClickListener {
            seleccionarImagenDesdeGaleria()
        }
        backButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
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
                // Mostrar el nombre de usuario y correo
                fullname.text = document.getString("username")
                usernameInput.setText(document.getString("username"))
                email_input.text = auth.currentUser?.email  // Mostrar el correo electrónico

                // Cargar la imagen de perfil
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

        updateButton.isEnabled = false  // Desactivar botón

        // Validar campos obligatorios
        if (nuevoUsername.isEmpty()) {
            Toast.makeText(this, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
            updateButton.isEnabled = true
            return
        }

        // Verificar si el nombre de usuario ya está en uso por otro
        db.collection("users").whereEqualTo("username", nuevoUsername).get()
            .addOnSuccessListener { usernameResult ->
                val usernameOcupadoPorOtro = usernameResult.any { it.id != uid }

                if (usernameOcupadoPorOtro) {
                    Toast.makeText(this, "Este nombre de usuario ya está en uso", Toast.LENGTH_SHORT).show()
                    updateButton.isEnabled = true
                } else {
                    // Si los campos son válidos, actualizar en Firebase
                    val updates = mutableMapOf<String, Any>()
                    updates["username"] = nuevoUsername

                    imageBase64?.let {
                        updates["profile_image"] = it
                    }

                    // Actualizar en Firestore
                    db.collection("users").document(uid).update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()

                            // Recargar los datos del usuario para reflejar los cambios inmediatamente
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
            // Maneja la URI de la imagen seleccionada aquí
            val inputStream = contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)

            // Convertir la imagen seleccionada a Base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val imageBytes = outputStream.toByteArray()
            val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            imageBase64 = "data:image/jpeg;base64,$encodedImage"
        }
    }

    private fun seleccionarImagenDesdeGaleria() {
        // Llamamos para seleccionar una imagen
        launcher.launch("image/*")  // Tipo de archivo
    }
}
