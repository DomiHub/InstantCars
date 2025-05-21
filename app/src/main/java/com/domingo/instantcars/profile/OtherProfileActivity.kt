package com.domingo.instantcars.profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.domingo.instantcars.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

class OtherProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var imageView: ImageView
    private lateinit var fullname: TextView
    private lateinit var usernameInput: TextInputEditText
    private lateinit var uploadLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.other_user)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        imageView = findViewById(R.id.profile_image)
        fullname = findViewById(R.id.full_name)
        usernameInput = findViewById(R.id.username_input)
        uploadLabel = findViewById(R.id.upload_label)

        val userId = intent.getStringExtra("userId") ?: return

        cargarDatosUsuario(userId)
        cargarNumeroSubidas(userId)
        cargarReputacionPromedio(userId)

        findViewById<Button>(R.id.btn_rate_user).setOnClickListener {
            mostrarDialogoCalificacion(userId)
        }

        findViewById<ImageView>(R.id.volver_button).setOnClickListener {
            finish()
        }
    }

    private fun cargarDatosUsuario(uid: String) {
        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
                fullname.text = document.getString("username")
                usernameInput.setText(document.getString("username"))

                document.getString("profile_image")?.let { base64 ->
                    val imageBytes = Base64.decode(base64.substringAfter(","), Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun mostrarDialogoCalificacion(uidCalificado: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rating, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.dialog_rating_bar)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Calificar") { _, _ ->
                val valor = ratingBar.rating.toDouble()
                val currentUid = auth.currentUser?.uid ?: return@setPositiveButton

                val ratingsRef = db.collection("ratings")
                val query = ratingsRef
                    .whereEqualTo("calificadoA", uidCalificado)
                    .whereEqualTo("calificadoPor", currentUid)

                query.get().addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        // Crear nueva calificación
                        val data = hashMapOf(
                            "calificadoA" to uidCalificado,
                            "calificadoPor" to currentUid,
                            "valor" to valor
                        )
                        ratingsRef.add(data).addOnSuccessListener {
                            cargarReputacionPromedio(uidCalificado)
                        }
                    } else {
                        // Ya existe, actualizar
                        val docId = result.documents[0].id
                        ratingsRef.document(docId)
                            .update("valor", valor)
                            .addOnSuccessListener {
                                cargarReputacionPromedio(uidCalificado)
                            }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun cargarReputacionPromedio(uid: String) {
        db.collection("ratings")
            .whereEqualTo("calificadoA", uid)
            .get()
            .addOnSuccessListener { result ->
                val total = result.size()
                val ratingBar = findViewById<RatingBar>(R.id.rating_bar_reputation)
                val ratingText = findViewById<TextView>(R.id.rating_count_text)

                if (total > 0) {
                    val suma = result.documents.sumOf {
                        it.getDouble("valor") ?: 0.0
                    }
                    val promedio = suma / total
                    ratingBar.rating = promedio.toFloat()
                    String.format(getString(R.string.calificaciones), promedio, total)
                        .also { ratingText.text = it }
                } else {
                    ratingBar.rating = 0f
                    ratingText.text = getString(R.string._0_calificaciones)
                }
            }
    }


    private fun cargarNumeroSubidas(uid: String) {
        db.collection("coches").whereEqualTo("subidoPor", uid).get()
            .addOnSuccessListener { result ->
                val total =
                    result.size()
                uploadLabel.text = String.format(Locale.getDefault(), "%d", total)
            }
    }
}
