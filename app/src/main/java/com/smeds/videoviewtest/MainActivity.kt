package com.smeds.videoviewtest

import android.bluetooth.BluetoothClass
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.withMatrix
import com.smeds.videoviewtest.devicedata.DeviceInfo
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer


class MainActivity : AppCompatActivity() {

    lateinit var videoView : VideoView
    lateinit var mediaController: MediaController
    lateinit var imageView : ImageView
    lateinit var canvas : Canvas
    lateinit var paint : Paint
    lateinit var textPaint : Paint
    lateinit var d : DeviceInfo

    // Queste verranno inviate dal server una volta finita l'elaborazione in backend
    var translateX : Float = -157f
    var translateY : Float = -197f
    var sizeOfImageInInchesX = 10
    var sizeOfImageInInchesY = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        // Ottieni informazioni sul dispositivo
        d = DeviceInfo(windowManager)

        // Esporta info su JSON
        var json = JSONObject()
        var deviceName = (Build.MODEL).toString().replace(" ", "") + "-smeds"
        json.put(deviceName, d.toJsonObject())

        val fileName = deviceName
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (storageDir != null) {
            if (!storageDir.exists()) {
                storageDir.mkdir()
            }
        }

        // Il file sta su /emulated/0/Android/data/com.smeds.videoviewtest/Documents
        val file = File.createTempFile(fileName, ".json", storageDir)
        val output : Writer = BufferedWriter(FileWriter(file))
        output.write(json.toString())
        output.close()


        // Fuffa classica per la ImageView
        imageView = findViewById(R.id.imageView)
        imageView.maxWidth =  d.screenWidthDp
        imageView.maxHeight = d.screenHeightDp
        val bitmap = Bitmap.createBitmap(d.screenWidthDp, d.screenHeightDp, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        paint = Paint()
        paint.apply {
            color = Color.WHITE
            strokeWidth = 10f
            style = Paint.Style.STROKE
        }
        textPaint = Paint()
        textPaint.apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            textSize = 20f
        }

        imageView.setImageBitmap(bitmap)

        draw()

// -------------------- PER LA VIDEOVIEW
//        videoView = findViewById(R.id.videoView)
//        mediaController = MediaController(this)
//        mediaController.setAnchorView(this.videoView)
//
//        videoView.setMediaController(mediaController)
//        videoView.rotation = 45f
//
//        videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + R.raw.sample))
//        videoView.requestFocus()
//        videoView.start()
//
//        videoView.setOnCompletionListener {
//            Toast.makeText(applicationContext, "Video is over", Toast.LENGTH_SHORT).show()
//        }
// ------------------- PER LA VIDEOVIEW

        draw()

    }

    private fun clean (){
        canvas.drawARGB(255, 255, 0, 0)
    }

    private fun draw() {
        clean()

        // Prendi l'immagine
        val image = BitmapFactory.decodeStream(
            this.assets.open("snow-purp.png")
        )

        // Rendi l'immagine tanto larga quanto il telefono
        val originalWidth: Float = image.getWidth().toFloat()
        val originalHeight: Float = image.getHeight().toFloat()
        val phoneScaleX: Float = d.screenWidthDp / originalWidth
        val phoneScaleY = d.screenHeightDp / originalHeight
//        val xTranslation = 0.0f
//        val yTranslation: Float = (d.screenHeightDp - originalHeight * phoneScale) / 2.0f


        // Scala l'immagine per arrivare alle dimensioni fisiche desiderate
        val finalScaleX = (sizeOfImageInInchesX / d.screenWidthInch).toFloat()
        val finalScaleY = (sizeOfImageInInchesY / d.screenHeightInch).toFloat()

        // Trasla l'immagine alle coordinate desiderate
        // ho bisogno del * 100 perchè l'immagine originale è 1000 x 1000
        val finalTranslateX = (originalWidth / (sizeOfImageInInchesX * 100)) * translateX
        val finalTranslateY = (originalHeight / (sizeOfImageInInchesY * 100)) * translateY


        val transformation = Matrix()
        transformation.preScale(phoneScaleX, phoneScaleY)
        transformation.postScale(finalScaleX, finalScaleY)
        transformation.preTranslate(finalTranslateX, finalTranslateY)

        Log.d("INFO", "Matrix: $transformation")

        // Applica la trasformazione
        canvas.withMatrix(transformation){
            canvas.drawBitmap(image, 0f, 0f, null)
        }



        // Info a schermo
        var text : String = "${d.toString()}\n" +
                "Image Width: ${image.getScaledWidth(canvas)}\n" +
                "Image Height: ${image.getScaledHeight(canvas)}"

        val x = 20
        var y = 15
        for (line in text.split("\n".toRegex()).toTypedArray()) {
            canvas.drawText(line, x.toFloat(), y.toFloat(), textPaint)
            y += (textPaint.descent() - textPaint.ascent()).toInt()
        }


    }




}