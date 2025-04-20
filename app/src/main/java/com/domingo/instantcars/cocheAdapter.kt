package com.domingo.instantcars

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CocheAdapter(private val listaCoches: List<Coche>) :
    RecyclerView.Adapter<CocheAdapter.CocheViewHolder>() {

    private var favoritos = listOf<String>()
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    class CocheViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCoche: ImageView = itemView.findViewById(R.id.imgCoche)
        val txtMarca: TextView = itemView.findViewById(R.id.txtMarca)
        val txtModelo: TextView = itemView.findViewById(R.id.txtModelo)
        val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
        val btnFavorito: ImageButton = itemView.findViewById(R.id.btnFavorito)
        val cardCoche: View =
            itemView.findViewById(R.id.cardCoche) // La CardView que se va a hacer clickeable
    }

    fun setFavoritos(lista: List<String>) {
        favoritos = lista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CocheViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coche, parent, false)
        return CocheViewHolder(view)
    }

    override fun onBindViewHolder(holder: CocheViewHolder, position: Int) {
        val coche = listaCoches[position]

        // Asignar datos a las vistas
        holder.txtMarca.text = coche.marca
        holder.txtModelo.text = coche.modelo
        holder.txtPrecio.text = "Precio: €${coche.precio}"

        // Convertir Base64 a Bitmap para la imagen
        val bitmap = convertirBase64ABitmap(coche.imagen)

        if (bitmap != null) {
            holder.imgCoche.setImageBitmap(bitmap)
        } else {
            val drawable = AppCompatResources.getDrawable(holder.itemView.context, R.drawable.errorimagencoche)
            holder.imgCoche.setImageDrawable(drawable)
        }

        // Marca o desmarca corazón en función de los favoritos
        if (favoritos.contains(coche.id)) {
            holder.btnFavorito.setImageResource(R.drawable.baseline_favorite_24)
        } else {
            holder.btnFavorito.setImageResource(R.drawable.outline_favorite_24)
        }

        // Lógica de favoritos
        holder.btnFavorito.setOnClickListener {
            if (userId == null || coche.id == null) return@setOnClickListener

            db.collection("favoritos").whereEqualTo("subidoPor", userId)
                .whereEqualTo("cocheId", coche.id).get().addOnSuccessListener { docs ->
                    if (docs.isEmpty) {
                        val favorito = mapOf(
                            "subidoPor" to userId, "cocheId" to coche.id
                        )
                        db.collection("favoritos").add(favorito)
                        holder.btnFavorito.setImageResource(R.drawable.baseline_favorite_24)
                        animarFavorito(holder.btnFavorito)
                    } else {
                        for (doc in docs) {
                            db.collection("favoritos").document(doc.id).delete()
                        }
                        holder.btnFavorito.setImageResource(R.drawable.outline_favorite_24)
                        animarFavorito(holder.btnFavorito)
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            //Solo va cuando le doy a favoritos y la imagen no cuadra bien(POR CORREGIR)
        }
        holder.cardCoche.setOnClickListener {
            if (coche.id != null) {
                val intent = android.content.Intent(
                    holder.itemView.context, DetallesCocheActivity::class.java
                )
                intent.putExtra("cocheId", coche.id)  // le pasas el id
                holder.itemView.context.startActivity(intent)
            } else {
                Toast.makeText(
                    holder.itemView.context, "Error: coche sin ID", Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun getItemCount(): Int = listaCoches.size

    private fun convertirBase64ABitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun animarFavorito(view: ImageView) {
        val scaleAnimation = ScaleAnimation(
            0.7f,
            1.0f,
            0.7f,
            1.0f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5f
        )
        scaleAnimation.duration = 200
        view.startAnimation(scaleAnimation)
    }
}
