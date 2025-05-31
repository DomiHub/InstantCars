package com.domingo.instantcars.subidas

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.domingo.instantcars.R
import com.domingo.instantcars.vehiculos.Coche
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MisSubidasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SubidasAdapter
    private val listaSubidas = mutableListOf<Coche>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.uploads_page)

        recyclerView = findViewById(R.id.recyclerViewFavoritos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SubidasAdapter(listaSubidas) { coche ->
            eliminarCocheSubido(coche)
        }
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.volver_button).setOnClickListener {
            finish()
        }

        cargarSubidasUsuario()
    }

    private fun cargarSubidasUsuario() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("coches")
            .whereEqualTo("subidoPor", userId)
            .get()
            .addOnSuccessListener { result ->
                listaSubidas.clear()
                for (doc in result) {
                    val coche = Coche(
                        id = doc.id,
                        marca = doc.getString("marca") ?: "",
                        modelo = doc.getString("modelo") ?: "",
                        precio = doc.getString("precio") ?: "",
                        ubicacion = doc.getString("ubicacion") ?: "",
                        imagen = doc.getString("imagen") ?: "",
                    )
                    listaSubidas.add(coche)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar tus subidas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarCocheSubido(coche: Coche) {
        coche.id?.let {
            db.collection("coches").document(it)
                .delete()
                .addOnSuccessListener {
                    db.collection("favoritos")
                        .whereEqualTo("cocheId", coche.id)
                        .get()
                        .addOnSuccessListener { favDocs ->
                            for (fav in favDocs) {
                                db.collection("favoritos").document(fav.id).delete()
                            }
                        }

                    listaSubidas.remove(coche)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Coche eliminado correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al eliminar el coche", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
