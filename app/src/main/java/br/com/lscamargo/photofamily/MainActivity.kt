package br.com.lscamargo.photofamily

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.com.lscamargo.photofamily.ui.login.LoginActivity
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions.DEFAULT_OPTIONS
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
//import com.google.android.gms.vision.label.internal.client.ImageLabelerOptions as ImageLabelerOptions1


class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        val user = mAuth.currentUser

        if (user != null) {
            Toast.makeText(
                applicationContext,
                "Bem vindo de volta " + user.email + "!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)

        }
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        val mStorageRef = FirebaseStorage.getInstance().reference;
        val btnCarrega = findViewById<Button>(R.id.btnCarregar)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val imagem = findViewById<ImageView>(R.id.imgViewPhoto)
        val mprogress = findViewById<ProgressBar>(R.id.progressBar)
        val textDaImagem = findViewById<TextView>(R.id.textDadosDaImagem)

        btnEnviar.isEnabled = false

        val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val uri = data?.data

                    Glide.with(this)
                        .load(uri)
                        .into(imagem)


                    val image: InputImage
                    try {
                        image = InputImage.fromFilePath(this, uri)
                        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                        labeler.process(image)
                            .addOnSuccessListener { labels ->
                                var text = ""
                                var confidence = 0
                                var index = 0

                                for (label in labels) {
                                    text = label.text + ", " + text
                                    //confidence = label.confidence.toInt()
                                    //index = label.index
                                }
                                textDaImagem.text = text

                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Processou e NAO encontrou algo na imagem", Toast.LENGTH_SHORT).show()

                            }


                    } catch (e: IOException) {
                        e.printStackTrace()
                    }


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

            if(imagem.drawable == null){
                Toast.makeText(this, "Selecione um arquivo para enviar", Toast.LENGTH_SHORT).show()

            }else {

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

                uploadTask.addOnFailureListener {
                    // Handle unsuccessful uploads
                    Toast.makeText(this, "Upload Falhou", Toast.LENGTH_SHORT).show()

                }.addOnSuccessListener { taskSnapshot ->
                    Toast.makeText(this, "Upload OK", Toast.LENGTH_SHORT).show()

                    btnEnviar.isEnabled = false


                }.addOnProgressListener {
                    val atualizaprogress = 100.0 * it.bytesTransferred / it.totalByteCount
                    mprogress.progress = atualizaprogress.toInt()
                }
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
                    btnEnviar.isEnabled = true
                }
            } else {
                chooseImageGallery()
                //if(imagem.drawable != null){
                btnEnviar.isEnabled = true

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
            R.id.menuLogoff ->{
                AlertDialog.Builder(this)
                    .setTitle("Fazer Logoff")
                    .setMessage("Fazer Logoff também fehcrá o aplicativo!\n Confirma?")
                    .setPositiveButton("SIM",

                        DialogInterface.OnClickListener { dialog, id ->
                            mAuth = FirebaseAuth.getInstance()
                            mAuth.signOut()
                            Toast.makeText(this, "Logoff efetuado!", Toast.LENGTH_SHORT).show()
                            finish()})

                    .setNegativeButton("Não",
                           DialogInterface.OnClickListener { dialog, id ->
                                    Toast.makeText(this, "Cancelado pelo usuário!", Toast.LENGTH_SHORT).show()
                           })
                    .show()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}