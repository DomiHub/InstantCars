package com.domingo.instantcars

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

    private fun cargarNumeroSubidas(uid: String) {
        db.collection("coches").whereEqualTo("subidoPor", uid).get()
            .addOnSuccessListener { result ->
                val total =
                    result.size()
                uploadLabel.text = String.format(Locale.getDefault(), "%d", total)
            }
    }
}
