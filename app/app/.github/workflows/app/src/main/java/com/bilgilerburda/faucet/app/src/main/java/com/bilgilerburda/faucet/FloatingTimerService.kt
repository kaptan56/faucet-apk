package com.bilgilerburda.faucet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat

class FloatingTimerService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: TextView
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val seconds = intent?.getIntExtra("SECONDS", 30) ?: 30
        val youtubeUrl = intent?.getStringExtra("YOUTUBE_URL") ?: ""

        // 1. Bildirimi başlat (Foreground service zorunluluğu)
        val notification = createNotification()
        startForeground(1, notification)

        // 2. Yüzen sayacı ekrana çiz
        showFloatingTimer()

        // 3. YouTube'u aç
        if (youtubeUrl.isNotEmpty()) {
            openYouTube(youtubeUrl)
        }

        // 4. Geri sayımı başlat
        startCountdown(seconds)

        return START_NOT_STICKY
    }

    private fun showFloatingTimer() {
        floatingView = TextView(this).apply {
            text = "Kalan: --"
            setTextColor(Color.parseColor("#4ADE80")) // Canlı yeşil
            textSize = 20f
            setPadding(30, 20, 30, 20)
            setBackgroundColor(Color.parseColor("#CC18181B")) // Yarı saydam koyu arka plan
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 150 // Ekranın en altından biraz yukarıda
        }

        windowManager.addView(floatingView, params)
    }

    private fun startCountdown(seconds: Int) {
        countDownTimer = object : CountDownTimer(seconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                floatingView.text = "Kalan: $sec sn"
            }

            override fun onFinish() {
                floatingView.text = "Süre Doldu!"
                // Sayaç bitince yüzen pencereyi kaldır ve servisi durdur
                if (::floatingView.isInitialized) {
                    windowManager.removeView(floatingView)
                }
                stopSelf()
            }
        }.start()
    }

    private fun openYouTube(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            // YouTube açılamazsa tarayıcıda aç
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(browserIntent)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "FaucetChannel")
            .setContentTitle("Faucet Video İzleniyor")
            .setContentText("Sayaç arka planda çalışıyor...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "FaucetChannel",
                "Faucet Servis",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        if (::floatingView.isInitialized) {
            try {
                windowManager.removeView(floatingView)
            } catch (e: Exception) {
                // Zaten kaldırılmışsa hata verme
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
