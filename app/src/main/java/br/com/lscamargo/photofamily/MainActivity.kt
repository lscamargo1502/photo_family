package br.com.lscamargo.photofamily

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        val mStorageRef = FirebaseStorage.getInstance().reference;
        val btnCarrega = findViewById<Button>(R.id.btnCarregar)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val imagem = findViewById<ImageView>(R.id.imgViewPhoto)
        val mprogress = findViewById<ProgressBar>(R.id.progressBar)

        val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val uri = data?.data

                    Glide.with(this)
                        .load(uri)
                        .into(imagem)

                }

            }


        fun chooseImageGallery() {

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory((Intent.CATEGORY_OPENABLE))
            }

            getContent.launch(intent)

        }


        btnEnviar.setOnClickListener {

            val sdf = SimpleDateFormat("DD:MM:HH:mm:ss", Locale.getDefault())
            val hora = Calendar.getInstance().time
            val nomearq = sdf.format(hora) + ".jpg"

            val arqRef =
                mStorageRef.child("images/arq" + nomearq) //Cria uma referencia para o nome do arquivo que irá subir

            val bitmap = (imagem.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            val dados = baos.toByteArray()

            val uploadTask = arqRef.putBytes(dados)

            //val uploadTask = arqRef.putFile(uri)

            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Toast.makeText(this, "Upload Falhou", Toast.LENGTH_SHORT).show()

            }.addOnSuccessListener { taskSnapshot ->
                Toast.makeText(this, "Upload OK", Toast.LENGTH_SHORT).show()

            }.addOnProgressListener {
                val atualizaprogress = 100.0 * it.bytesTransferred / it.totalByteCount
                mprogress.progress = atualizaprogress.toInt()
            }

        }

        btnCarrega.setOnClickListener {

            val IMAGE_CHOOSE = 1000;
            val PERMISSION_CODE = 1001;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                } else {
                    chooseImageGallery();
                    mprogress.progress = 0
                }
            } else {
                chooseImageGallery();
            }
        }

    }

    @Override
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {


        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.menuAbrirGaleria -> {

                val intent = Intent(this, ImagesActivity::class.java)

                startActivity(intent)

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}