package com.conri.focusbrowser

import android.content.Context
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.view.isVisible
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class Tab(val id: Long, var title: String = "New Tab", var url: String = "about:blank", var webView: WebView? = null)

object TabManager {
    private val tabs = mutableListOf<Tab>()
    var currentIndex: Int = -1
        private set

    fun list(): List<Tab> = tabs

    fun create(context: Context, container: FrameLayout, url: String): Int {
        val tab = Tab(System.currentTimeMillis(), "New Tab", url)
        tabs.add(tab)
        attachWebView(context, container, tabs.lastIndex)
        switchTo(container, tabs.lastIndex)
        return tabs.lastIndex
    }

    fun close(container: FrameLayout, index: Int) {
        if (index < 0 || index >= tabs.size) return
        val vw = tabs[index].webView
        container.removeView(vw)
        vw?.destroy()
        tabs.removeAt(index)
        if (tabs.isEmpty()) {
            currentIndex = -1
        } else {
            currentIndex = (index - 1).coerceAtLeast(0)
            showOnly(container, currentIndex)
        }
    }

    fun move(from: Int, to: Int) {
        if (from == to) return
        val tab = tabs.removeAt(from)
        tabs.add(to, tab)
        if (currentIndex == from) currentIndex = to
    }

    fun attachWebView(context: Context, container: FrameLayout, index: Int) {
        val tab = tabs[index]
        val web = WebView(context)
        val s: WebSettings = web.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.userAgentString = s.userAgentString + " FocusBrowser/1.1"
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                tab.title = view?.title ?: tab.title
                tab.url = url ?: tab.url
            }
        }
        container.addView(web, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ))
        tab.webView = web
        web.loadUrl(tab.url)
    }

    fun showOnly(container: FrameLayout, index: Int) {
        currentIndex = index
        for (i in 0 until container.childCount) {
            container.getChildAt(i).isVisible = (i == index)
        }
    }

    fun switchTo(container: FrameLayout, index: Int) {
        if (tabs.getOrNull(index)?.webView == null) attachWebView(container.context, container, index)
        showOnly(container, index)
    }
}
