package com.example.musicapp

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat

class MusicService : Service(),AudioManager.OnAudioFocusChangeListener{
    private var myBinder= MyBinder()
    var mediaPlayer:MediaPlayer?=null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(p0: Intent?): IBinder? {
        mediaSession= MediaSessionCompat(baseContext,"My music")
        return myBinder
    }
    inner class MyBinder:Binder(){
        fun currentService() : MusicService{
            return this@MusicService
        }
    }
    fun showNotification(playPauseBtn:Int){

        val intent = Intent(baseContext,MainActivity::class.java)
        val contentIntent=PendingIntent.getActivity(this,0,intent,0)


        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if(imgArt != null){
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        }else{
            BitmapFactory.decodeResource(resources, R.drawable.music)
        }


        val notification=NotificationCompat.Builder(baseContext,ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
        .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
        .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
        .setSmallIcon(R.drawable.music_icon)
        .setLargeIcon(image)
        .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setOnlyAlertOnce(true)
        .addAction(R.drawable.previous_icon,"Previous",prevPendingIntent)
        .addAction(playPauseBtn,"Play",playPendingIntent)
        .addAction(R.drawable.next_icon,"Next",nextPendingIntent)
        .addAction(R.drawable.exit_icon,"Exit",exitPendingIntent)
        .build()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val playbackSpeed = if(PlayerActivity.isPlaying) 1F else 0F
            mediaSession.setMetadata(MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                .build())
            val playBackState = PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
            mediaSession.setPlaybackState(playBackState)
            mediaSession.setCallback(object: MediaSessionCompat.Callback(){
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer!!.seekTo(pos.toInt())
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                    mediaSession.setPlaybackState(playBackStateNew)
                }
            })
        }

        startForeground(7,notification)

    }
    fun createMediaPlayer(){
        try {
            if(PlayerActivity.musicService!!.mediaPlayer == null) PlayerActivity.musicService!!.mediaPlayer= MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.playPauseButtonPA.setIconResource(R.drawable.pause_icon)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
            PlayerActivity.binding.tvSeekbarstart.text= formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekbarend.text= formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekbarPA.progress=0
            PlayerActivity.binding.seekbarPA.max= mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
        }catch (e:Exception){
            return
        }
    }

fun seekBarSetup(){
    runnable= Runnable {
        PlayerActivity.binding.tvSeekbarstart.text= formatDuration(mediaPlayer!!.currentPosition.toLong())
        PlayerActivity.binding.seekbarPA.progress=mediaPlayer!!.currentPosition
Handler(Looper.getMainLooper()).postDelayed(runnable,200)
    }
    Handler(Looper.getMainLooper()).postDelayed(runnable,0)
}

    override fun onAudioFocusChange(focusChange: Int) {
        if(focusChange <= 0){
            //pause music
            PlayerActivity.binding.playPauseButtonPA.setIconResource(R.drawable.play_icon)
            NowPlaying.binding.playPauseButtonNP.setIconResource(R.drawable.play_icon)
            showNotification(R.drawable.play_icon)
            PlayerActivity.isPlaying =false
            PlayerActivity.musicService!!.mediaPlayer!!.pause()

        }
            //play music
        PlayerActivity.binding.playPauseButtonPA.setIconResource(R.drawable.pause_icon)
        NowPlaying.binding.playPauseButtonNP.setIconResource(R.drawable.pause_icon)
       showNotification(R.drawable.pause_icon)
        PlayerActivity.isPlaying =true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
    }
}