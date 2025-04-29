package com.domingo.instantcars

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritosAdapter(
    private val listaFavoritos: List<Coche>,
    private val onEliminarFavorito: (Coche) -> Unit
) : RecyclerView.Adapter<FavoritosAdapter.FavoritoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subida_favorito, parent, false)
        return FavoritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {
        holder.bind(listaFavoritos[position], onEliminarFavorito)
    }

    override fun getItemCount(): Int = listaFavoritos.size

    class FavoritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgCoche: ImageView = itemView.findViewById(R.id.imgCoche)
        private val txtMarca: TextView = itemView.findViewById(R.id.txtMarca)
        private val txtModelo: TextView = itemView.findViewById(R.id.txtModelo)
        private val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
        private val txtUbi: TextView = itemView.findViewById(R.id.txtUbi)
        private val btnFavorito: ImageButton = itemView.findViewById(R.id.btnFavorito)

        fun bind(coche: Coche, onEliminarFavorito: (Coche) -> Unit) {
            txtMarca.text = coche.marca
            txtModelo.text = coche.modelo
            txtPrecio.text = coche.precio
            txtUbi.text = coche.ubicacion

            // Cargar la imagen del coche
            try {
                val imageBytes = Base64.decode(coche.imagen.substringAfter(","), Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                imgCoche.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imgCoche.setImageResource(R.drawable.coche)  // Imagen por defecto en caso de error
            }

            // Clic para eliminar de favoritos
            btnFavorito.setOnClickListener {
                onEliminarFavorito(coche)
            }

            // Clic en toda la tarjeta para abrir detalles
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetallesCocheActivity::class.java)
                intent.putExtra("cocheId", coche.id)  // Pasamos el id del coche
                context.startActivity(intent)
            }
        }
    }
}
