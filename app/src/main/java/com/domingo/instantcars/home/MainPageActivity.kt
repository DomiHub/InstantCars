package com.domingo.instantcars.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.domingo.instantcars.R
import com.domingo.instantcars.chat.ChatListActivity
import com.domingo.instantcars.profile.ProfileActivity
import com.domingo.instantcars.subidas.FormCocheActivity
import com.domingo.instantcars.vehiculos.Coche
import com.domingo.instantcars.vehiculos.CocheAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainPageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cocheAdapter: CocheAdapter
    private var listaCoches = mutableListOf<Coche>()
    private var listaFavoritos = listOf<String>()
    private lateinit var imageView: ImageView
    private lateinit var searchView: SearchView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var profileImageView: ImageView
    private lateinit var fabFilter: FloatingActionButton
    private var filtroKm: Int? = null
    private var filtroPrecio: Int? = null
    private var filtroUbicacion: String = ""
    private var textoBuscado: String = ""

    override fun onResume() {
        super.onResume()
        cargarFavoritosYActualizar()
        cargarImagenPerfil()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        recyclerView = findViewById(R.id.recyclerViewCoches)
        searchView = findViewById(R.id.searchView)
        imageView = findViewById(R.id.imageViewChat)
        fabAdd = findViewById(R.id.fabAdd)
        profileImageView = findViewById(R.id.profile_image)
        fabFilter = findViewById(R.id.fabFilter)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        cocheAdapter = CocheAdapter(listaCoches)
        recyclerView.adapter = cocheAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                textoBuscado = query ?: ""
                filtrarTodos()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                textoBuscado = newText ?: ""
                filtrarTodos()
                return true
            }
        })

        fabFilter.setOnClickListener {
            val bottomSheet = layoutInflater.inflate(R.layout.filtros_main_page, null)
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(bottomSheet)

            val kmEditText = bottomSheet.findViewById<EditText>(R.id.editTextKm)
            val precioEditText = bottomSheet.findViewById<EditText>(R.id.editTextPrecio)
            val ubicacionEditText = bottomSheet.findViewById<EditText>(R.id.editTextUbicacion)
            val aplicarButton = bottomSheet.findViewById<Button>(R.id.btnAplicarFiltros)
            val limpiarButton = bottomSheet.findViewById<Button>(R.id.btnLimpiarFiltros)

            aplicarButton.setOnClickListener {
                filtroKm = kmEditText.text.toString().toIntOrNull()
                filtroPrecio = precioEditText.text.toString().toIntOrNull()
                filtroUbicacion = ubicacionEditText.text.toString()
                filtrarTodos()
                dialog.dismiss()
            }

            limpiarButton.setOnClickListener {
                filtroKm = null
                filtroPrecio = null
                filtroUbicacion = ""
                textoBuscado = ""

                kmEditText.text.clear()
                precioEditText.text.clear()
                ubicacionEditText.text.clear()
                searchView.setQuery("", false)
                searchView.clearFocus()

                filtrarTodos()
                dialog.dismiss()
            }

            dialog.show()
        }

        imageView.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

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

        db.collection("favoritos").whereEqualTo("subidoPor", userId).get()
            .addOnSuccessListener { documents ->
                listaFavoritos = documents.mapNotNull { it.getString("cocheId") }
                cargarCochesDesdeFirebase()
            }
    }

    private fun cargarCochesDesdeFirebase() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("coches").get().addOnSuccessListener { documents ->
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
                // Incluir coches que no sean del usuario actual
                if (coche.subidoPor != userId) {
                    listaCoches.add(coche)
                }
            }
            cocheAdapter.setFavoritos(listaFavoritos)
            cocheAdapter.notifyDataSetChanged()
        }
    }

    private fun filtrarTodos() {
        val listaFiltrada = listaCoches.filter { coche ->
            val kmOk = filtroKm == null || (coche.km.toIntOrNull() ?: Int.MAX_VALUE) <= filtroKm!!
            val precioOk = filtroPrecio == null || (coche.precio.toIntOrNull()
                ?: Int.MAX_VALUE) <= filtroPrecio!!
            val ubicacionOk = filtroUbicacion.isEmpty() || coche.ubicacion.contains(
                filtroUbicacion, ignoreCase = true
            )
            val textoOk = textoBuscado.isEmpty() || coche.marca.contains(
                textoBuscado, true
            ) || coche.modelo.contains(textoBuscado, true) || coche.ubicacion.contains(
                textoBuscado, true
            )
            kmOk && precioOk && ubicacionOk && textoOk
        }

        cocheAdapter = CocheAdapter(listaFiltrada).apply {
            setFavoritos(listaFavoritos)
        }
        recyclerView.adapter = cocheAdapter
    }

    private fun cargarImagenPerfil() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
            document.getString("profile_image")?.let { encoded ->
                val pureBase64 = encoded.substringAfter(",")
                val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                profileImageView.setImageBitmap(bitmap)
            }
        }
    }
}
