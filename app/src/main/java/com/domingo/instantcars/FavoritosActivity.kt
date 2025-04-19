package com.domingo.instantcars

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var favoritosAdapter: FavoritosAdapter
    private val listaFavoritos = mutableListOf<Coche>()
    private lateinit var volver_button: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.like_page)

        volver_button = findViewById(R.id.volver_button)
        volver_button.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerViewFavoritos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        favoritosAdapter = FavoritosAdapter(listaFavoritos) { coche ->
            eliminarDeFavoritos(coche)
        }
        recyclerView.adapter = favoritosAdapter

        cargarFavoritosDesdeFirestore()
    }

    private fun cargarFavoritosDesdeFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("favoritos")
            .whereEqualTo("subidoPor", userId)
            .get()
            .addOnSuccessListener { documentos ->
                listaFavoritos.clear()

                if (documentos.isEmpty) {
                    Toast.makeText(this, "No tienes coches favoritos todavía.", Toast.LENGTH_SHORT)
                        .show()
                    favoritosAdapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                var totalFavoritos = documentos.size()
                var cochesCargados = 0

                for (doc in documentos) {
                    val cocheId = doc.getString("cocheId")

                    if (cocheId != null) {
                        db.collection("coches").document(cocheId)
                            .get()
                            .addOnSuccessListener { cocheDoc ->
                                val coche =
                                    cocheDoc.toObject(Coche::class.java)?.copy(id = cocheDoc.id)
                                if (coche != null) {
                                    listaFavoritos.add(coche)
                                }
                            }
                            .addOnFailureListener {
                                // Puedes mostrar un error individual si quieres aquí
                            }
                            .addOnCompleteListener {
                                cochesCargados++

                                if (cochesCargados == totalFavoritos) {
                                    favoritosAdapter.notifyDataSetChanged()

                                    if (listaFavoritos.isEmpty()) {
                                        Toast.makeText(
                                            this,
                                            "No tienes coches favoritos todavía.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                    } else {
                        cochesCargados++
                        if (cochesCargados == totalFavoritos) {
                            favoritosAdapter.notifyDataSetChanged()

                            if (listaFavoritos.isEmpty()) {
                                Toast.makeText(
                                    this,
                                    "No tienes coches favoritos todavía.",
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
                    "Error al cargar los favoritos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun eliminarDeFavoritos(coche: Coche) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Buscar si este coche está en los favoritos del usuario
        db.collection("favoritos")
            .whereEqualTo("subidoPor", userId)
            .whereEqualTo(
                "cocheId",
                coche.id
            )  // Nos aseguramos de que estamos buscando el coche correcto
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    Toast.makeText(this, "Este coche no está en tus favoritos", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // Si encontramos el coche en los favoritos, eliminamos el primer documento encontrado
                    val document = docs.first()
                    db.collection("favoritos").document(document.id).delete()
                        .addOnSuccessListener {
                            listaFavoritos.remove(coche)
                            favoritosAdapter.notifyDataSetChanged()
                            Toast.makeText(this, "Coche eliminado de favoritos", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Error al eliminar el coche de favoritos",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al buscar el coche en favoritos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}
