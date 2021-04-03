package uk.easys.easykeeper

import android.animation.*
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.View
import android.webkit.CookieManager
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import uk.easys.easykeeper.webview.AdvancedWebView
import java.util.*
import kotlin.system.exitProcess

class Main : AppCompatActivity(), AdvancedWebView.Listener {
    lateinit var mBody: ConstraintLayout
    lateinit var mMotor1: View
    lateinit var mWV: AdvancedWebView
    lateinit var mLoadingBG: View
    lateinit var mLoading: ConstraintLayout
    lateinit var mLoadCycle1: ConstraintLayout
    lateinit var mLoadCycle2: View
    lateinit var mCartIcon: ConstraintLayout
    lateinit var mCartIconIV: ImageView
    lateinit var mLoad: ConstraintLayout
    lateinit var mLoadTV1: ImageView
    lateinit var mLoadTV2: ImageView
    lateinit var mLoadTVCover: View
    lateinit var mLoadIV: ImageView

    lateinit var c: Context
    lateinit var cook: CookieManager
    lateinit var cookies: List<Cookie>

    var firstBackToExit = false
    var mLoadDur: Long = 2000
    val supportedRtlLangs = listOf("ar")
    var isLoading = false
    var loadingFadeDur: Long = 66
    var mLoadingAlpha = 0.95f
    var dropped = false
    var mLoaded = false

    companion object {
        const val loadDur: Long = 125
        var dm = DisplayMetrics()
        var dirLtr = true
        var kiriAlpha = 0.999f

        fun va(
            v: View,
            prop: String,
            dur: Long,
            val1: Float,
            val2: Float,
            delay: Long = 0
        ): ValueAnimator {
            val va = ObjectAnimator.ofFloat(v, prop, val1, val2).setDuration(dur)
            va.startDelay = delay
            va.start()
            return va
        }

        fun oa(v: View, prop: String, dur: Long, value: Float, delay: Long = 0): ObjectAnimator {
            val oa = ObjectAnimator.ofFloat(v, prop, value).setDuration(dur)
            oa.startDelay = delay
            oa.start()
            return oa
        }

        fun dp(px: Int): Int = (px * dm.density).toInt()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        mBody = findViewById(R.id.mBody)
        mMotor1 = findViewById(R.id.mMotor1)
        mWV = findViewById(R.id.mWV)
        mLoadingBG = findViewById(R.id.mLoadingBG)
        mLoading = findViewById(R.id.mLoading)
        mLoadCycle1 = mLoading.getChildAt(1) as ConstraintLayout
        mLoadCycle2 = mLoading.getChildAt(2) as View
        mCartIcon = findViewById(R.id.mCartIcon)
        mCartIconIV = findViewById(R.id.mCartIconIV)
        mLoad = findViewById(R.id.mLoad)
        mLoadTV1 = findViewById(R.id.mLoadTV1)
        mLoadTV2 = findViewById(R.id.mLoadTV2)
        mLoadTVCover = findViewById(R.id.mLoadTVCover)
        mLoadIV = findViewById(R.id.mLoadIV)

        c = applicationContext
        windowManager.defaultDisplay.getMetrics(dm)
        for (lang in supportedRtlLangs)
            if (Locale.getDefault().language == lang) {
                mBody.layoutDirection = View.LAYOUT_DIRECTION_RTL
                dirLtr = false
            }


        // Loading
        if (isOnline()) {
            mWV.setListener(this, this)
            mWV.loadUrl(resources.getString(R.string.site))
            mWV.setCookiesEnabled(true)
            mWV.setThirdPartyCookiesEnabled(true)
            cook = mWV.cookieManager//ALWAYS AFTER "setCookiesEnabled"
        } else mLoad(false)
        mLoad.setOnClickListener {}
        val anLoad1 = va(mMotor1, "translationX", loadDur, 10f, 0f, 1970)
        anLoad1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                var dest = mLoadIV.width * 0.65f
                var tvDest = (mLoadTV2.width - dest) + (dm.density * 13)
                val drop = AnimatorSet().setDuration(209)
                drop.playTogether(
                    ObjectAnimator.ofFloat(mLoadIV, "translationX", 0f - dest),
                    ObjectAnimator.ofFloat(mLoadTVCover, "translationX", 0f - dest),
                    ObjectAnimator.ofFloat(mLoadTV1, "translationX", tvDest),
                    ObjectAnimator.ofFloat(mLoadTV2, "translationX", tvDest)
                )
                drop.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        dropped = true
                    }
                })
                drop.start()

                val timer = object : CountDownTimer(10000, 10000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        mLoad(false)
                    }
                }
                timer.start()

                // SEPARABLE
                mLoadCycle1.alpha = kiriAlpha// NECESSARY
                mLoadCycle2.alpha = kiriAlpha
            }
        })

        // Button
        mCartIconIV.setColorFilter(ContextCompat.getColor(c, R.color.mCartIconIV))
        mCartIcon.setOnClickListener { mWV.loadUrl(resources.getString(R.string.button)) }
    }

    override fun onBackPressed() {
        //if (mWV.canGoBack()) mWV.goBack()
        //else {
        if (!firstBackToExit) {
            firstBackToExit = true
            Toast.makeText(c, R.string.toExit, Toast.LENGTH_SHORT).show()
            val exit = va(mMotor1, "translationX", 4000, 4f, 0f)
            exit.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    firstBackToExit = false
                }
            })
        } else {
            moveTaskToBack(true)
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)//System.exit(1)
        }
        //}
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val anCon = va(mMotor1, "translationX", loadDur, 10f, 0f)
        anCon.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                windowManager.defaultDisplay.getMetrics(dm)
            }
        })
    }

    override fun onPageFinished(url: String?) {
        loading(false)
        /*if (!mLoaded) {
            pageFinished = true
            checkMLoad()
        }
        checkLogin()*/
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
    }

    override fun onDownloadRequested(
        url: String?, suggestedFilename: String?, mimeType: String?, contentLength: Long,
        contentDisposition: String?, userAgent: String?
    ) {
    }

    override fun onExternalPageRequest(url: String?) {
    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {
        loading(true)
    }


    fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        var nwi: NetworkInfo? = null
        if (cm != null) nwi = cm.activeNetworkInfo
        return (nwi != null && nwi.isConnected)
    }

    fun mLoad(b: Boolean) {
        when (b) {
            false -> {
                if (mLoaded) return
                var dest = dm.widthPixels * 4f
                if (dirLtr) dest = 0 - dest
                oa(mLoad, "translationX", mLoadDur, dest)
                var ml2 = oa(mLoad, "alpha", (mLoadDur / 5) * 4, 0f, mLoadDur / 5)
                ml2.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        mLoad.visibility = View.INVISIBLE
                        mLoad.translationX = 0f
                        mLoad.alpha = 1f
                    }
                })
            }
            true -> if (mLoaded) mLoad.visibility = View.VISIBLE
        }
        mLoaded = !b
    }

    fun loading(b: Boolean) {
        if (b) {
            if (isLoading) return
            mLoading.visibility = View.VISIBLE
            mLoadingBG.visibility = View.VISIBLE
            oa(mLoading, "alpha", loadingFadeDur, mLoadingAlpha)
            oa(mLoadingBG, "alpha", loadingFadeDur, 1f)
            anLoading()
        } else {
            if (!isLoading) return
            oa(mLoading, "alpha", loadingFadeDur, 0f)
            oa(mLoadingBG, "alpha", loadingFadeDur, 0f).addListener(object :
                AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    mLoading.visibility = View.GONE
                    mLoadingBG.visibility = View.GONE
                }
            })
        }
        isLoading = b
    }

    fun anLoading() {
        var an = AnimatorSet().setDuration(684)
        an.playTogether(
            ObjectAnimator.ofFloat(mLoadCycle1, "rotation", 0f, 360f),
            ObjectAnimator.ofFloat(mLoadCycle2, "rotation", 0f, -360f)
        )
        an.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (isLoading) anLoading()
            }
        })
        an.start()
    }
}
