package com.example.musicapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    lateinit var binding:ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding= ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title="About"
        binding.aboutText.text=aboutText()
    }
    private fun aboutText():String{
        return "Developed by Harsh Anand"+
                "\n\n if you want to provide any feedback,I would love to hear that!"
    }
}