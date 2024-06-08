package com.dicoding.asclepius.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermission()

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let { uri ->
                analyzeImage(uri)
            } ?: showToast("No image selected")
        }
    }

    private fun checkPermission() {
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun startGallery() {
        launcherGallery.launch("image/*")
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            currentImageUri = uri
            showImage()
        } ?: showToast("No media selected")
    }

    private fun showImage() {
        binding.previewImageView.setImageURI(currentImageUri)
        binding.analyzeButton.visibility = android.view.View.VISIBLE
        binding.galleryButton.apply {
            text = resources.getString(R.string.replace_image)
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
        }
    }

    private val classifierListener = object : ImageClassifierHelper.ClassifierListener {
        override fun onError(error: String) {
            showToast(error)
            binding.progressIndicator.visibility = View.GONE
        }

        override fun onResults(results: List<Classifications>?) {
            if (!results.isNullOrEmpty()) {
                val topResult = results[0].categories.firstOrNull()
                if (topResult != null) {
                    val label = topResult.label
                    val confidence = topResult.score
                    val intent = Intent(this@MainActivity, ResultActivity::class.java)
                    intent.putExtra("imageUri", currentImageUri.toString())
                    intent.putExtra("label", label)
                    intent.putExtra("confidence", confidence)
                    startActivity(intent)
                } else {
                    showToast("No results found")
                }
            } else {
                showToast("No results found")
            }
            binding.progressIndicator.visibility = View.GONE
        }
    }

    private fun analyzeImage(imageUri: Uri) {
        binding.progressIndicator.visibility = View.VISIBLE
        val helper = ImageClassifierHelper(
            context = this,
            classifierListener = classifierListener
        )
        helper.classifyStaticImage(imageUri)
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}