package com.obodroid.kaitomm.gifplayer.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.JsonParser
import com.obodroid.kaitomm.gifplayer.R
import com.obodroid.kaitomm.gifplayer.VideoActivity

private const val ARG_URLS = "urls"
private const val ARG_OPTION = "option"

class VideoFbFragment : VideoFragmentInterface() {

    private lateinit var webView: WebView

    private var urls: String? = null
    private var option: VideoActivity.Option? = null
    private var listener: VideoFragmentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            urls = it.getString(ARG_URLS)
            option = it.getSerializable(ARG_OPTION) as VideoActivity.Option
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is VideoFragmentListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_fb, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webView)

        val urlList = JsonParser().parse(urls).asJsonArray
        val url = urlList[0]

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false

        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36 Edge/12.0"

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        webView.reload()

        webView.loadDataWithBaseURL(
            "https://www.facebook.com",
            """
                <html>
                    <head>
                      <title>Your Website Title</title>
                    </head>
                    <body style="margin:0px">
                    
                      <script>
                        window.fbAsyncInit = function() {
                          FB.init({
                            xfbml      : true,
                            version    : 'v3.2'
                          });
                        
                          var my_video_player;
                          FB.Event.subscribe('xfbml.ready', function(msg) {
                            if (msg.type === 'video') {
                              my_video_player = msg.instance;
                              my_video_player.play();
                              my_video_player.subscribe('finishedPlaying', function() {
                                Android.close();
                              })
                            }
                          });
                        };
                      </script>
                      <div id="fb-root"></div>
                      <script async defer src="https://connect.facebook.net/en_US/sdk.js"></script>
                    
                      <div  
                        class="fb-video" 
                        data-href="${url.asString}" 
                        data-width="1080" 
                        data-allowfullscreen="true"
                        data-autoplay="false"></div>
                    </body>
                </html>
            """, "text/html", "utf-8", null
        )
    }

    // VideoFragmentInterface
    override fun pauseVideo() {

    }

    override fun resumeVideo() {

    }

    companion object {
        @JvmStatic
        fun newInstance(urls: String, option: VideoActivity.Option) =
            VideoFbFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URLS, urls)
                    putSerializable(ARG_OPTION, option)
                }
            }
    }

    class WebAppInterface internal constructor(private var fragment: VideoFbFragment) {

        @JavascriptInterface
        fun close() {
            fragment.listener?.videoEnded()
        }
    }
}