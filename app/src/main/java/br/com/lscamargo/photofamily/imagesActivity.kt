package br.com.lscamargo.photofamily

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


class imagesActivity : AppCompatActivity() {

    //private val imageRef = Firebase.storage.reference

    val storage = Firebase.storage
    val listRef = storage.reference.child("/images")
    //private val mAdapter: ImageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)

        val mRecyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.setLayoutManager(LinearLayoutManager(this))
        //mRecyclerView.adapter = mAdapter

        //var mUploads = ArrayList<String>()

        //val mDatabaseRef = FirebaseStorage.getInstance().getReference("images")

        //val lista = mDatabaseRef.listAll()
        val imageUrls = ArrayList<String>()

        listRef.listAll()
            .addOnSuccessListener { (items, prefixes) ->
                prefixes.forEach { prefix ->
                    // All the prefixes under listRef.
                    // You may call listAll() recursively on them.

                    Log.e("LISTA!: ", prefix.path)
                    Toast.makeText(this, prefix.path, Toast.LENGTH_SHORT).show()
                }


                items.forEach { item ->
                    //Log.e("ITEM!: ",item.downloadUrl.)

                    val url = item.downloadUrl

                    Log.e("LISTA!: ", item.toString())
                    imageUrls.add(url.toString())

                }
                val imageAdapter = ImageAdapter(imageUrls)
                mRecyclerView.adapter = imageAdapter

            }
            .addOnFailureListener {
                Toast.makeText(this, "FALHOU!", Toast.LENGTH_SHORT).show()
            // Uh-oh, an error occurred!
            }

    }


}