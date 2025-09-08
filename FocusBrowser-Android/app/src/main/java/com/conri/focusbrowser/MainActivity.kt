package com.conri.focusbrowser

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.conri.focusbrowser.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var allowHosts: Set<String>
    private var startUrl: String = "https://office.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadPolicy()
        setupWebView()
        binding.web.loadUrl(startUrl)
    }

    private fun loadPolicy() {
        // Read assets/policy.json
        val input = assets.open("policy.json")
        val text = BufferedReader(InputStreamReader(input)).use { it.readText() }
        val json = JSONObject(text)

        // start_url is optional
        startUrl = json.optString("start_url", startUrl)

        // Convert allowlist array to set of host suffixes
        val arr = json.optJSONArray("allowlist")
        val hosts = mutableSetOf<String>()
        if (arr != null) {
            for (i in 0 until arr.length()) {
                hosts.add(arr.getString(i).lowercase())
            }
        }
        allowHosts = hosts
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val web = binding.web
        val s: WebSettings = web.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.allowFileAccess = false
        s.userAgentString = s.userAgentString + " FocusBrowser/1.0"

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val host = request.url.host?.lowercase() ?: return true
                val allowed = allowHosts.any { host == it || host.endsWith(".$it") }
                return if (allowed) {
                    false // let WebView load it
                } else {
                    // show local block page with the URL
                    val html = assets.open("blocked.html").bufferedReader().readText()
                        .replace("{{URL}}", request.url.toString())
                    view.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                    true
                }
            }
        }
    }

    override fun onBackPressed() {
        val web = binding.web
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
