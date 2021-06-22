package br.com.lscamargo.photofamily

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*


class ImagesActivity : AppCompatActivity() {

    private val storage = Firebase.storage
    private val listRef = storage.reference.child("/images")

    private val imageAdapter: ImageAdapter by lazy { ImageAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)

        val mRecyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = imageAdapter

        listImages()
    }

    private fun listImages() = CoroutineScope(Dispatchers.IO).launch {
        try {
            listRef.listAll()
                .addOnSuccessListener { (items, _) ->
                    imageAdapter.setList(items)
                }
                .addOnFailureListener {
                    Toast.makeText(this@ImagesActivity, "FALHOU!", Toast.LENGTH_SHORT).show()
                    // Uh-oh, an error occurred!
                }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ImagesActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}