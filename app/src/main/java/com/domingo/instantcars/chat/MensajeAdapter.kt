package com.domingo.instantcars.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.domingo.instantcars.R
import java.text.SimpleDateFormat
import java.util.Locale

class MensajeAdapter(
    private val context: Context,
    private val mensajes: List<Mensaje>,
    private val senderId: String,
    private val receiverName: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val itemSent = 1
    private val itemReceived = 2

    override fun getItemViewType(position: Int): Int {
        return if (mensajes[position].senderId == senderId) itemSent else itemReceived
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == itemSent) {
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

        val currentDate = mensaje.timestamp?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
        }

        val previousDate = if (position > 0) {
            mensajes[position - 1].timestamp?.let {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
            }
        } else null

        val showDate = currentDate != previousDate

        if (holder is SentViewHolder) {
            holder.messageText.text = mensaje.text
            holder.timestamp.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                mensaje.timestamp ?: java.util.Date()
            )
            holder.date.visibility = if (showDate) View.VISIBLE else View.GONE
            holder.date.text = currentDate
        } else if (holder is ReceivedViewHolder) {
            holder.messageText.text = mensaje.text
            holder.timestamp.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                mensaje.timestamp ?: java.util.Date()
            )
            holder.date.visibility = if (showDate) View.VISIBLE else View.GONE
            holder.date.text = currentDate
            holder.username.text = receiverName
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
    }
}
