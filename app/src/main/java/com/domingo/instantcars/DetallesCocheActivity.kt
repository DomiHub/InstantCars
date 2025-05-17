package com.domingo.instantcars

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetallesCocheActivity : AppCompatActivity() {

    private lateinit var imageViewCoche: ImageView
    private lateinit var textViewTituloCoche: TextView
    private lateinit var textViewPrecioCoche: TextView
    private lateinit var textViewUbi: TextView
    private lateinit var textViewKm: TextView
    private lateinit var textViewNombreUsuario: TextView
    private lateinit var textViewDescripcion: TextView
    private lateinit var buttonNegociar: Button
    private lateinit var imageViewFavorite: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var ratingBar: RatingBar

    private var userIdPropietario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.car_page)

        imageViewCoche = findViewById(R.id.imageViewCoche)
        textViewTituloCoche = findViewById(R.id.textViewTituloCoche)
        textViewPrecioCoche = findViewById(R.id.textViewPrecioCoche)
        textViewNombreUsuario = findViewById(R.id.textViewNombreUsuario)
        textViewUbi = findViewById(R.id.textViewUbi)
        textViewKm = findViewById(R.id.textViewKm)
        textViewDescripcion = findViewById(R.id.textViewDescripcion)
        buttonNegociar = findViewById(R.id.buttonNegociar)
        imageViewFavorite = findViewById(R.id.imageViewFavorite)
        profileImage = findViewById(R.id.profile_image)
        ratingBar = findViewById(R.id.rating_bar_reputation)

        val cocheId = intent.getStringExtra("cocheId")

        if (cocheId != null) {
            cargarDetallesCoche(cocheId)
            verificarFavorito(cocheId)
        } else {
            Toast.makeText(this, "Error: ID de coche no recibido", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        fun abrirChat(chatId: String, receiverId: String) {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chatId)
            intent.putExtra("receiverId", receiverId)
            startActivity(intent)
        }

        buttonNegociar.setOnClickListener {
            val senderId = FirebaseAuth.getInstance().currentUser?.uid
            val receiverId = userIdPropietario

            if (senderId != null && receiverId != null && senderId != receiverId) {
                val db = FirebaseFirestore.getInstance()

                db.collection("chats")
                    .whereEqualTo("user1", senderId)
                    .whereEqualTo("user2", receiverId)
                    .get()
                    .addOnSuccessListener { query1 ->
                        if (!query1.isEmpty) {
                            val chatId = query1.documents[0].id
                            abrirChat(chatId, receiverId)
                        } else {
                            db.collection("chats")
                                .whereEqualTo("user1", receiverId)
                                .whereEqualTo("user2", senderId)
                                .get()
                                .addOnSuccessListener { query2 ->
                                    if (!query2.isEmpty) {
                                        val chatId = query2.documents[0].id
                                        abrirChat(chatId, receiverId)
                                    } else {
                                        val nuevoChat = hashMapOf(
                                            "user1" to senderId,
                                            "user2" to receiverId,
                                            "userIds" to listOf(senderId, receiverId),
                                            "lastMessage" to "",
                                            "timestamp" to com.google.firebase.Timestamp.now()
                                        )

                                        db.collection("chats")
                                            .add(nuevoChat)
                                            .addOnSuccessListener { documentReference ->
                                                val nuevoChatId = documentReference.id
                                                abrirChat(nuevoChatId, receiverId)
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    this,
                                                    "Error creando chat: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al buscar chat: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "No puedes negociar contigo mismo", Toast.LENGTH_SHORT).show()
            }
        }

        val irAOtroPerfil = {
            userIdPropietario?.let {
                val intent = Intent(this, OtherProfileActivity::class.java)
                intent.putExtra("userId", it)
                startActivity(intent)
            }
        }

        textViewNombreUsuario.setOnClickListener { irAOtroPerfil() }
        profileImage.setOnClickListener { irAOtroPerfil() }

        imageViewFavorite.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null || cocheId == null) return@setOnClickListener

            val db = FirebaseFirestore.getInstance()
            db.collection("favoritos")
                .whereEqualTo("subidoPor", userId)
                .whereEqualTo("cocheId", cocheId)
                .get()
                .addOnSuccessListener { docs ->
                    if (docs.isEmpty) {
                        val favorito = mapOf(
                            "subidoPor" to userId,
                            "cocheId" to cocheId
                        )
                        db.collection("favoritos").add(favorito)
                        imageViewFavorite.setImageResource(R.drawable.baseline_favorite_24)
                        Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show()
                    } else {
                        for (doc in docs) {
                            db.collection("favoritos").document(doc.id).delete()
                        }
                        imageViewFavorite.setImageResource(R.drawable.outline_favorite_24)
                        Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
                    val ubicacion = doc.getString("ubicacion") ?: ""
                    val kilometraje = doc.getString("kilometraje") ?: ""
                    val subidoPorNombre = doc.getString("subidoPorNombre") ?: ""
                    val descripcion = doc.getString("descripcion") ?: ""
                    val imagenBase64 = doc.getString("imagen") ?: ""
                    val subidoPorUid = doc.getString("subidoPor") ?: ""

                    userIdPropietario = subidoPorUid

                    textViewTituloCoche.text = getString(R.string.MarcaModelo, marca, modelo)
                    textViewPrecioCoche.text = getString(R.string.PrecioCoche, precio)
                    textViewUbi.text = ubicacion
                    textViewKm.text = getString(R.string.kilometrajeKm, kilometraje)
                    textViewNombreUsuario.text = subidoPorNombre
                    textViewDescripcion.text = descripcion

                    val bitmap = convertirBase64ABitmap(imagenBase64)
                    if (bitmap != null) {
                        imageViewCoche.setImageBitmap(bitmap)
                    } else {
                        imageViewCoche.setImageResource(R.drawable.errorimagencoche)
                    }

                    cargarReputacionPromedio(subidoPorUid)

                    if (subidoPorUid.isNotEmpty()) {
                        db.collection("users").document(subidoPorUid).get()
                            .addOnSuccessListener { userDoc ->
                                val base64 = userDoc.getString("profile_image")
                                base64?.let {
                                    val imageBytes =
                                        Base64.decode(it.substringAfter(","), Base64.DEFAULT)
                                    val bitmap2 = BitmapFactory.decodeByteArray(
                                        imageBytes,
                                        0,
                                        imageBytes.size
                                    )
                                    profileImage.setImageBitmap(bitmap2)
                                }
                            }
                    }

                } else {
                    Toast.makeText(this, "Coche no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
    }

    private fun cargarReputacionPromedio(uid: String) {
        val db = FirebaseFirestore.getInstance()
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
                } else {
                    ratingBar.rating = 0f
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar reputación", Toast.LENGTH_SHORT).show()
            }
    }


    private fun verificarFavorito(cocheId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("favoritos")
            .whereEqualTo("subidoPor", userId)
            .whereEqualTo("cocheId", cocheId)
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    imageViewFavorite.setImageResource(R.drawable.outline_favorite_24)
                } else {
                    imageViewFavorite.setImageResource(R.drawable.baseline_favorite_24)
                }
            }
    }

    private fun convertirBase64ABitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
