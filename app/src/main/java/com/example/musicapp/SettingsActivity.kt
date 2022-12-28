package com.example.musicapp

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {

    lateinit var binding:ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding= ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title="Settings"

        when(MainActivity.themeIndex){
            0 -> binding.coolpinktheme.setBackgroundColor(Color.YELLOW)
            1 -> binding.coolbluetheme.setBackgroundColor(Color.YELLOW)
            2 -> binding.coolpurpletheme.setBackgroundColor(Color.YELLOW)
            3 -> binding.coolgreentheme.setBackgroundColor(Color.YELLOW)
            4 -> binding.coolblacktheme.setBackgroundColor(Color.YELLOW)

        }
        binding.coolpinktheme.setOnClickListener{ saveTheme(0) }
        binding.coolbluetheme.setOnClickListener{ saveTheme(1) }
        binding.coolpurpletheme.setOnClickListener{ saveTheme(2) }
        binding.coolgreentheme.setOnClickListener{ saveTheme(3) }
        binding.coolblacktheme.setOnClickListener{ saveTheme(4) }
        binding.versionName.text=setVersionDetails()
        binding.sortBtn.setOnClickListener{
            val menuList = arrayOf("Recently Added","Song Title","File Size")
            var currentSort=MainActivity.sortOrder
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Sorting")
                .setPositiveButton("OK"){_,_ ->
                    val editor =getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("sortOrder",currentSort)
                    editor.apply()
                }
                .setSingleChoiceItems(menuList,currentSort){_,which->
                    currentSort=which
                }
            val customDialog =builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        }
    }

    //save theme for app
    private fun saveTheme(index:Int){
        if(MainActivity.themeIndex != index){
            val editor =getSharedPreferences("THEMES", MODE_PRIVATE).edit()
            editor.putInt("themeIndex",index)
            editor.apply()
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Apply Theme")
                .setMessage("Do you want to apply this theme?")
                .setPositiveButton("YES"){_,_ ->
                    exitApplication()
                }
                .setNegativeButton("No"){ dialog,_ ->
                    dialog.dismiss()
                }
            val customDialog =builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)

        }
    }
    private fun setVersionDetails():String{
        return "Version Name : ${BuildConfig.VERSION_NAME}"
    }
}