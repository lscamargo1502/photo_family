package br.com.lscamargo.photofamily

//import android.R
import android.content.Context
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

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.image_view_upload)
        }
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

        Glide.with(holder.itemView)
            .load(url)
            .centerCrop()
            .into(holder.imageView)
    }

}
/*class ImageAdapter(context: Context, uploads: List<Upload>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    private val mContext: Context
    private val mUploads: List<Upload>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val v: View = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(v)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uploadCurrent: Upload = mUploads[position]
        holder.textViewName.setText(uploadCurrent.getName())

        Glide.with(mContext)
            .load(uploadCurrent.getImageUrl())
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return mUploads.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewName: TextView
        var imageView: ImageView

        init {
            textViewName = itemView.findViewById(R.id.text_view_name)
            imageView = itemView.findViewById(R.id.image_view_upload)
        }
    }

    init {
        mContext = context
        mUploads = uploads
    }
}*/