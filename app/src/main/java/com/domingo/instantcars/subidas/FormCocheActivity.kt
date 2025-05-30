package com.domingo.instantcars.subidas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.domingo.instantcars.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.InputStream

class FormCocheActivity : AppCompatActivity() {

    private lateinit var brandEditText: EditText
    private lateinit var modelEditText: EditText
    private lateinit var yearSpinner: Spinner
    private lateinit var mileageEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var selectImageButton: Button
    private lateinit var ubispinner: Spinner
    private lateinit var previewImageView: ImageView
    private lateinit var registerCarButton: Button

    private var selectedImageBitmap: Bitmap? = null
    private val db = FirebaseFirestore.getInstance()

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                selectedImageBitmap = BitmapFactory.decodeStream(inputStream)
                previewImageView.setImageBitmap(selectedImageBitmap)
                previewImageView.visibility = ImageView.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.formulario_coche)

        brandEditText = findViewById(R.id.brand_edit_text)
        modelEditText = findViewById(R.id.model_edit_text)
        yearSpinner = findViewById(R.id.year_spinner)
        mileageEditText = findViewById(R.id.mileage_edit_text)
        priceEditText = findViewById(R.id.price_edit_text)
        descriptionEditText = findViewById(R.id.description_edit_text)
        selectImageButton = findViewById(R.id.button_select_image)
        previewImageView = findViewById(R.id.preview_image)
        registerCarButton = findViewById(R.id.button_register_car)
        ubispinner = findViewById(R.id.ubi_spinner)

        // Llenar el Spinner con los años en orden descendente
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val years = (2000..currentYear).map { it.toString() }.reversed()

        // Agregar un "placeholder" en la primera posición para que salga en el Spinner
        val yearsWithPlaceholder = mutableListOf("Año del vehículo") + years

        // Adaptador para el Spinner de año
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearsWithPlaceholder)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = adapter

        // Establecer el valor predeterminado en "Año del vehículo"
        yearSpinner.setSelection(0)

        val provincias = listOf(
            "Seleccione una provincia", "A Coruña", "Álava", "Albacete", "Alicante", "Almería",
            "Asturias", "Ávila", "Badajoz", "Barcelona", "Burgos", "Cáceres", "Cádiz", "Cantabria",
            "Castellón", "Ceuta", "Ciudad Real", "Córdoba", "Cuenca", "Girona", "Granada",
            "Guadalajara", "Guipúzcoa", "Huelva", "Huesca", "Islas Baleares", "Jaén", "La Rioja",
            "Las Palmas", "León", "Lleida", "Lugo", "Madrid", "Málaga", "Melilla", "Murcia",
            "Navarra", "Ourense", "Palencia", "Pontevedra", "Salamanca", "Santa Cruz de Tenerife",
            "Segovia", "Sevilla", "Soria", "Tarragona", "Teruel", "Toledo", "Valencia",
            "Valladolid", "Vizcaya", "Zamora", "Zaragoza"
        )

        // Adaptador para el Spinner de ubicación
        val ubicacionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provincias)
        ubicacionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ubispinner.adapter = ubicacionAdapter

        ubispinner.setSelection(0)


        selectImageButton.setOnClickListener {
            seleccionarImagenDesdeGaleria()
        }

        registerCarButton.setOnClickListener {
            subirDatosDelCoche()
        }
        //Evento para volver
        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }



    private fun seleccionarImagenDesdeGaleria() {
        selectImageLauncher.launch("image/*")
    }



    private fun convertirImagenABase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imagenBytes = outputStream.toByteArray()
        return Base64.encodeToString(imagenBytes, Base64.DEFAULT)
    }


    //Recoge los datos ingresados y los sube a Firestore.

    private fun subirDatosDelCoche() {
        val marca = brandEditText.text.toString().trim()
        val modelo = modelEditText.text.toString().trim()
        val year = yearSpinner.selectedItem?.toString() ?: ""
        val kilometraje = mileageEditText.text.toString().trim()
        val precio = priceEditText.text.toString().trim()
        val descripcion = descriptionEditText.text.toString().trim()
        val ubicacion = ubispinner.selectedItem?.toString() ?: ""


        if (marca.isEmpty() || modelo.isEmpty() || kilometraje.isEmpty() || precio.isEmpty() || descripcion.isEmpty() || selectedImageBitmap == null || year == "Año del vehículo" || ubicacion == "Seleccione su provincia") {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val imagenBase64 = selectedImageBitmap?.let { convertirImagenABase64(it) } ?: ""

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Acceder a Firestore y obtener el nombre de usuario
        val userRef = db.collection("users").document(userId)

        // Obtener el nombre de usuario
        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "Usuario desconocido"

                    // Crear el objeto coche
                    val coche = hashMapOf(
                        "marca" to marca,
                        "modelo" to modelo,
                        "año" to year,
                        "kilometraje" to kilometraje,
                        "precio" to precio,
                        "descripcion" to descripcion,
                        "imagen" to imagenBase64,
                        "subidoPor" to userId,
                        "subidoPorNombre" to username,
                        "ubicacion" to ubicacion
                    )
                    // Subir los datos a Firestore
                    db.collection("coches")
                        .add(coche)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Coche registrado con éxito", Toast.LENGTH_LONG)
                                .show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al subir los datos", Toast.LENGTH_SHORT)
                                .show()
                        }
                } else {
                    Toast.makeText(
                        this,
                        "No se pudo obtener el nombre de usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error al obtener los datos del usuario: $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}