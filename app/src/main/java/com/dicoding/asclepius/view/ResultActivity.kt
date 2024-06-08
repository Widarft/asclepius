package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        val label = intent.getStringExtra("label")
        val confidence = intent.getFloatExtra("confidence", 0f)

        binding.resultImage.load(imageUri) {
            crossfade(true)
            placeholder(R.drawable.ic_place_holder)
        }

        val confidencePercent = (confidence * 100).toInt()
        val resultText = getString(R.string.prediction_result, label) +
                "\n" + getString(R.string.confidence_score, confidencePercent)
        binding.resultText.text = resultText
    }
}