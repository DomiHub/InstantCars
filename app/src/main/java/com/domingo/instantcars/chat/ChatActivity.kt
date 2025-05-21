package com.domingo.instantcars.chat

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.domingo.instantcars.profile.OtherProfileActivity
import com.domingo.instantcars.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageButton

    private lateinit var messageList: ArrayList<Mensaje>
    private lateinit var messageAdapter: MensajeAdapter

    private val db = FirebaseFirestore.getInstance()
    private var chatId: String? = null
    private var receiverId: String? = null
    private var senderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_list_activity)

        recyclerView = findViewById(R.id.recycler_gchat)
        messageBox = findViewById(R.id.edit_gchat_message)
        sendButton = findViewById(R.id.button_gchat_send)

        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        chatId = intent.getStringExtra("chatId")
        receiverId = intent.getStringExtra("receiverId")
        senderId = FirebaseAuth.getInstance().currentUser?.uid

        val nombreTextView = findViewById<TextView>(R.id.user_name)
        val imagenPerfil = findViewById<ShapeableImageView>(R.id.profile_image)

        // Ir al perfil del receptor
        val irAOtroPerfil = {
            receiverId?.let {
                val intent = Intent(this, OtherProfileActivity::class.java)
                intent.putExtra("userId", it)
                startActivity(intent)
            }
        }

        nombreTextView.setOnClickListener { irAOtroPerfil() }
        imagenPerfil.setOnClickListener { irAOtroPerfil() }


        // Carga el nombre e imagen del receptor y luego inicia la conversación
        receiverId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    val receiverName = document.getString("username") ?: "Usuario"
                    nombreTextView.text = receiverName

                    val encodedImage = document.getString("profile_image")
                    if (!encodedImage.isNullOrEmpty()) {
                        try {
                            val base64Data = encodedImage.substringAfter(",")
                            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                            val bitmap =
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            imagenPerfil.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            Log.e("ChatActivity", "Error al decodificar imagen: ${e.message}")
                        }
                    }

                    // Solo cuando ya tenemos el nombre del receptor, inicializamos el adaptador
                    messageList = ArrayList()
                    messageAdapter = MensajeAdapter(this, messageList, senderId!!, receiverName)
                    recyclerView.adapter = messageAdapter
                    recyclerView.layoutManager = LinearLayoutManager(this)

                    if (chatId != null && senderId != null) {
                        escucharMensajes(chatId!!)

                        sendButton.setOnClickListener {
                            val mensajeTexto = messageBox.text.toString().trim()
                            if (mensajeTexto.isNotEmpty()) {
                                val mensaje = hashMapOf(
                                    "senderId" to senderId,
                                    "text" to mensajeTexto,
                                    "timestamp" to Timestamp.now()
                                )

                                db.collection("chats").document(chatId!!).collection("mensajes")
                                    .add(mensaje)
                                    .addOnSuccessListener {
                                        db.collection("chats").document(chatId!!)
                                            .update(
                                                mapOf(
                                                    "lastMessage" to mensajeTexto,
                                                    "timestamp" to Timestamp.now()
                                                )
                                            )
                                    }
                                    .addOnFailureListener {
                                        Log.e("Chat", "Error al enviar mensaje: ${it.message}")
                                    }

                                messageBox.text.clear()
                            }
                        }
                    }
                }
        }
    }

    private fun escucharMensajes(chatId: String) {
        db.collection("chats").document(chatId).collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Chat", "Error al recibir mensajes: ${error.message}")
                    return@addSnapshotListener
                }

                messageList.clear()
                for (doc in value!!) {
                    val mensaje = Mensaje(
                        doc.getString("senderId") ?: "",
                        doc.getString("text") ?: "",
                        doc.getTimestamp("timestamp")?.toDate()
                    )
                    messageList.add(mensaje)
                }

                messageAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messageList.size - 1)
            }
    }
}
