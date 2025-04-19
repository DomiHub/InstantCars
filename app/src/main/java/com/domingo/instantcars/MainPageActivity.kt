package com.domingo.instantcars

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainPageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cocheAdapter: CocheAdapter
    private var listaCoches = mutableListOf<Coche>()
    private var listaFavoritos = listOf<String>()
    private lateinit var searchView: SearchView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var profileImageView: ImageView

    override fun onResume() {
        super.onResume()
        cargarFavoritosYActualizar() // Recargar coches + favoritos al volver
        cargarImagenPerfil() // Cargar imagen de perfil también
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        recyclerView = findViewById(R.id.recyclerViewCoches)
        searchView = findViewById(R.id.searchView)
        fabAdd = findViewById(R.id.fabAdd)
        profileImageView = findViewById(R.id.profile_image)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        cocheAdapter = CocheAdapter(listaCoches)
        recyclerView.adapter = cocheAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarCoches(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarCoches(newText)
                return true
            }
        })

        fabAdd.setOnClickListener {
            startActivity(Intent(this, FormCocheActivity::class.java))
        }

        profileImageView.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun cargarFavoritosYActualizar() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("favoritos")
            .whereEqualTo("subidoPor", userId)
            .get()
            .addOnSuccessListener { documents ->
                listaFavoritos = documents.mapNotNull { it.getString("cocheId") }
                cargarCochesDesdeFirebase() // Cargar coches cuando favoritos estén listos
            }
    }

    private fun cargarCochesDesdeFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("coches")
            .get()
            .addOnSuccessListener { documents ->
                listaCoches.clear()
                for (document in documents) {
                    val coche = Coche(
                        id = document.id,
                        marca = document.getString("marca") ?: "",
                        modelo = document.getString("modelo") ?: "",
                        anio = document.getString("anio") ?: "",
                        precio = document.getString("precio") ?: "",
                        km = document.getString("kilometraje") ?: "",
                        imagen = document.getString("imagen") ?: "",
                        subidoPor = document.getString("subidoPor") ?: "",
                        descripcion = document.getString("descripcion") ?: "",
                        ubicacion = document.getString("ubicacion") ?: ""
                    )
                    listaCoches.add(coche)
                }
                cocheAdapter.setFavoritos(listaFavoritos)
                cocheAdapter.notifyDataSetChanged()
            }
    }

    private fun filtrarCoches(query: String?) {
        val listaFiltrada = listaCoches.filter {
            it.marca.contains(query ?: "", ignoreCase = true) ||
                    it.modelo.contains(query ?: "", ignoreCase = true) ||
                    it.ubicacion.contains(query ?: "", ignoreCase = true)
        }
        val nuevoAdapter = CocheAdapter(listaFiltrada).apply {
            setFavoritos(listaFavoritos)
        }
        recyclerView.adapter = nuevoAdapter
    }

    private fun cargarImagenPerfil() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                document.getString("profile_image")?.let { encoded ->
                    val pureBase64 = encoded.substringAfter(",")
                    val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    profileImageView.setImageBitmap(bitmap)
                }
            }
    }
}
