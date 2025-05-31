package com.domingo.instantcars.subidas

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
import com.domingo.instantcars.R
import com.domingo.instantcars.vehiculos.Coche
import com.domingo.instantcars.vehiculos.DetallesCocheActivity

class SubidasAdapter(
    private val listaSubidas: List<Coche>,
    private val onEliminarSubida: (Coche) -> Unit
) : RecyclerView.Adapter<SubidasAdapter.SubidaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubidaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subida_favorito, parent, false)
        return SubidaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubidaViewHolder, position: Int) {
        holder.bind(listaSubidas[position], onEliminarSubida)
    }

    override fun getItemCount(): Int = listaSubidas.size

    inner class SubidaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgCoche: ImageView = itemView.findViewById(R.id.imgCoche)
        private val txtMarca: TextView = itemView.findViewById(R.id.txtMarca)
        private val txtModelo: TextView = itemView.findViewById(R.id.txtModelo)
        private val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
        private val txtUbi: TextView = itemView.findViewById(R.id.txtUbi)
        private val btnEliminar: ImageButton = itemView.findViewById(R.id.btnFavorito)

        fun bind(coche: Coche, onEliminar: (Coche) -> Unit) {
            txtMarca.text = coche.marca
            txtModelo.text = coche.modelo
            txtPrecio.text = "${coche.precio} €"
            txtUbi.text = coche.ubicacion

            try {
                val bytes = Base64.decode(coche.imagen.substringAfter(","), Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgCoche.setImageBitmap(bitmap)
            } catch (_: Exception) {
                imgCoche.setImageResource(R.drawable.errorimagencoche)
            }

            itemView.setOnClickListener {
                val ctx = it.context
                val intent = Intent(ctx, DetallesCocheActivity::class.java)
                intent.putExtra("cocheId", coche.id)
                ctx.startActivity(intent)
            }

            btnEliminar.setImageResource(R.drawable.round_remove_circle_outline_24)
            btnEliminar.setOnClickListener {
                onEliminar(coche)
            }
        }
    }
}
