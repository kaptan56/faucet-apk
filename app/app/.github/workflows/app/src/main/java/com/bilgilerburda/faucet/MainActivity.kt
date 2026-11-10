package com.bilgilerburda.faucet

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.setSupportMultipleWindows(true)
                                settings.javaScriptCanOpenWindowsAutomatically = true
                                
                                // Sitemizin JavaScript'i için köprü
                                addJavascriptInterface(WebAppInterface(), "Android")
                                
                                loadUrl("https://bilgilerburda.com/faucet/")
                                webView = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun startTimer(seconds: Int, youtubeUrl: String) {
            if (!Settings.canDrawOverlays(this@MainActivity)) {
                // İzin yoksa ayarlara yönlendir
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                // İzin varsa servisi başlat (YouTube açılacak ve sayaç çıkacak)
                val serviceIntent = Intent(this@MainActivity, FloatingTimerService::class.java).apply {
                    putExtra("SECONDS", seconds)
                    putExtra("YOUTUBE_URL", youtubeUrl)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            }
        }

        @JavascriptInterface
        fun hasOverlayPermission(): Boolean {
            return Settings.canDrawOverlays(this@MainActivity)
        }
    }
}
