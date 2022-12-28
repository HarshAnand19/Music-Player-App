package com.example.musicapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.databinding.ActivityPlaylistDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlaylistDetails : AppCompatActivity() {
    lateinit var binding:ActivityPlaylistDetailsBinding
    lateinit var adapter: MusicAdapter

    companion object{
        var currentPlaylistPos:Int=-1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding= ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlaylistPos=intent.extras?.get("index") as Int

        try {
            PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist =
                checkPlaylist(playlist = PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist)
        }catch (e:Exception){}
        binding.playlistDetailsRV.setItemViewCacheSize(10)
        binding.playlistDetailsRV.setHasFixedSize(true)
        binding.playlistDetailsRV.layoutManager=LinearLayoutManager(this)
        adapter= MusicAdapter(this,PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist,playlistDetails = true)
        binding.playlistDetailsRV.adapter=adapter

        binding.backbtnPD.setOnClickListener{finish()}

        //shuffle Btn
        binding.shuffleBtnPD.setOnClickListener{
            val intent = Intent(this,PlayerActivity ::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","PlaylistDetailsShuffle")
            startActivity(intent)
        }
binding.addBtnPD.setOnClickListener{
   startActivity(Intent(this,SelectionActivity::class.java))
}
       binding.removeAllBtnPD.setOnClickListener{
           val builder = MaterialAlertDialogBuilder(this)
           builder.setTitle("Remove")
               .setMessage("Do you want to remove all songs from Playlist?")
               .setPositiveButton("YES"){dialog,_ ->
                 PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist.clear()
                   adapter.refreshPlaylist()
                   dialog.dismiss()
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

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
    binding.playlistNamePD.text=PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].name
        binding.moreInfoPD.text="Total ${adapter.itemCount} Songs.\n\n"+
                                 "Created On :\n${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createdOn}\n\n"+
                                   " -- ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createdBy}"

        if(adapter.itemCount>0){
            Glide.with(this)
                .load(PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
                .into(binding.playlistImgPD)

            binding.shuffleBtnPD.visibility= View.VISIBLE
        }
adapter.notifyDataSetChanged()

        //for storing favourite data using shared preferences
        val editor=getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonStringPlaylist= GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist",jsonStringPlaylist)
        editor.apply()
    }

}