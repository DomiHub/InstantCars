package com.domingo.instantcars

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale


class MensajeAdapter(
    private val context: Context,
    private val mensajes: List<Mensaje>,
    private val senderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_SENT = 1
    private val ITEM_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        return if (mensajes[position].senderId == senderId) ITEM_SENT else ITEM_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_me, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_other, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun getItemCount(): Int = mensajes.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mensaje = mensajes[position]

        if (holder is SentViewHolder) {
            holder.messageText.text = mensaje.text
            holder.timestamp.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                mensaje.timestamp ?: java.util.Date()
            )
            // Opcional: actualizar fecha también si la usas
            holder.date.text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(
                mensaje.timestamp ?: java.util.Date()
            )
        } else if (holder is ReceivedViewHolder) {
            holder.messageText.text = mensaje.text
            holder.timestamp.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                mensaje.timestamp ?: java.util.Date()
            )
            holder.date.text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(
                mensaje.timestamp ?: java.util.Date()
            )
            holder.username.text = "Usuario" //Usar el nombre real si lo tienes
            // holder.profileImage.setImageResource(...) //Para usar imagen
        }
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.text_gchat_message_me)
        val timestamp: TextView = itemView.findViewById(R.id.text_gchat_timestamp_me)
        val date: TextView = itemView.findViewById(R.id.text_gchat_date_me)
    }

    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.text_gchat_message_other)
        val timestamp: TextView = itemView.findViewById(R.id.text_gchat_timestamp_other)
        val date: TextView = itemView.findViewById(R.id.text_gchat_date_other)
        val username: TextView = itemView.findViewById(R.id.text_gchat_user_other)
        val profileImage: ImageView = itemView.findViewById(R.id.image_gchat_profile_other)
    }
}
