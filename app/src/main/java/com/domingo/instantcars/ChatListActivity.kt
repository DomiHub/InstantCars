package com.domingo.instantcars

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private lateinit var chatList: MutableList<ChatPreview>

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_select)

        recyclerView = findViewById(R.id.recycler_view_chats)
        chatList = mutableListOf()
        adapter = ChatListAdapter(this, chatList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        obtenerChats()
    }

    private fun obtenerChats() {
        if (currentUserId == null) return

        db.collection("chats")
            .whereArrayContains("userIds", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatList", "Error al obtener chats", error)
                    return@addSnapshotListener
                }

                chatList.clear()
                snapshot?.forEach { doc ->
                    val chat = doc.toObject(ChatPreview::class.java)
                    chat.chatId = doc.id
                    chatList.add(chat)
                }
                adapter.notifyDataSetChanged()
            }
    }
}
