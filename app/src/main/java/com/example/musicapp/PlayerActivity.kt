package com.example.musicapp

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(),ServiceConnection ,MediaPlayer.OnCompletionListener{

    companion object{
        lateinit var musicListPA:ArrayList<Music>
        var songPosition:Int=0
        var isPlaying:Boolean=false
         var musicService:MusicService?=null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding:ActivityPlayerBinding
        var repeat:Boolean=false
        var min15: Boolean=false
        var min30: Boolean=false
        var min60: Boolean=false
        var nowPlayingId: String=""
        var isFavourite:Boolean =false
        var fIndex:Int=-1

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding= ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

if(intent.data?.scheme.contentEquals("content")){
    val intentService=Intent(this,MusicService::class.java)
    bindService(intentService,this, BIND_AUTO_CREATE)
    startService(intentService)
    musicListPA= ArrayList()
    musicListPA.add(getMusicDetails(intent.data!!))

    Glide.with(this)
        .load(getImgArt(musicListPA[songPosition].path))
        .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
        .into(binding.songImgPA)
    binding.songNamePA.text= musicListPA[songPosition].title
}
        else{
    InitializeLayout()
        }


        //Back Button in PA
        binding.backbtnPA.setOnClickListener{finish()}

        binding.playPauseButtonPA.setOnClickListener{
            if(isPlaying) pauseMusic()

            else playMusic()
        }
        binding.previousBtnPA.setOnClickListener{
            prevNextSong(increment = false)
        }
    binding.nextButtonPA.setOnClickListener{
        prevNextSong(increment = true)
    }

      binding.seekbarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
          override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
             if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
          }

          override fun onStartTrackingTouch(p0: SeekBar?) =Unit

          override fun onStopTrackingTouch(p0: SeekBar?) =Unit
      })

        //repeat functionality
        binding.repeatButtonPA.setOnClickListener{
            if (!repeat){
                repeat=true
                binding.repeatButtonPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            Toast.makeText(this,"Repeat mode on!",Toast.LENGTH_SHORT).show()
            }
            else{
                repeat=false
                binding.repeatButtonPA.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
                Toast.makeText(this,"Repeat mode off!",Toast.LENGTH_SHORT).show()
            }
        }

   //Equalizer btn PA
        binding.equalizerBtnPA.setOnClickListener{
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 7)
            }catch (e:Exception){
                Toast.makeText(this,"Equalizer Feature Not Supported!",Toast.LENGTH_SHORT).show()
            }
            }

        //Timer btn PA
        binding.timerbtnPA.setOnClickListener{
            val timer =min15 || min30 || min60
            if (!timer) {
                showBottomSheetDialog()
            }else{
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop Timer?")
                    .setPositiveButton("YES"){_,_ ->
                        min15=false
                        min30=false
                        min60=false
                        binding.timerbtnPA.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
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

    //Share Button PA
        binding.shareBtnPA.setOnClickListener{
            val shareIntent=Intent()
            shareIntent.action=Intent.ACTION_SEND
            shareIntent.type="audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent,"Sharing Music File!"))
        }

        //Favourite Button PA
        binding.favouriteBtnPA.setOnClickListener{
            fIndex = favouriteChecker(musicListPA[songPosition].id)
            if(isFavourite){
                isFavourite = false
                binding.favouriteBtnPA.setImageResource(R.drawable.favorite_empty)
                FavoriteActivity.favouriteSongs.removeAt(fIndex)
            } else{
                isFavourite = true
                binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
                FavoriteActivity.favouriteSongs.add(musicListPA[songPosition])
            }
        }
    }

private fun setLayout(){
    fIndex= favouriteChecker(musicListPA[songPosition].id)

    Glide.with(this)
        .load(musicListPA[songPosition].artUri)
        .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
        .into(binding.songImgPA)

    binding.songNamePA.text= musicListPA[songPosition].title
    if(repeat)  binding.repeatButtonPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
    if (min15 || min30 || min60) binding.timerbtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))

    //add to favourite(icon setting)
    if(isFavourite) binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
    else binding.favouriteBtnPA.setImageResource(R.drawable.favorite_empty)

}

  private fun createMediaPlayer(){
      try {
          if(musicService!!.mediaPlayer == null) musicService!!.mediaPlayer= MediaPlayer()
          musicService!!.mediaPlayer!!.reset()
          musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
          musicService!!.mediaPlayer!!.prepare()
          musicService!!.mediaPlayer!!.start()
          isPlaying=true
          binding.playPauseButtonPA.setIconResource(R.drawable.pause_icon)
          musicService!!.showNotification(R.drawable.pause_icon)
          binding.tvSeekbarstart.text= formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
          binding.tvSeekbarend.text= formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
         binding.seekbarPA.progress=0
          binding.seekbarPA.max= musicService!!.mediaPlayer!!.duration
          musicService!!.mediaPlayer!!.setOnCompletionListener(this)
          nowPlayingId= musicListPA[songPosition].id
      }catch (e:Exception){
          return
      }
  }

private fun InitializeLayout(){
    songPosition=intent.getIntExtra("index",0)
    when(intent.getStringExtra("class")){
        "FavouriteAdapter" ->{
            val intent=Intent(this,MusicService::class.java)
            bindService(intent,this, BIND_AUTO_CREATE)
            startService(intent)
            musicListPA= ArrayList()
            musicListPA.addAll(FavoriteActivity.favouriteSongs)
            setLayout()
        }

        "NowPlaying" ->{
            setLayout()
            binding.tvSeekbarstart.text= formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekbarend.text= formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekbarPA.progress= musicService!!.mediaPlayer!!.currentPosition
            binding.seekbarPA.max= musicService!!.mediaPlayer!!.duration
            if(isPlaying) binding.playPauseButtonPA.setIconResource(R.drawable.pause_icon)
            else binding.playPauseButtonPA.setIconResource(R.drawable.play_icon)

        }
        "MusicAdapterSearch" ->{
            //For starting Service
            val intent=Intent(this,MusicService::class.java)
            bindService(intent,this, BIND_AUTO_CREATE)
            startService(intent)
            musicListPA= ArrayList()
            musicListPA.addAll(MainActivity.musicListSearch)
            setLayout()
        }
        "MusicAdapter" ->{
            //For starting Service
            val intent=Intent(this,MusicService::class.java)
            bindService(intent,this, BIND_AUTO_CREATE)
            startService(intent)
            musicListPA= ArrayList()
            musicListPA.addAll(MainActivity.MusicListMA)
            setLayout()

        }
        "MainActivity" ->{
            //For starting Service
            val intent=Intent(this,MusicService::class.java)
            bindService(intent,this, BIND_AUTO_CREATE)
            startService(intent)
            musicListPA= ArrayList()
            musicListPA.addAll(MainActivity.MusicListMA)
            musicListPA.shuffle()
            setLayout()

        }
        "FavouriteShuffle" ->{
            val intent=Intent(this,MusicService::class.java)
            bindService(intent,this, BIND_AUTO_CREATE)
            startService(intent)
            musicListPA= ArrayList()
            musicListPA.addAll(FavoriteActivity.favouriteSongs)
            musicListPA.shuffle()
            setLayout()
        }
       "PlaylistDetailsAdapter" ->{
           val intent=Intent(this,MusicService::class.java)
           bindService(intent,this, BIND_AUTO_CREATE)
           startService(intent)
           musicListPA= ArrayList()
           musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
           setLayout()
       }
        "PlaylistDetailsShuffle" ->{
            val intent=Intent(this,MusicService::class.java)
            bindService(intent,this, BIND_AUTO_CREATE)
            startService(intent)
            musicListPA= ArrayList()
            musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
            musicListPA.shuffle()
            setLayout()
        }
    }
}

   private fun playMusic(){
       binding.playPauseButtonPA.setIconResource(R.drawable.pause_icon)
       musicService!!.showNotification(R.drawable.pause_icon)
       isPlaying=true
       musicService!!.mediaPlayer!!.start()
   }

    private fun pauseMusic(){
        binding.playPauseButtonPA.setIconResource(R.drawable.play_icon)
        musicService!!.showNotification(R.drawable.play_icon)
        isPlaying=false
        musicService!!.mediaPlayer!!.pause()

    }

private fun prevNextSong(increment:Boolean){
    if(increment){
       setSongPosition(increment = true)
        setLayout()
        createMediaPlayer()
    }
    else{
        setSongPosition(increment=false)
        setLayout()
        createMediaPlayer()
    }
}



    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
   val binder=service as MusicService.MyBinder
        musicService=binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()
        musicService!!.audioManager=getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(musicService,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN)

    }

    override fun onServiceDisconnected(p0: ComponentName?) {
     musicService=null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(true)
        createMediaPlayer()
        try { setLayout()
        }catch (e:java.lang.Exception){
            return
        }

        //for refreshing now playing image & text on song completion
        NowPlaying.binding.songnameNP.isSelected = true
        Glide.with(applicationContext)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(NowPlaying.binding.songImgNP)
        NowPlaying.binding.songnameNP.text = musicListPA[songPosition].title
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 7 || resultCode == RESULT_OK)
            return
    }

private fun showBottomSheetDialog(){
    val dialog=BottomSheetDialog(this@PlayerActivity)
    dialog.setContentView(R.layout.bottom_sheet_dialog)
    dialog.show()
    dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener{
        Toast.makeText(baseContext,"Music Player will stop after 15 minutes !",Toast.LENGTH_SHORT).show()
        binding.timerbtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
        min15=true
        Thread{Thread.sleep((15*60000).toLong())
        if (min15) exitApplication()}.start()
        dialog.dismiss()
    }

    dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener{
        Toast.makeText(baseContext,"Music Player will stop after 30 minutes !",Toast.LENGTH_SHORT).show()
        binding.timerbtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
        min30=true
        Thread{Thread.sleep((30*60000).toLong())
            if (min30) exitApplication()}.start()
        dialog.dismiss()
    }

    dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener{
        Toast.makeText(baseContext,"Music Player will stop after 60 minutes !",Toast.LENGTH_SHORT).show()
        binding.timerbtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
        min60=true
        Thread{Thread.sleep((60*60000).toLong())
            if (min60) exitApplication()}.start()
        dialog.dismiss()
    }
}

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getMusicDetails(contentUri:Uri):Music{
        var cursor:Cursor?=null
        try{
            val projection=arrayOf(MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DURATION)
            cursor=this.contentResolver.query(contentUri,projection,null,null,null)
            val dataColumn=cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn=cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path= dataColumn?.let { cursor.getString(it) }
            val duration= durationColumn?.let { cursor.getLong(it) }!!
            return Music(id="Unknown",title=path.toString(),album="Unknown",artist = "Unknown",duration=duration,
            artUri = "Unknown",path = path.toString())
        }finally {
            cursor?.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    if(musicListPA[songPosition].id == "Unknown" && !isPlaying)
        exitApplication()
    }
}