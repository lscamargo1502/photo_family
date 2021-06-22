package br.com.lscamargo.photofamily

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.StorageReference


class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.image_view_upload)
        private val progress: ProgressBar = itemView.findViewById(R.id.progress)

        fun bind(url: String) {

            Glide.with(itemView)
                .load(url)
                .centerCrop()
                .into(imageView)
            progress.visibility = View.GONE
        }
    }

    private val urls: MutableList<StorageReference> = mutableListOf()

    fun setList(urls: List<StorageReference>) {
        this.urls.clear()
        this.urls.addAll(urls)
        notifyDataSetChanged()
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
        urls[position].downloadUrl.addOnSuccessListener {
            holder.bind(it.toString())
        }
    }

}
