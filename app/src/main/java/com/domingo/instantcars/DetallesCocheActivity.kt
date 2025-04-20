package com.domingo.instantcars

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class DetallesCocheActivity : AppCompatActivity() {

    private lateinit var imageViewCoche: ImageView
    private lateinit var textViewTituloCoche: TextView
    private lateinit var textViewPrecioCoche: TextView
    private lateinit var textViewNombreUsuario: TextView
    private lateinit var textViewDescripcion: TextView
    private lateinit var buttonNegociar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_page)

        // Enlazar vistas
        imageViewCoche = findViewById(R.id.imageViewCoche)
        textViewTituloCoche = findViewById(R.id.textViewTituloCoche)
        textViewPrecioCoche = findViewById(R.id.textViewPrecioCoche)
        textViewNombreUsuario = findViewById(R.id.textViewNombreUsuario)
        textViewDescripcion = findViewById(R.id.textViewDescripcion)
        buttonNegociar = findViewById(R.id.buttonNegociar)

        // Recoger ID que viene desde el Adapter
        val cocheId = intent.getStringExtra("cocheId")

        if (cocheId != null) {
            cargarDetallesCoche(cocheId)
        } else {
            Toast.makeText(this, "Error: ID de coche no recibido", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Volver a la página principal
        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarDetallesCoche(cocheId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("coches").document(cocheId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val marca = doc.getString("marca") ?: ""
                    val modelo = doc.getString("modelo") ?: ""
                    val precio = doc.getString("precio") ?: ""
                    val subidoPorNombre = doc.getString("subidoPorNombre") ?: ""
                    val descripcion = doc.getString("descripcion") ?: ""
                    val imagenBase64 = doc.getString("imagen") ?: ""

                    textViewTituloCoche.text = getString(R.string.MarcaModelo, marca, modelo)
                    textViewPrecioCoche.text = getString(R.string.PrecioCoche, precio)
                    textViewNombreUsuario.text = subidoPorNombre
                    textViewDescripcion.text = descripcion

                    val bitmap = convertirBase64ABitmap(imagenBase64)
                    if (bitmap != null) {
                        imageViewCoche.setImageBitmap(bitmap)
                    } else {
                        imageViewCoche.setImageResource(R.drawable.errorimagencoche)
                    }

                } else {
                    Toast.makeText(this, "Coche no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun convertirBase64ABitmap(base64String: String): android.graphics.Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
