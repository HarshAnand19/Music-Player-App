package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicapp.databinding.ActivityFavoriteBinding

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var adapter: FavouriteAdapter

    companion object{
        var favouriteSongs:ArrayList<Music> = ArrayList()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding= ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favouriteSongs= checkPlaylist(favouriteSongs)


        binding.backbtnFA.setOnClickListener{finish()}

        binding.favouriteRV.setHasFixedSize(true)
        binding.favouriteRV.setItemViewCacheSize(12)
        binding.favouriteRV.layoutManager= GridLayoutManager(this,4)
        adapter= FavouriteAdapter(this, favouriteSongs)
        binding.favouriteRV.adapter=adapter

        if(favouriteSongs.size < 1) binding.shuffleBtnFA.visibility= View.INVISIBLE

        //shuffle Btn
        binding.shuffleBtnFA.setOnClickListener{
            val intent = Intent(this,PlayerActivity ::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","FavouriteShuffle")
            startActivity(intent)
        }

    }
}