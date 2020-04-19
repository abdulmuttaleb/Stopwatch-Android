package com.ahmad.stopwatch.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Message
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ahmad.stopwatch.R
import com.ahmad.stopwatch.view.MainActivity

class NotificationService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent?.getStringExtra("time")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0 , notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Stopwatch")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_play_arrow)
            .setContentIntent(null)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    companion object{
        const val CHANNEL_ID = "NotificationService"

        fun startService(context: Context, message: String){
            val startIntent = Intent(context, NotificationService::class.java)
            startIntent.putExtra("time", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context){
            val stopIntent = Intent(context, NotificationService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteNotificationIfVisible()
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Notification Service Channel", NotificationManager.IMPORTANCE_DEFAULT)

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    fun deleteNotificationIfVisible(){
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager!!.cancel(1)
    }
}