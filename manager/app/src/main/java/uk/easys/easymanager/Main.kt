package uk.easys.easymanager

import android.animation.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import uk.easys.easymanager.webview.AdvancedWebView
import uk.easys.easymanager.woo.WooCommerce
import uk.easys.easymanager.woo.models.Customer
import uk.easys.easymanager.woo.models.Order
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
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
    lateinit var mCartIconBG: View
    lateinit var mCartIconBGDis: View
    lateinit var mCartIconIV: ConstraintLayout
    lateinit var mMenu: ConstraintLayout
    lateinit var mMenuClose: ImageView
    lateinit var mMenuTitle: TextView
    lateinit var mLoad: ConstraintLayout
    lateinit var mLoadTV1: ImageView
    lateinit var mLoadTV2: ImageView
    lateinit var mLoadTVCover: View
    lateinit var mLoadIV: ImageView

    lateinit var c: Context
    lateinit var wc: WooCommerce
    lateinit var cook: CookieManager
    lateinit var cookies: List<Cookie>
    lateinit var mCartDots: ArrayList<View>
    lateinit var mMenus: ArrayList<ConstraintLayout>
    lateinit var mMnuSh: ArrayList<View?>
    lateinit var mMnuTV: ArrayList<TextView>

    var firstBackToExit = false
    var mLoadDur: Long = 2000
    val supportedRtlLangs = listOf("ar")
    var bCart = false
    var dropped = false
    var pageFinished = false
    var mLoaded = false
    var isLoggedIn = false
    var isLoading = false
    var loadingFadeDur: Long = 66
    var mLoadingAlpha = 0.95f
    var cartScale = 0.84f
    val mMITextScale = 0.44f
    val mMITextAlpha = 0f

    companion object {
        const val conKey = "ck_51a676dd44e7953afa08eb9552fec58b11b8a0a7"
        const val conSec = "cs_8efa7e08c4a3c3c6366e9467117ef12ff6ecd648"
        const val loadDur: Long = 125
        var dm = DisplayMetrics()
        var dirLtr = true
        var liUser: String? = null
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

        fun isOnline(c: Context): Boolean {
            val cm = c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            var nwi: NetworkInfo? = null
            if (cm != null) nwi = cm.activeNetworkInfo
            return (nwi != null && nwi.isConnected)
        }

        fun compileTime(time: Long): String {
            val lm = Calendar.getInstance()
            lm.timeInMillis = time
            var y = lm.get(Calendar.YEAR)
            var m = lm.get(Calendar.MONTH) + 1
            var d = lm.get(Calendar.DAY_OF_MONTH)
            var h = lm.get(Calendar.HOUR_OF_DAY)
            var n = lm.get(Calendar.MINUTE)
            return "$y." + (if (m < 10) "0" else "") + "$m." + (if (d < 10) "0" else "") + "$d" // - " +
            //(if (h < 10) "0" else "") + "$h:" + (if (n < 10) "0" else "") + "$n"
        }

        fun fixPrice(price: String): String {
            var p = StringBuilder()
            var dropZero = price.contains(".")
            var passedDot = false
            var three = 0
            for (i in (price.length - 1) downTo 0) {
                if (dropZero) {// DON'T MERGE THESE!
                    if (price[i].toString() != "0") {
                        dropZero = false
                        if (price[i].toString() != "." || p.isNotEmpty()) p.insert(0, price[i])
                    }
                } else {
                    p.insert(0, price[i])
                    if (passedDot) {
                        if (three == 2) {
                            if (i != 0) p.insert(0, ",")
                            three = 0
                        } else three += 1
                    }
                }
                if (price[i].toString() == ".") passedDot = true
            }
            return p.toString()
        }

        fun currency(c: Context, cur: String): String = when (cur) {
            "IQD" -> c.resources.getString(R.string.IQD)
            else -> cur
        }

        fun man(
            c: Context,
            get: String,
            lis: com.android.volley.Response.Listener<String>,
            err: com.android.volley.Response.ErrorListener?,
            hashMap: HashMap<String, String>,
            tag: String
        ) {
            var req = Volley.newRequestQueue(c)
            var srt = object :
                StringRequest(
                    Method.POST,
                    c.resources.getString(R.string.manManager) + get,
                    lis,
                    err
                ) {
                @Throws(AuthFailureError::class)
                override fun getParams() = hashMap
            }
            srt.tag = tag
            req.add(srt)
        }

        fun findOrdById(id: Int, orders: List<Order>?): Order? {
            if (orders == null) return null
            var ord: Order? = null
            for (o in orders) if (o.id == id) ord = o
            return ord
        }

        fun findCusById(id: Int, customers: List<Customer>?): Customer? {
            if (customers == null) return null
            var cus: Customer? = null
            for (c in customers) if (c.id == id) cus = c
            return cus
        }

        fun findDrvByOrd(ord: Order, drivers: List<Driver>?): Driver? {
            if (drivers == null) return null
            var drv: Driver? = null
            for (d in drivers) for (o in d.ords) if (o == ord.id) drv = d
            return drv
        }

        fun scale(v: View, x: Float, y: Float) {
            v.scaleX = x
            v.scaleY = y
        }

        fun trans(v: View, x: Float, y: Float) {
            v.translationX = x
            v.translationY = y
        }

        fun shadow(c: Context, v: View, src: Int = R.drawable.circle_shadow_1): ImageView? {
            var parent: ConstraintLayout? = null
            try {
                parent = v.parent as ConstraintLayout
            } catch (ignored: java.lang.Exception) {
                return null
            }
            if (parent == null) return null

            var sh = ImageView(c)
            var shLP = ConstraintLayout.LayoutParams(0, 0)
            shLP.topToTop = v.id
            shLP.leftToLeft = v.id
            shLP.rightToRight = v.id
            shLP.bottomToBottom = v.id
            var scale = 1.1515f
            sh.scaleX = scale
            sh.scaleY = scale
            sh.setImageResource(src)
            parent.addView(sh, parent.indexOfChild(v), shLP)
            return sh
        }

        fun explode(
            c: Context,
            v: View,
            dur: Long = 522,
            src: Int = R.drawable.circle_cp,
            alpha: Float = 1f,
            max: Float = 4f
        ) {
            var parent: ConstraintLayout? = null
            try {
                parent = v.parent as ConstraintLayout
            } catch (ignored: java.lang.Exception) {
                return
            }
            if (parent == null) return

            var ex = View(c)
            var exLP = ConstraintLayout.LayoutParams(0, 0)
            exLP.topToTop = v.id
            exLP.leftToLeft = v.id
            exLP.rightToRight = v.id
            exLP.bottomToBottom = v.id
            ex.background = ContextCompat.getDrawable(c, src)
            ex.translationX = v.translationX
            ex.translationY = v.translationY
            ex.scaleX = v.scaleX
            ex.scaleY = v.scaleY
            ex.alpha = alpha
            parent.addView(ex, parent.indexOfChild(v), exLP)

            var explode = AnimatorSet().setDuration(dur)
            var hide = ObjectAnimator.ofFloat(ex, "alpha", 0f)
            hide.startDelay = explode.duration / 4
            explode.playTogether(
                ObjectAnimator.ofFloat(ex, "scaleX", ex.scaleX * max),
                ObjectAnimator.ofFloat(ex, "scaleY", ex.scaleY * max),
                hide
            )
            explode.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    parent.removeView(ex)
                }
            })
            explode.start()
        }
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
        mCartIconBG = mCartIcon.getChildAt(0) as View
        mCartIconBGDis = mCartIcon.getChildAt(1) as View
        mCartIconIV = mCartIcon.getChildAt(2) as ConstraintLayout
        mMenu = findViewById(R.id.mMenu)
        mMenuClose = findViewById(R.id.mMenuClose)
        mMenuTitle = findViewById(R.id.mMenuTitle)
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
        mCartDots = ArrayList()
        for (d in 0 until mCartIconIV.childCount) mCartDots.add(mCartIconIV.getChildAt(d) as View)
        mMenus = ArrayList()
        for (m in 0 until mMenu.childCount - 3) mMenus.add(mMenu.getChildAt(m) as ConstraintLayout)


        // Loading
        if (isOnline(c)) {
            mWV.setListener(this, this)
            mWV.loadUrl(resources.getString(R.string.siteLogin))
            mWV.setCookiesEnabled(true)
            mWV.setThirdPartyCookiesEnabled(true)
            cook = mWV.cookieManager//ALWAYS AFTER "setCookiesEnabled"
        } else {
            Toast.makeText(c, R.string.noInternet, Toast.LENGTH_LONG).show()
            ////////////////////////
            mLoad(false)
        }
        mLoad.setOnClickListener {}
        val anLoad1 = va(mMotor1, "translationX", loadDur, 10f, 0f, 1970)
        anLoad1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                var dest = mLoadIV.width * 0.71f
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

                        // Menu Items
                        var max =
                            if (dm.widthPixels < dm.heightPixels) dm.widthPixels else dm.heightPixels
                        var cirHeight = (max / 4.6f).toInt()
                        for (m in mMenus) {
                            var mLP = m.layoutParams as ConstraintLayout.LayoutParams
                            mLP.width = cirHeight
                            mLP.height = cirHeight
                            m.layoutParams = mLP
                        }

                        // Menu Items' Texts
                        mMnuTV = ArrayList()
                        var mParent = mMenus[0].parent as ConstraintLayout
                        var mTexts = resources.getStringArray(R.array.mMenuTexts)
                        for (m in 0 until mMenus.size) {
                            var text = TextView(c)
                            var textLP =
                                ConstraintLayout.LayoutParams(0, (cirHeight * 1.6f).toInt())
                            textLP.topToTop = mMenus[m].id
                            textLP.bottomToBottom = mMenus[m].id
                            textLP.leftToLeft = mMenus[m].id
                            textLP.rightToRight = mMenus[m].id
                            text.text = mTexts[m]
                            text.setTextColor(ContextCompat.getColor(c, R.color.mMenuIconTV))
                            text.textSize =
                                (((10 * dm.density) + (cirHeight / 42f)).toInt()).toFloat()
                            text.typeface = Typeface.DEFAULT_BOLD
                            text.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                            text.gravity = Gravity.BOTTOM
                            text.alpha = mMITextAlpha
                            scale(text, mMITextScale, mMITextScale)
                            mParent.addView(text, mParent.indexOfChild(mMenus[0]), textLP)
                            mMnuTV.add(text)
                        }
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

        // Menu
        loggedIn(false)
        mCartIcon.setOnClickListener { if (isLoggedIn) showCart(true) else checkLogin() }
        var dosDis = dm.density * 8
        trans(mCartDots[0], -dosDis, -dosDis)
        trans(mCartDots[1], 0f, -dosDis)
        trans(mCartDots[2], dosDis, -dosDis)
        trans(mCartDots[3], -dosDis, 0f)
        trans(mCartDots[4], 0f, 0f)
        trans(mCartDots[5], dosDis, 0f)
        trans(mCartDots[6], -dosDis, dosDis)
        trans(mCartDots[7], 0f, dosDis)
        trans(mCartDots[8], dosDis, dosDis)
        shadow(c, mCartIcon)
        mMenuClose.setOnClickListener { showCart(false) }
        var close = shadow(c, mMenuClose)
        close?.setPadding(
            mMenuClose.paddingLeft,
            mMenuClose.paddingTop,
            mMenuClose.paddingRight,
            mMenuClose.paddingBottom
        )

        // Menu Items
        val mMenuLinks = resources.getStringArray(R.array.mMenuLinks)
        for (l in 0 until mMenuLinks.size) mMenus[l].setOnClickListener {
            showCart(false)
            mWV.loadUrl(mMenuLinks[l])
        }
        mMenus[mMenus.size - 1].setOnClickListener {
            explode(c, it, 920, R.drawable.circle_cp2, 0.65f, 10f)
            startActivity(Intent(c, Orders::class.java))
        }
        (mMenus[5].getChildAt(0) as ImageView).setColorFilter(ContextCompat.getColor(c, R.color.CP))
        mMnuSh = ArrayList()
        for (m in mMenus) {
            m.id = View.generateViewId()
            mMnuSh.add(shadow(c, m))
        }

        // WooCommerce
        wc = WooCommerce.Builder()
            .setSiteUrl(resources.getString(R.string.site) + "index.php")
            .setApiVersion(WooCommerce.API_V3)
            .setConsumerKey(conKey)
            .setConsumerSecret(conSec)
            .build()
    }

    override fun onBackPressed() {
        if (bCart) showCart(false)
        else {
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
        if (!mLoaded) {
            pageFinished = true
            checkMLoad()
        }
        checkLogin()
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
        checkLogin()
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

    fun checkMLoad() {
        if (dropped && pageFinished) mLoad(false)
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

    fun checkLogin() {//var err: String? = null
        if (::cook.isInitialized && cook.hasCookies()) try {
            var coo = cook.getCookie(resources.getString(R.string.site))
            if (coo != null) {
                cookies = Cookie.parse(coo)
                var li = Cookie.findLogin(cookies)
                if (li != null) {
                    liUser = li.value.split("%")[0]
                    if (liUser != null) {
                        val params = HashMap<String, String>()
                        params["user"] = liUser!!
                        man(
                            c, "?action=login", com.android.volley.Response.Listener { res ->
                                if (res == "notFound") loggedIn(true)
                                else loggedIn(false)
                            }, com.android.volley.Response.ErrorListener { loggedIn(false) },
                            params, "login"
                        )
                    } else loggedIn(false)//;err = "liUser == null"
                } else loggedIn(false)//;err = "li == null"
            } else loggedIn(false)//;err = "coo == null"
        } catch (ignored: Exception) { //err = e.javaClass.name
        } else loggedIn(false)//err = "!::cook.isInitialized && !cook.hasCookies()"
        //if (err != null) Toast.makeText(c, err, Toast.LENGTH_LONG).show()
    }

    fun loggedIn(b: Boolean) {
        if (b && !isLoggedIn) {
            explode(c, mCartIcon)
            mWV.loadUrl(resources.getString(R.string.siteOrders))
        }
        if (!b && isLoggedIn) {
            mWV.loadUrl(resources.getString(R.string.siteLogin))
            if (bCart) showCart(false)
        }
        isLoggedIn = b
        mCartIconBGDis.visibility = if (b) View.INVISIBLE else View.VISIBLE
        for (d in mCartDots) {
            d.setBackgroundResource(if (b) R.drawable.circle_white else R.drawable.circle_cp)
            d.alpha = if (b) 0.92f else 1f
        }
    }

    fun showCart(b: Boolean) {
        if (bCart == b) return
        val cartDur: Long = 720
        if (b) {
            explode(c, mCartIcon)
            mCartIcon.pivotX = mCartIcon.width / 2f
            mCartIcon.pivotY = mCartIcon.height / 2f
            mCartIcon.isClickable = false
            var preBoom = AnimatorSet().setDuration((cartDur / 3) * 2)
            var oas = ArrayList<ObjectAnimator>()
            oas.add(ObjectAnimator.ofFloat(mCartIconIV, "rotation", -(1.4f * 360f)))
            oas.add(ObjectAnimator.ofFloat(mCartIconIV, "alpha", 0f))
            oas.add(ObjectAnimator.ofFloat(mCartIcon, "scaleX", 1.79f))
            oas.add(ObjectAnimator.ofFloat(mCartIcon, "scaleY", 1.79f))
            /*for (d in mCartDots) {
                if (d.translationX != 0f) oas.add(ObjectAnimator.ofFloat(d, "translationX", d.translationX * 50f))
                if (d.translationY != 0f) oas.add(ObjectAnimator.ofFloat(d, "translationY", d.translationY * 50f))
            }*/
            preBoom.playTogether(oas as Collection<Animator>?)
            preBoom.start()
            preBoom.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    mCartIconIV.visibility = View.INVISIBLE

                    var boom = AnimatorSet().setDuration(cartDur)
                    boom.startDelay = 22
                    var scale = (if (dm.heightPixels > dm.widthPixels) dm.heightPixels
                    else dm.widthPixels).toFloat() / mCartIcon.height.toFloat()
                    scale *= 2.22f
                    boom.playTogether(
                        ObjectAnimator.ofFloat(mCartIcon, "scaleX", scale),
                        ObjectAnimator.ofFloat(mCartIcon, "scaleY", scale)
                    )
                    boom.start()

                    mMenu.scaleX = cartScale
                    mMenu.scaleY = cartScale
                    var showCart = AnimatorSet().setDuration(cartDur / 4)
                    showCart.startDelay = (cartDur / 5) * 4
                    showCart.playTogether(
                        ObjectAnimator.ofFloat(mMenu, "alpha", 1f),
                        ObjectAnimator.ofFloat(mMenu, "scaleX", 1f),
                        ObjectAnimator.ofFloat(mMenu, "scaleY", 1f)
                    )
                    showCart.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {//START not End!!!
                            mMenu.visibility = View.VISIBLE
                            bCart = true
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            var showIcons = AnimatorSet().setDuration((cartDur / 3.5f).toLong())
                            var midis = mMenus[0].height * 1.4f//dm.density * 100
                            var o = ArrayList<ObjectAnimator>()
                            var values = arrayOf(
                                0f, -midis, -midis, -(midis / 6f), midis, -(midis / 6f),
                                -(midis / 2f), midis, (midis / 2f), midis
                            )
                            var first = true
                            var shI = 0
                            var sdAddition: Long = 65
                            for (m in 0..((mMenus.size - 2) * 2) + 1) {
                                var prop = if (first) "translationX" else "translationY"
                                var men = ObjectAnimator.ofFloat(mMenus[shI], prop, values[m])
                                var sha = ObjectAnimator.ofFloat(mMnuSh[shI], prop, values[m])
                                var tex = ObjectAnimator.ofFloat(mMnuTV[shI], prop, values[m])
                                men.startDelay = shI * sdAddition
                                sha.startDelay = men.startDelay
                                tex.startDelay = men.startDelay
                                o.add(men)
                                o.add(sha)
                                o.add(tex)
                                if (first) first = false
                                else {
                                    shI += 1; first = true
                                }
                            }
                            showIcons.playTogether(o as Collection<Animator>?)
                            showIcons.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    for (m in mMenus)
                                        explode(c, m, 444, R.drawable.circle_cp2, 0.3f, 2f)
                                }
                            })
                            showIcons.start()

                            var showIconsLonger = AnimatorSet().setDuration(showIcons.duration * 5)
                            var o2 = ArrayList<ObjectAnimator>()
                            for (tex in 0 until mMnuTV.size) {
                                var rot = ObjectAnimator.ofFloat(mMnuTV[tex], "rotation", 360f)
                                var scx = ObjectAnimator.ofFloat(mMnuTV[tex], "scaleX", 1f)
                                var scy = ObjectAnimator.ofFloat(mMnuTV[tex], "scaleY", 1f)
                                var alp = ObjectAnimator.ofFloat(mMnuTV[tex], "alpha", 1f)
                                rot.startDelay = tex * sdAddition
                                scx.startDelay = rot.startDelay
                                scy.startDelay = rot.startDelay
                                alp.startDelay = rot.startDelay
                                o2.add(rot)
                                o2.add(scx)
                                o2.add(scy)
                                o2.add(alp)
                            }
                            showIconsLonger.playTogether(o2 as Collection<Animator>?)
                            showIconsLonger.start()
                        }
                    })
                    showCart.start()
                }
            })
        } else {
            bCart = false
            var hideCart = AnimatorSet().setDuration(cartDur / 4)
            hideCart.playTogether(
                ObjectAnimator.ofFloat(mMenu, "alpha", 0f),
                ObjectAnimator.ofFloat(mMenu, "scaleX", cartScale),
                ObjectAnimator.ofFloat(mMenu, "scaleY", cartScale)
            )
            hideCart.start()
            hideCart.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    mMenu.visibility = View.GONE
                    for (m in mMenus) trans(m, 0f, 0f)
                    for (sh in mMnuSh) if (sh != null) trans(sh, 0f, 0f)
                    for (tex in mMnuTV) {
                        scale(tex, mMITextScale, mMITextScale)
                        tex.rotation = 0f
                        tex.alpha = mMITextAlpha
                    }

                    var crunch = AnimatorSet().setDuration((cartDur / 3) * 2)
                    crunch.playTogether(
                        ObjectAnimator.ofFloat(mCartIcon, "scaleX", 1f),
                        ObjectAnimator.ofFloat(mCartIcon, "scaleY", 1f)
                    )
                    crunch.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            mCartIconIV.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            mCartIcon.isClickable = true
                        }
                    })
                    crunch.start()

                    var postCrunch = AnimatorSet().setDuration(222)
                    postCrunch.startDelay = (crunch.duration / 4) * 3
                    postCrunch.playTogether(
                        ObjectAnimator.ofFloat(mCartIconIV, "rotation", 0f),
                        ObjectAnimator.ofFloat(mCartIconIV, "alpha", 1f)
                    )
                    postCrunch.start()
                }
            })
        }
    }
}
