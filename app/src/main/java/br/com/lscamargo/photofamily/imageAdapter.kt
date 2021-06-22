package br.com.lscamargo.photofamily

//import android.R
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class ImageAdapter(
    val urls: List<String>
): RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imageView: ImageView = itemView.findViewById(R.id.image_view_upload)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.image_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return urls.size
    }


    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = urls[position]

        Log.e("teste:::", url )

        Glide.with(holder.itemView)
            .load(url)
            .centerCrop()
            .into(holder.imageView)
    }

}
