package com.example.musicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicapp.databinding.ActivityPlaylistBinding
import com.example.musicapp.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlaylistActivity : AppCompatActivity() {
private lateinit var binding: ActivityPlaylistBinding
private lateinit var adapter:PlayListViewAdapter

companion object{
    var musicPlaylist:MusicPlaylist=MusicPlaylist()

}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding= ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

     
        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.setItemViewCacheSize(12)
        binding.playlistRV.layoutManager= GridLayoutManager(this@PlaylistActivity,2)
        adapter= PlayListViewAdapter(this,playlistList = musicPlaylist.ref)
        binding.playlistRV.adapter=adapter

        binding.backbtnPLA.setOnClickListener{finish()}
        binding.addPlaylistBtn.setOnClickListener{customAlertDialog()}

    }

    //Adding dialog box in Playlist Activity while creating a Playlist
private fun customAlertDialog(){
    val customDialog= LayoutInflater.from(this@PlaylistActivity).inflate(R.layout.add_playlist_dialog,binding.root,false)
   val binder=AddPlaylistDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
            .setTitle("Playlist Details")
            .setPositiveButton("ADD"){dialog,_ ->
                val playlistName=binder.playlistName.text
                val createdBy=binder.yourName.text
                if(playlistName!=null && createdBy !=null){
                    if(playlistName.isNotEmpty() && createdBy.isNotEmpty()){
                        addPlaylist(playlistName.toString(),createdBy.toString())
                    }
                }
             dialog.dismiss()
            }.show()

}
    private fun addPlaylist(name :  String,createdBy : String){
var playlistExists =false
        for(i in musicPlaylist.ref){
            if(name.equals(i.name)){
                playlistExists=true
                break
            }
        }
        if(playlistExists) Toast.makeText(this,"Playlist Exist!!",Toast.LENGTH_SHORT).show()
        else{
            val tempPlaylist = Playlist()
            tempPlaylist.name=name
            tempPlaylist.playlist= ArrayList()
            tempPlaylist.createdBy=createdBy
            val calendar=java.util.Calendar.getInstance().time
            val sdf=SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn=sdf.format(calendar)
            musicPlaylist.ref.add(tempPlaylist)
            adapter.refreshPlaylist()
        }
    }

    override fun onResume() {
        adapter.notifyDataSetChanged()
        super.onResume()
    }
}