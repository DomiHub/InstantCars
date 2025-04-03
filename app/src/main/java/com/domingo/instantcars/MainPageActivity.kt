package com.domingo.instantcars

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class MainPageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cocheAdapter: CocheAdapter
    private var listaCoches = mutableListOf<Coche>()
    private lateinit var searchView: SearchView
    private lateinit var fabAdd: FloatingActionButton

    override fun onResume() {
        super.onResume()
        cargarCochesDesdeFirebase() // Recarga la lista de coches al volver a esta pantalla
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewCoches)
        searchView = findViewById(R.id.searchView)
        fabAdd = findViewById(R.id.fabAdd)

        // Configurar RecyclerView con GridLayoutManager (2 columnas)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        cocheAdapter = CocheAdapter(listaCoches)
        recyclerView.adapter = cocheAdapter

        // Cargar coches desde Firebase
        cargarCochesDesdeFirebase()

        // Configurar búsqueda en SearchView
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

        // Botón flotante para agregar un coche
        fabAdd.setOnClickListener {
            val intent = Intent(this, FormCocheActivity::class.java)
            startActivity(intent)
        }
    }

    private fun convertirBase64ABitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
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
                cocheAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Manejo de error
            }
    }

    private fun filtrarCoches(query: String?) {
        val listaFiltrada = listaCoches.filter {
            it.marca.contains(query ?: "", ignoreCase = true) ||
                    it.modelo.contains(query ?: "", ignoreCase = true) ||
                    it.ubicacion.contains(query ?: "", ignoreCase = true)
        }
        cocheAdapter = CocheAdapter(listaFiltrada)
        recyclerView.adapter = cocheAdapter
    }
}
