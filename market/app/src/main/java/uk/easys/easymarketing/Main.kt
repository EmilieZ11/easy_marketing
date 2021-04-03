package uk.easys.easymarketing

import android.animation.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.easys.easymarketing.webview.AdvancedWebView
import uk.easys.easymarketing.woo.WooCommerce
import uk.easys.easymarketing.woo.models.Customer
import uk.easys.easymarketing.woo.models.Order
import uk.easys.easymarketing.woo.models.filters.OrderFilter
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
    lateinit var mCartExplosion: View
    lateinit var mCartIcon: ConstraintLayout
    lateinit var mCartIconBG: View
    lateinit var mCartIconBGDis: View
    lateinit var mCartIconIV: ImageView
    lateinit var mCart: ConstraintLayout
    lateinit var mCartTB: View
    lateinit var mCartClose: ImageView
    lateinit var mCartReload: View
    lateinit var mCartReloadIV: ImageView
    lateinit var mCIList: LinearLayout
    lateinit var mCIList1: TextView
    lateinit var mCIList2: TextView
    lateinit var mCIList3: TextView
    lateinit var mRV: RecyclerView
    lateinit var mRVEmpty: TextView
    lateinit var mLoad: ConstraintLayout
    lateinit var mLoadTV1: ImageView
    lateinit var mLoadTV2: ImageView
    lateinit var mLoadTVCover: View
    lateinit var mLoadIV: ImageView

    lateinit var c: Context
    lateinit var wc: WooCommerce
    lateinit var mAdapter: CartAdapter
    lateinit var cook: CookieManager
    lateinit var cookies: List<Cookie>

    var firstBackToExit = false
    var mLoadDur: Long = 2000
    val supportedRtlLangs = listOf("ar")
    var bCart = false
    var wcLoading = false
    var dropped = false
    var pageFinished = false
    var mLoaded = false
    var isLoggedIn = false
    var liUser: String? = null
    var users: List<Customer>? = null
    var me: Customer? = null
    var isLoading = false
    var loadingFadeDur: Long = 66
    var mLoadingAlpha = 0.95f
    var cartExploding = false
    var cartScale = 0.84f

    companion object {
        lateinit var sp: SharedPreferences

        const val conKey = "ck_51a676dd44e7953afa08eb9552fec58b11b8a0a7"
        const val conSec = "cs_8efa7e08c4a3c3c6366e9467117ef12ff6ecd648"
        const val loadDur: Long = 125
        const val orderID = "orderID"
        const val exSortBy = "sortBy"
        const val exAscending = "ascending"
        var dm = DisplayMetrics()
        var dirLtr = true
        val mCICreated = ArrayList<Boolean>()
        var orders: ArrayList<Order>? = null
        var mRVLManager: LinearLayoutManager? = null
        var sortBy = 0
        var ascending = false
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

        fun compileTime(time: Long): String {
            val lm = Calendar.getInstance()
            lm.timeInMillis = time
            var y = lm.get(Calendar.YEAR)
            var m = lm.get(Calendar.MONTH) + 1
            var d = lm.get(Calendar.DAY_OF_MONTH)
            var h = lm.get(Calendar.HOUR_OF_DAY)
            var n = lm.get(Calendar.MINUTE)
            return "$y." + (if (m < 10) "0" else "") + "$m." + (if (d < 10) "0" else "") + "$d - " +
                    (if (h < 10) "0" else "") + "$h:" + (if (n < 10) "0" else "") + "$n"
        }

        fun rtlBmp(src: Bitmap): Bitmap {
            var amin = Matrix()
            amin.postRotate(180f)
            return Bitmap.createBitmap(src, 0, 0, src.width, src.height, amin, true)
        }

        fun menu(c: Context, v: View, wc: WooCommerce, order: Order, that: AppCompatActivity) {
            val can = "cancelled"
            var popup = PopupMenu(c, v)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.momCancel -> {
                        if (order.status == can) return@setOnMenuItemClickListener true
                        val bull = AlertDialog.Builder(that).apply {
                            setIcon(R.drawable.easy_logo_2_tiny)
                            setTitle(R.string.momCancel)
                            setMessage(R.string.momCancelSure)
                            setPositiveButton(R.string.yes) { _, _ ->
                                var newOrder = order
                                newOrder.status = can
                                wc.OrderRepository().update(order.id, newOrder)
                                    .enqueue(object : Callback<Order> {
                                        override fun onResponse(
                                            call: Call<Order>,
                                            response: Response<Order>
                                        ) {
                                            if (orders == null) return
                                            var pos = orders!!.indexOf(order)
                                            orders!![pos].status = can
                                            orders!![pos].stat = statusToStat(can)
                                            if (mRVLManager == null) return
                                            var item =
                                                mRVLManager!!.findViewByPosition(pos) as LinearLayout
                                            var ll1 = item.getChildAt(0) as LinearLayout
                                            var parent = ll1.getChildAt(4) as ConstraintLayout
                                            var iv = parent.getChildAt(0) as ImageView
                                            CartAdapter.statIcon(c, order, iv, parent, that)
                                        }

                                        override fun onFailure(call: Call<Order>, t: Throwable) {
                                        }
                                    })
                            }
                            setNegativeButton(R.string.no) { _, _ -> }
                        }
                        bull.create().show()
                        return@setOnMenuItemClickListener true
                    }
                    else -> return@setOnMenuItemClickListener false
                }
            }
            popup.inflate(R.menu.order_more)
            popup.show()
        }

        fun statusToStat(status: String) = when (status) {
            //["any","pending","processing","driver-assigned","out-for-delivery",
            // "order-returned","on-hold","completed","cancelled","refunded","failed"]
            "out-for-delivery" -> 0
            "driver-assigned" -> 1
            "failed" -> 2
            "completed" -> 4
            "cancelled" -> 5
            else -> 3
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
        mCartExplosion = findViewById(R.id.mCartExplosion)
        mCartIcon = findViewById(R.id.mCartIcon)
        mCartIconBG = mCartIcon.getChildAt(0) as View
        mCartIconBGDis = mCartIcon.getChildAt(1) as View
        mCartIconIV = mCartIcon.getChildAt(2) as ImageView
        mCart = findViewById(R.id.mCart)
        mCartTB = findViewById(R.id.mCartTB)
        mCartClose = findViewById(R.id.mCartClose)
        mCartReload = findViewById(R.id.mCartReload)
        mCartReloadIV = findViewById(R.id.mCartReloadIV)
        mCIList = findViewById(R.id.mCIList)
        mCIList1 = mCIList.getChildAt(1) as TextView
        mCIList2 = mCIList.getChildAt(2) as TextView
        mCIList3 = mCIList.getChildAt(3) as TextView
        mRV = findViewById(R.id.mRV)
        mRVEmpty = findViewById(R.id.mRVEmpty)
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
        sp = PreferenceManager.getDefaultSharedPreferences(c)


        // Loading
        if (isOnline()) {
            mWV.setListener(this, this)
            mWV.loadUrl(resources.getString(R.string.site))
            mWV.setCookiesEnabled(true)
            mWV.setThirdPartyCookiesEnabled(true)//Ask him
            cook = mWV.cookieManager//ALWAYS AFTER "setCookiesEnabled"
        } else mLoad(false)
        mLoad.setOnClickListener {}
        val anLoad1 = va(mMotor1, "translationX", loadDur, 10f, 0f, 1970)
        anLoad1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                var dest = mLoadIV.width * 0.86f
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
                        checkMLoad()
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

        // Cart
        loggedIn(false)
        mCartIcon.setOnClickListener {
            if (me != null) showCart(true)
            else mWV.loadUrl(resources.getString(R.string.site) + resources.getString(R.string.loginURL))
        }
        mCartClose.setOnClickListener { showCart(false) }
        if (!dirLtr) {
            mCartIcon.pivotX = 0f
            mCartIconBG.rotationY = 180f
            var mCartIconIVLP = mCartIconIV.layoutParams as ConstraintLayout.LayoutParams
            mCartIconIVLP.horizontalBias = 0.5f - (mCartIconIVLP.horizontalBias - 0.5f)
            mCartIconIV.layoutParams = mCartIconIVLP
        }
        mRV.setHasFixedSize(false)
        mRVLManager = LinearLayoutManager(c)
        mRV.layoutManager = mRVLManager

        // WooCommerce
        wc = WooCommerce.Builder()
            .setSiteUrl(resources.getString(R.string.site) + "index.php")
            .setApiVersion(WooCommerce.API_V3)
            .setConsumerKey(conKey)
            .setConsumerSecret(conSec)
            .build()
        mCartReload.setOnClickListener { wcOrders() }
        wcCustomers()

        // Order Titles and Sorting
        if (sp.contains(exSortBy)) sortBy = sp.getInt(exSortBy, 0)
        if (sp.contains(exAscending)) ascending = sp.getBoolean(exAscending, false)
        mCIList1.setOnClickListener { if (::mAdapter.isInitialized) mAdapter.sort(0, !ascending) }
        mCIList2.setOnClickListener { if (::mAdapter.isInitialized) mAdapter.sort(1, !ascending) }
        mCIList3.setOnClickListener { if (::mAdapter.isInitialized) mAdapter.sort(2, !ascending) }
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

    fun wcCustomers() {
        wc.CustomerRepository().customers().enqueue(object : Callback<List<Customer>> {
            override fun onResponse(
                call: Call<List<Customer>>,
                response: Response<List<Customer>>
            ) {
                users = response.body()
            }

            override fun onFailure(call: Call<List<Customer>>, t: Throwable) {
                users = null
            }
        })
    }

    fun whosThis(username: String, users: List<Customer>): Customer? {
        var c: Customer? = null
        for (u in users) if (username == u.username) c = u
        return c
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
                        if (users != null) {
                            me = whosThis(liUser!!, users!!)
                            if (me != null) loggedIn(true)
                            else loggedIn(false)//;err = "me == null"
                        } else {
                            loggedIn(false)//;err = "users == null"
                            wcCustomers()
                        }
                    } else loggedIn(false)//;err = "liUser == null"
                } else loggedIn(false)//;err = "li == null"
            } else loggedIn(false)//;err = "coo == null"
        } catch (ignored: Exception) { //err = e.javaClass.name
        } else loggedIn(false)//err = "!::cook.isInitialized && !cook.hasCookies()"
        //if (err != null) Toast.makeText(c, err, Toast.LENGTH_LONG).show()
    }

    fun loggedIn(b: Boolean) {
        if (!b) me = null
        if (b && !isLoggedIn) cartExplode()
        isLoggedIn = b
        mCartIconBGDis.visibility = if (b) View.INVISIBLE else View.VISIBLE
        if (b) mCartIconIV.clearColorFilter()
        else mCartIconIV.setColorFilter(ContextCompat.getColor(c, R.color.mCartIconCF))
        mCartIconIV.alpha = if (b) 0.92f else 1f
        mCartIconIV.setImageResource(if (b) R.drawable.purchase_2 else R.drawable.user_1)

        var mCartIconIVLP = mCartIconIV.layoutParams as ConstraintLayout.LayoutParams
        mCartIconIVLP.verticalBias = if (b) 0.55f else 0.5f
        mCartIconIV.layoutParams = mCartIconIVLP
    }

    fun cartExplode(dur: Long = 522) {
        if (cartExploding) return
        cartExploding = true
        mCartExplosion.alpha = 1f
        var explode = AnimatorSet().setDuration(dur)
        var hide = ObjectAnimator.ofFloat(mCartExplosion, "alpha", 0f)
        hide.startDelay = explode.duration / 4
        explode.playTogether(
            ObjectAnimator.ofFloat(mCartExplosion, "scaleX", 4f),
            ObjectAnimator.ofFloat(mCartExplosion, "scaleY", 4f),
            hide
        )
        explode.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                mCartExplosion.alpha = 0f
                mCartExplosion.scaleX = 1f
                mCartExplosion.scaleY = 1f
                cartExploding = false
            }
        })
        explode.start()
    }

    fun showCart(b: Boolean) {
        if (bCart == b) return
        if (b) {
            wcOrders()
            cartExplode()
            mCartIcon.pivotX = mCartIcon.width / 2f
            mCartIcon.pivotY = mCartIcon.height / 2f
            mCartIcon.isClickable = false
            var preBoom = AnimatorSet().setDuration(920)
            preBoom.playTogether(
                ObjectAnimator.ofFloat(mCartIconIV, "rotation", -(1.4f * 360f)),
                ObjectAnimator.ofFloat(mCartIconIV, "alpha", 0f),
                ObjectAnimator.ofFloat(mCartIcon, "scaleX", 1.79f),
                ObjectAnimator.ofFloat(mCartIcon, "scaleY", 1.79f)
            )
            preBoom.start()
            preBoom.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    mCartIconIV.visibility = View.INVISIBLE

                    var boom = AnimatorSet().setDuration(920)
                    boom.startDelay = 22
                    var scale = (if (dm.heightPixels > dm.widthPixels) dm.heightPixels
                    else dm.widthPixels).toFloat() / mCartIcon.height.toFloat()
                    scale *= 2.22f
                    boom.playTogether(
                        ObjectAnimator.ofFloat(mCartIcon, "scaleX", scale),
                        ObjectAnimator.ofFloat(mCartIcon, "scaleY", scale)
                    )
                    boom.start()

                    mCart.scaleX = cartScale
                    mCart.scaleY = cartScale
                    var showCart = AnimatorSet().setDuration(boom.duration / 3)
                    showCart.startDelay = (boom.duration / 5) * 4
                    showCart.playTogether(
                        ObjectAnimator.ofFloat(mCart, "alpha", 1f),
                        ObjectAnimator.ofFloat(mCart, "scaleX", 1f),
                        ObjectAnimator.ofFloat(mCart, "scaleY", 1f)
                    )
                    showCart.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {//START not End!!!
                            mCart.visibility = View.VISIBLE
                            bCart = true
                        }
                    })
                    showCart.start()
                }
            })
        } else {
            bCart = false
            var hideCart = AnimatorSet().setDuration(111)
            hideCart.playTogether(
                ObjectAnimator.ofFloat(mCart, "alpha", 0f),
                ObjectAnimator.ofFloat(mCart, "scaleX", cartScale),
                ObjectAnimator.ofFloat(mCart, "scaleY", cartScale)
            )
            hideCart.start()
            hideCart.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    mCart.visibility = View.GONE
                    var crunch = AnimatorSet().setDuration(920)
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

    fun wcOrders() {
        if (wcLoading || me == null) return
        whirlReload()
        wcLoading = true
        var that = this
        val filter = OrderFilter()
        filter.setCustomer(me!!.id)//REMEMBER: ALWAYS USE set...() INSTEAD OF ... = ...
        wc.OrderRepository().orders(filter).enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                wcLoading = false
                orders = response.body() as ArrayList<Order>?
                if (orders != null) {
                    if (orders!!.size > 0) {
                        for (o in 0 until orders!!.size) {
                            var ord = orders!![o]
                            ord.stat = statusToStat(ord.status)
                            orders!![o] = ord
                        }

                        mRVEmpty.visibility = View.GONE
                        mRVEmpty.alpha = 0f
                        mRVEmpty.translationY = dp(-111).toFloat()

                        Collections.sort(orders, SortOrders())
                        mCICreated.clear()
                        for (m in orders!!) mCICreated.add(false)
                        mAdapter = CartAdapter(c, orders!!, wc, that)
                        mRV.adapter = mAdapter
                    } else {
                        var mRVEmptyDur: Long = 179
                        oa(mRVEmpty, "alpha", mRVEmptyDur, 1f)
                        oa(mRVEmpty, "translationY", mRVEmptyDur, 0f)
                        mRVEmpty.visibility = View.VISIBLE
                    }
                } else mWCError()
            }

            //Use this shitty "t" if any error occurred!
            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                mWCError()
            }
        })
    }

    fun mWCError() {
        wcLoading = false
        Toast.makeText(c, R.string.mWCErrorUnknown, Toast.LENGTH_LONG).show()
        if (::mAdapter.isInitialized) mAdapter.clear()
    }

    fun whirlReload() {
        var wr = va(mCartReloadIV, "rotation", 522, 0f, 360f)
        wr.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (wcLoading) whirlReload()
            }
        })
    }


    class SortOrders(val by: Int = sortBy, val asc: Boolean = ascending) : Comparator<Order> {
        override fun compare(a: Order, b: Order): Int {
            var aa = if (asc) a else b
            var bb = if (asc) b else a
            return when (by) {
                1 -> aa.total.toFloat().compareTo(bb.total.toFloat())
                2 -> aa.stat.compareTo(bb.stat)
                else -> aa.dateCreated.time.compareTo(bb.dateCreated.time)
            }
        }
    }
}
