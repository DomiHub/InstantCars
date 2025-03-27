package com.domingo.instantcars

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.BitmapFactory
import java.util.concurrent.Executors

class CocheAdapter(private val listaCoches: List<Coche>) : RecyclerView.Adapter<CocheAdapter.CocheViewHolder>() {

    class CocheViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCoche: ImageView = itemView.findViewById(R.id.imgCoche)
        val txtMarca: TextView = itemView.findViewById(R.id.txtMarca)
        val txtModelo: TextView = itemView.findViewById(R.id.txtModelo)
        val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CocheViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coche, parent, false)
        return CocheViewHolder(view)
    }

    override fun onBindViewHolder(holder: CocheViewHolder, position: Int) {
        val coche = listaCoches[position]
        holder.txtMarca.text = coche.marca
        holder.txtModelo.text = coche.modelo
        holder.txtPrecio.text = "Precio: €${coche.precio}"

        //Cargar imagen
        cargarImagenDesdeFirebase(coche.imagen, holder.imgCoche)
    }

    override fun getItemCount(): Int = listaCoches.size

    //Función para cargar imagen manualmente desde URL (Firebase Storage)
    private fun cargarImagenDesdeFirebase(urlImagen: String, imageView: ImageView) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream = java.net.URL(urlImagen).openStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Mostrar la imagen en el hilo principal
                imageView.post {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Si falla, usa una imagen por defecto
                imageView.post {
                    imageView.setImageResource(R.drawable.errorimagencoche)
                }
            }
        }
    }
}
