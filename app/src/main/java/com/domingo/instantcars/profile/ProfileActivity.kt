package com.domingo.instantcars.profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.domingo.instantcars.home.MainPageActivity
import com.domingo.instantcars.R
import com.domingo.instantcars.favoritos.FavoritosActivity
import com.domingo.instantcars.portal.PortalActivity
import com.domingo.instantcars.subidas.MisSubidasActivity
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
    private lateinit var uploadLabel: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var ratingText: TextView

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
        uploadLabel = findViewById(R.id.upload_label)
        ratingBar = findViewById(R.id.rating_bar_reputation)
        ratingText = findViewById(R.id.rating_count_text)

        cargarDatosUsuario()
        contarFavoritos()
        contarSubidas()

        val uid = auth.currentUser?.uid
        uid?.let { cargarReputacionPromedio(it) }

        imageView.setOnClickListener {
            seleccionarImagenDesdeGaleria()
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, MainPageActivity::class.java))
            finish()
        }

        cardViewFav.setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
        }

        cardViewUpload.setOnClickListener {
            startActivity(Intent(this, MisSubidasActivity::class.java))
        }

        updateButton.setOnClickListener {
            actualizarDatosUsuario()
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Has cerrado sesión", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, PortalActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        contarFavoritos()
        contarSubidas()
    }

    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
                fullname.text = document.getString("username")
                usernameInput.setText(document.getString("username"))
                emailInput.text = auth.currentUser?.email

                document.getString("profile_image")?.let { base64 ->
                    val imageBytes = Base64.decode(base64.substringAfter(","), Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun contarFavoritos() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("favoritos")
            .whereEqualTo("subidoPor", userId)
            .get()
            .addOnSuccessListener { docs ->
                favLabel.text = docs.size().toString()
            }
    }

    private fun contarSubidas() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("coches")
            .whereEqualTo("subidoPor", userId)
            .get()
            .addOnSuccessListener { docs ->
                uploadLabel.text = docs.size().toString()
            }
    }

    private fun actualizarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return
        val nuevoUsername = usernameInput.text.toString().trim()
        if (nuevoUsername.isEmpty()) {
            Toast.makeText(this, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT)
                .show()
            return
        }

        updateButton.isEnabled = false

        db.collection("users").whereEqualTo("username", nuevoUsername).get()
            .addOnSuccessListener { result ->
                if (result.any { it.id != uid }) {
                    Toast.makeText(this, "Nombre de usuario en uso", Toast.LENGTH_SHORT).show()
                } else {
                    val updates = mutableMapOf<String, Any>("username" to nuevoUsername)
                    imageBase64?.let { updates["profile_image"] = it }
                    db.collection("users").document(uid).update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                }
                updateButton.isEnabled = true
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar nombre", Toast.LENGTH_SHORT).show()
                updateButton.isEnabled = true
            }
    }

    private fun cargarReputacionPromedio(uid: String) {
        db.collection("ratings")
            .whereEqualTo("calificadoA", uid)
            .get()
            .addOnSuccessListener { result ->
                val total = result.size()
                if (total > 0) {
                    val suma = result.documents.sumOf {
                        it.getDouble("valor") ?: 0.0
                    }
                    val promedio = suma / total
                    ratingBar.rating = promedio.toFloat()
                    ratingText.text = String.format(getString(R.string.calificaciones), promedio, total)
                } else {
                    ratingBar.rating = 0f
                    ratingText.text = getString(R.string._0_calificaciones)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar reputación", Toast.LENGTH_SHORT).show()
            }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            contentResolver.openInputStream(it)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                imageView.setImageBitmap(bitmap)
                ByteArrayOutputStream().use { os ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                    imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(
                        os.toByteArray(),
                        Base64.DEFAULT
                    )
                }
            }
        }
    }

    private fun seleccionarImagenDesdeGaleria() = launcher.launch("image/*")
}
