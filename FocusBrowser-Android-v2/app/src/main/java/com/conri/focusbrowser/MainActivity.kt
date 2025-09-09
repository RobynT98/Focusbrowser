package com.conri.focusbrowser

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.conri.focusbrowser.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var policy: JSONObject
    private lateinit var prefs: androidx.preference.PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        loadPolicy()

        // Theme toggle state
        val mode = getSharedPreferences("fb", MODE_PRIVATE).getString("theme", "system")!!
        applyTheme(mode)

        // New tab with start url
        val start = policy.optString("start_url", "https://office.com")
        TabManager.create(this, binding.tabContainer, start)

        // Address/search bar logic (supports shortcuts)
        binding.address.setOnEditorActionListener { v, _, _ ->
            val text = binding.address.text.toString().trim()
            handleAddress(text)
            true
        }

        // Bottom actions
        binding.btnTabs.setOnClickListener {
            startActivity(Intent(this, TabsActivity::class.java))
        }
        binding.btnBack.setOnClickListener {
            val w = TabManager.list().getOrNull(TabManager.currentIndex)?.webView
            if (w?.canGoBack() == true) w.goBack()
        }
        binding.btnForward.setOnClickListener {
            val w = TabManager.list().getOrNull(TabManager.currentIndex)?.webView
            if (w?.canGoForward() == true) w.goForward()
        }
        binding.btnNew.setOnClickListener {
            TabManager.create(this, binding.tabContainer, start)
        }

        // AI bottom sheet
        val sheetBehavior = BottomSheetBehavior.from(binding.aiSheet.root)
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.btnAi.setOnClickListener {
            val url = policy.optString("ai_url", "https://chat.openai.com")
            binding.aiSheet.aiWeb.loadUrl(url)
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        binding.aiSheet.btnClose.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun handleAddress(text: String) {
        val w = TabManager.list().getOrNull(TabManager.currentIndex)?.webView ?: return
        // Command palette style prefixes
        when {
            text.startsWith("g ") -> w.loadUrl("https://www.google.com/search?q=" + text.substring(2))
            text.startsWith("gh ") -> w.loadUrl("https://github.com/search?q=" + text.substring(3))
            text.startsWith("yt ") -> w.loadUrl("https://www.youtube.com/results?search_query=" + text.substring(3))
            text.startsWith("m ") -> {
                val which = text.substring(2)
                val url = when (which) {
                    "outlook" -> "https://outlook.office.com/mail/"
                    "gmail" -> "https://mail.google.com/"
                    else -> "https://outlook.office.com/mail/"
                }
                w.loadUrl(url)
            }
            text.contains("://") || text.contains(".") -> {
                val target = if (text.contains("://")) text else "https://$text"
                w.loadUrl(target)
            }
            else -> {
                w.loadUrl("https://www.google.com/search?q=$text")
            }
        }
    }

    private fun loadPolicy() {
        val text = assets.open("policy.json").bufferedReader().use { it.readText() }
        policy = JSONObject(text)
    }

    private fun applyTheme(mode: String) {
        val m = when(mode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(m)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_theme -> {
                val current = getSharedPreferences("fb", MODE_PRIVATE).getString("theme", "system")!!
                val next = when(current) {
                    "system" -> "light"
                    "light" -> "dark"
                    else -> "system"
                }
                getSharedPreferences("fb", MODE_PRIVATE).edit().putString("theme", next).apply()
                applyTheme(next)
                val msg = "Theme: $next"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
