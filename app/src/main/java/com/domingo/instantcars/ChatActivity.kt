package com.domingo.instantcars

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        chatId = intent.getStringExtra("chatId")
        receiverId = intent.getStringExtra("receiverId")
        senderId = FirebaseAuth.getInstance().currentUser?.uid

        messageList = ArrayList()
        messageAdapter = MensajeAdapter(this, messageList, senderId!!)
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

                    // Guarda el mensaje en la subcolección "mensajes"
                    db.collection("chats").document(chatId!!).collection("mensajes")
                        .add(mensaje)
                        .addOnSuccessListener {
                            // Actualiza el último mensaje del chat
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
