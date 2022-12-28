package com.example.musicapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {
    lateinit var binding:ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding= ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title="Feedback"

        val feedbackMsg=binding.feedbackmsgFA.text.toString()+"\n"+binding.emailFA.text.toString()
        val subject=binding.topicFA.text.toString()
binding.sendFA.setOnClickListener{
    Toast.makeText(this,"Your Feedback sent",Toast.LENGTH_SHORT).show()
}

    }
}