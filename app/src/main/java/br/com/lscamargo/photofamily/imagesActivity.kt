package br.com.lscamargo.photofamily

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.awaitAll
import java.util.Arrays.toString


class imagesActivity : AppCompatActivity() {

    val storage = Firebase.storage
    val listRef = storage.reference.child("/images")

    private var imageAdapter: ImageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)

        val mRecyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = imageAdapter

        val imageUrls = ArrayList<String>()

        listRef.listAll()
            .addOnSuccessListener { (items, prefixes) ->
                prefixes.forEach { prefix ->

                    Log.e("LISTA!: ", prefix.path)
                    Toast.makeText(this, prefix.path, Toast.LENGTH_SHORT).show()
                }

                items.forEach { item ->
                    val url = item.downloadUrl.addOnSuccessListener {
                        Log.e("LISTA!: ", it.toString())
                        imageUrls.add(it.toString())
                    }
                }

            }
            .addOnFailureListener {
                Toast.makeText(this, "FALHOU!", Toast.LENGTH_SHORT).show()
                // Uh-oh, an error occurred!
            }

        imageAdapter = ImageAdapter(imageUrls)
        mRecyclerView.adapter = imageAdapter
    }
}