package com.example.myapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingImageView: ImageView
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startMyForeground()
        setupFloatingWidget()
    }

    private fun startMyForeground() {
        val channelId = "floating_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Floating Widget Active",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Floating Widget Running")
            .setContentText("Displaying overlay widget on screen.")
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingWidget() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create the image element directly via code
        floatingImageView = ImageView(this).apply {
            // By default setting a built-in Android icon as placeholder. 
            // You can replace ic_menu_gallery with your image resource later.
            setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Layout parameters configurations for drawing over apps
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            200, // Width in pixels (Default size)
            200, // Height in pixels (Default size)
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        // Touch Listener logic to Drag and Move the image freely on screen
        floatingImageView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.x = Math.max(0, params.x) // prevent going outside screen left
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        params.y = Math.max(0, params.y) // prevent going outside screen top
                        windowManager.updateViewLayout(floatingImageView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingImageView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingImageView.isInitialized) {
            windowManager.removeView(floatingImageView)
        }
    }
}
