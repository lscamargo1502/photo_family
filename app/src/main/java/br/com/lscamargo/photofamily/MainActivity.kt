package br.com.lscamargo.photofamily

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import androidx.camera.core.*
import androidx.camera.core.Preview.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var mAuth: FirebaseAuth
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        val orientationEventListener = object : OrientationEventListener(this as Context) {
            override fun onOrientationChanged(orientation : Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation : Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageCapture?.targetRotation = rotation
            }
        }
        orientationEventListener.enable()

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
        val btnCapturarFoto = findViewById<Button>(R.id.btnCapturarFoto)
        val viewFinder = findViewById<PreviewView>(R.id.viewFinder)
        viewFinder.isVisible = true
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
                    viewFinder.isVisible = true



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
                    viewFinder.isVisible = false
                }
            } else {
                chooseImageGallery()
                //if(imagem.drawable != null){
                btnEnviar.isEnabled = true
                viewFinder.isVisible = false

            }
        }
        btnCapturarFoto.setOnClickListener {
            takePhoto()
            viewFinder.isVisible = false

        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissões nao concedidas pelo usuário!:(",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }
    companion object {
        private const val TAG = "SPC"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, "Photo Family").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)



        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val viewFinder = findViewById<PreviewView>(R.id.viewFinder)
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Builder()
                .build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(480, 640))

                .build()


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        // Set up image capture listener, which is triggered after photo has
        // been taken


        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Captura falhou :(: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Foto salva com sucesso! :)"
                    val imagem = findViewById<ImageView>(R.id.imgViewPhoto)
                    val viewFinder = findViewById<PreviewView>(R.id.viewFinder)
                    val textDaImagem = findViewById<TextView>(R.id.textDadosDaImagem)
                    val btnEnviar = findViewById<Button>(R.id.btnEnviar)

                    val imageUri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "br.com.lscamargo.photofamily.provider",
                        photoFile
                    )
                    Glide.with(this@MainActivity)
                        .load(imageUri)
                        .into(imagem)


                    val image: InputImage
                    try {
                        image = InputImage.fromFilePath(this@MainActivity, imageUri)
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
                                btnEnviar.isEnabled = true


                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@MainActivity, "Processou e NAO encontrou algo na imagem", Toast.LENGTH_SHORT).show()

                            }


                    } catch (e: IOException) {
                        e.printStackTrace()
                    }


                }
            })
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