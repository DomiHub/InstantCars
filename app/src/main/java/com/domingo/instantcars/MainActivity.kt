package com.domingo.instantcars

import android.os.Bundle
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cocheAdapter: cocheAdapter
    private lateinit var searchView: SearchView
    private lateinit var imageViewChat: ImageView
    private lateinit var imageViewUser: ImageView
    private lateinit var fabAdd: FloatingActionButton
    private val db = FirebaseFirestore.getInstance()

    private var listaCoches: MutableList<Coche> = mutableListOf()
    private var listaFiltrada: MutableList<Coche> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        recyclerView = findViewById(R.id.recyclerViewCoches)
        searchView = findViewById(R.id.searchView)
        imageViewChat = findViewById(R.id.imageView)
        imageViewUser = findViewById(R.id.imagenPulsable)
        fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        recyclerView.layoutManager = LinearLayoutManager(this)
        cocheAdapter = cocheAdapter(listaFiltrada)
        recyclerView.adapter = cocheAdapter

        obtenerCochesDesdeFirebase()
        setupSearchView()
        setupButtons()
    }

    private fun obtenerCochesDesdeFirebase() {
        db.collection("coches").get()
            .addOnSuccessListener { result ->
                listaCoches.clear()
                for (document in result) {
                    val coche = document.toObject(Coche::class.java)
                    listaCoches.add(coche)
                }
                listaFiltrada.clear()
                listaFiltrada.addAll(listaCoches)
                cocheAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Manejo de errores si la carga falla
            }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarCoches(newText)
                return true
            }
        })
    }

    private fun filtrarCoches(texto: String?) {
        listaFiltrada.clear()
        if (texto.isNullOrEmpty()) {
            listaFiltrada.addAll(listaCoches)
        } else {
            val filtro = texto.lowercase()
            listaFiltrada.addAll(listaCoches.filter {
                it.marca.lowercase().contains(filtro) || it.modelo.lowercase().contains(filtro)
            })
        }
        cocheAdapter.notifyDataSetChanged()
    }

    private fun setupButtons() {
        imageViewChat.setOnClickListener {
            // Acción al presionar el icono de chat
        }

        imageViewUser.setOnClickListener {
            // Acción al presionar el icono de usuario
        }
        fabAdd.setOnClickListener {
            // Acción al presionar el botón flotante
        }

    }
}
