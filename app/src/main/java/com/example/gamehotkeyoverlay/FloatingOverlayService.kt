package com.example.gamehotkeyoverlay

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var params: WindowManager.LayoutParams

    private lateinit var layoutBubble: LinearLayout
    private lateinit var layoutPanel: LinearLayout
    private lateinit var editCommand: EditText

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.layout_floating_overlay, null)

        layoutBubble = overlayView.findViewById(R.id.layoutBubble)
        layoutPanel = overlayView.findViewById(R.id.layoutPanel)
        editCommand = overlayView.findViewById(R.id.editCommand)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        windowManager.addView(overlayView, params)

        setupBubbleInteractions()
        setupPanelInteractions()
    }

    private fun setupBubbleInteractions() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = true

        layoutBubble.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        isClick = false
                    }
                    params.x = initialX + deltaX
                    params.y = initialY + deltaY
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        expandPanel()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun expandPanel() {
        layoutBubble.visibility = View.GONE
        layoutPanel.visibility = View.VISIBLE
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        windowManager.updateViewLayout(overlayView, params)
    }

    private fun minimizePanel() {
        layoutPanel.visibility = View.GONE
        layoutBubble.visibility = View.VISIBLE
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        windowManager.updateViewLayout(overlayView, params)
    }

    private fun setupPanelInteractions() {
        // Drag bar
        val headerDrag = overlayView.findViewById<LinearLayout>(R.id.headerDrag)
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        headerDrag.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                else -> false
            }
        }

        // Minimize & Close
        overlayView.findViewById<Button>(R.id.btnMinimize).setOnClickListener {
            minimizePanel()
        }
        overlayView.findViewById<Button>(R.id.btnCloseApp).setOnClickListener {
            stopSelf()
        }

        // Hotkeys in Browser: Copies the JavaScript execution snippet for Brave Console / Address Bar
        overlayView.findViewById<Button>(R.id.btnShiftO).setOnClickListener {
            val js = "javascript:(function(){window.dispatchEvent(new KeyboardEvent('keydown',{key:'O',code:'KeyO',keyCode:79,which:79,shiftKey:true,bubbles:true}));setTimeout(()=>{window.dispatchEvent(new KeyboardEvent('keyup',{key:'O',code:'KeyO',keyCode:79,which:79,shiftKey:true,bubbles:true}))},50);})();"
            copyToClipboard("Shift+O Trigger", js)
            Toast.makeText(this, "Copied Shift+O JS! Paste in Brave URL bar or DevTools", Toast.LENGTH_SHORT).show()
        }

        overlayView.findViewById<Button>(R.id.btnShiftD).setOnClickListener {
            val js = "javascript:(function(){window.dispatchEvent(new KeyboardEvent('keydown',{key:'D',code:'KeyD',keyCode:68,which:68,shiftKey:true,bubbles:true}));setTimeout(()=>{window.dispatchEvent(new KeyboardEvent('keyup',{key:'D',code:'KeyD',keyCode:68,which:68,shiftKey:true,bubbles:true}))},50);})();"
            copyToClipboard("Shift+D Trigger", js)
            Toast.makeText(this, "Copied Shift+D JS! Paste in Brave", Toast.LENGTH_SHORT).show()
        }

        overlayView.findViewById<Button>(R.id.btnShiftR).setOnClickListener {
            val js = "javascript:(function(){location.reload();})();"
            copyToClipboard("Reload Trigger", js)
            Toast.makeText(this, "Copied Reload Trigger", Toast.LENGTH_SHORT).show()
        }

        overlayView.findViewById<Button>(R.id.btnEsc).setOnClickListener {
            val js = "javascript:(function(){window.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape',code:'Escape',keyCode:27,which:27,bubbles:true}));})();"
            copyToClipboard("ESC Trigger", js)
            Toast.makeText(this, "Copied ESC Trigger", Toast.LENGTH_SHORT).show()
        }

        // Copy Helper
        overlayView.findViewById<Button>(R.id.btnCopyCheatHelper).setOnClickListener {
            val helper = "javascript:(function(){let cmd=prompt('Enter Console Command/Cheat:');if(cmd){try{eval(cmd)}catch(e){alert(e)}}})();"
            copyToClipboard("Console Prompt", helper)
            Toast.makeText(this, "Copied In-Game Prompt Helper! Paste in Brave URL bar", Toast.LENGTH_LONG).show()
        }

        // Copy custom typed command
        overlayView.findViewById<Button>(R.id.btnCopyCommand).setOnClickListener {
            val text = editCommand.text.toString().trim()
            if (text.isNotEmpty()) {
                copyToClipboard("Command", text)
                Toast.makeText(this, "Copied command: $text", Toast.LENGTH_SHORT).show()
            }
        }

        // Presets
        overlayView.findViewById<Button>(R.id.btnPresetMoney).setOnClickListener {
            editCommand.setText("gold=999999")
            copyToClipboard("Cheat", "gold=999999")
            Toast.makeText(this, "Copied: gold=999999", Toast.LENGTH_SHORT).show()
        }
        overlayView.findViewById<Button>(R.id.btnPresetUnlock).setOnClickListener {
            editCommand.setText("unlocked=true")
            copyToClipboard("Cheat", "unlocked=true")
            Toast.makeText(this, "Copied: unlocked=true", Toast.LENGTH_SHORT).show()
        }
        overlayView.findViewById<Button>(R.id.btnPresetGallery).setOnClickListener {
            editCommand.setText("gallery_unlocked=true")
            copyToClipboard("Cheat", "gallery_unlocked=true")
            Toast.makeText(this, "Copied: gallery_unlocked=true", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }
}
