package uk.easys.easydelivery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.res.Configuration
import android.graphics.Typeface
import android.location.Location
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.easys.easydelivery.woo.WooCommerce
import uk.easys.easydelivery.woo.models.Customer
import uk.easys.easydelivery.woo.models.Order
import uk.easysy.easydelivery.OrderAdapter
import java.lang.Exception
import java.util.*
import kotlin.system.exitProcess

// SHOW THE WAY TO THE DESTINATION (OPTIONAL)
class Delivery : AppCompatActivity() {
    lateinit var dBody: ConstraintLayout
    lateinit var dMotor1: View
    lateinit var dToolbar: Toolbar
    lateinit var dMap: SupportMapFragment
    lateinit var dNavBG: View
    lateinit var dNavShadow: View
    lateinit var dNav: ConstraintLayout
    lateinit var dNavLogo: ConstraintLayout
    lateinit var dNL0: ImageView
    lateinit var dNL1: ImageView
    lateinit var dNL2: ImageView
    lateinit var dOrdReload: View
    lateinit var dOrdReloadIV: ImageView
    lateinit var dOrders: RecyclerView
    lateinit var dOrdEmpty: TextView
    lateinit var dStatuses: LinearLayout
    lateinit var dChangeStatus: ConstraintLayout
    lateinit var dChangeStatusTV: TextView

    lateinit var c: Context
    lateinit var lr: LocationRequest
    lateinit var flpc: FusedLocationProviderClient
    lateinit var lc: LocationCallback
    lateinit var dTBTitle: TextView
    lateinit var wc: WooCommerce
    lateinit var dOrdAdapter: OrderAdapter
    lateinit var user: String
    lateinit var pass: String

    var here: Location? = null
    var firstBackToExit = false
    val reqCheckSettings = 1110
    var me: Marker? = null
    var navOpened = false
    var gettingOrders = false
    var gettingCustomers = false
    var filteringOrders = false
    var fOrders: ArrayList<Order>? = null
    var navLoaded = false
    var statuses: Array<String>? = null
    var statusesCodes: Array<String>? = null
    var statusesRes = listOf(
        R.drawable.motorcycle_1, R.drawable.motorcycle_1, R.drawable.ord_failed_1,
        R.drawable.ord_completed_1, R.drawable.ord_cancelled_1
    )
    var motorcycle = R.drawable.motorcycle_1
    var firstShowMe = true

    companion object {
        lateinit var dStatusesSV: ScrollView
        lateinit var dCover1: View
        lateinit var map: GoogleMap
        lateinit var coCu: LatLng

        const val conKey = "ck_51a676dd44e7953afa08eb9552fec58b11b8a0a7"
        const val conSec = "cs_8efa7e08c4a3c3c6366e9467117ef12ff6ecd648"
        const val interval: Long = 15000
        var dm = DisplayMetrics()
        var orders: List<Order>? = null
        var customers: List<Customer>? = null
        var dOrdLManager: LinearLayoutManager? = null
        val dOrdCreated = ArrayList<Boolean>()
        var cu: Marker? = null
        var selected = -1
        var changeStatId = -1
        var nonMotorAlpha = 0.98f

        fun findCusById(id: Int, customers: List<Customer>?): Customer? {
            if (customers == null) return null
            var cus: Customer? = null
            for (c in customers) if (c.id == id) cus = c
            return cus
        }

        fun findOrdById(id: Int, orders: List<Order>?): Order? {
            if (orders == null) return null
            var ord: Order? = null
            for (o in orders) if (o.id == id) ord = o
            return ord
        }

        fun select(c: Context, i: Int) {
            if (dOrdLManager == null || i >= dOrdLManager!!.childCount || i == selected) return
            disselectAll()
            selected = i
            var item = dOrdLManager!!.findViewByPosition(i) as LinearLayout
            var ll1 = item.getChildAt(0) as ConstraintLayout
            ll1.background = null
            ll1.setBackgroundColor(ContextCompat.getColor(c, R.color.mCartItemPressed))
            var ll2 = item.getChildAt(2) as ConstraintLayout
            val list = ll2.getChildAt(0) as LinearLayout
            if (ll2.visibility == View.GONE) {
                list.alpha = 0f
                ll2.scaleY = 0f
                ll2.visibility = View.VISIBLE
                var scroll = Login.oa(ll2, "scaleY", 222, 1f)
                scroll.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        Login.oa(list, "alpha", 92, 1f)
                    }
                })
            }

            if (cu != null) {
                coCu = when (i) {
                    0 -> LatLng(33.307931, 44.378340)
                    1 -> LatLng(34.181857, 43.898252)
                    2 -> LatLng(33.747976, 44.606542)
                    3 -> LatLng(32.016320, 44.330056)
                    4 -> LatLng(30.518779, 47.798440)
                    5 -> LatLng(36.870392, 42.981892)
                    6 -> LatLng(33.026574, 40.286251)
                    else -> LatLng(33.344725, 44.272188)
                }
                cu!!.position = coCu
                cu!!.isVisible = true
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(coCu, 16f))
            }
        }

        fun disselectAll() {
            selected = -1
            for (o in 0 until dOrdLManager!!.childCount) if (dOrdLManager!!.findViewByPosition(o) != null) {
                var item = dOrdLManager!!.findViewByPosition(o) as LinearLayout
                var ll1 = item.getChildAt(0) as ConstraintLayout
                ll1.setBackgroundResource(R.drawable.m_cart_item_bg)
                var ll2 = item.getChildAt(2) as ConstraintLayout
                if (ll2.visibility == View.VISIBLE) ll2.visibility = View.GONE
            }
        }

        fun showChangeStat(b: Boolean, id: Int = -1) {
            if (!::dStatusesSV.isInitialized) return
            cover(0, b)
            var dur: Long = 92
            if (b) {
                dStatusesSV.visibility = View.VISIBLE
                Login.oa(dStatusesSV, "alpha", dur, 1f)
                if (::dCover1.isInitialized) dCover1.setOnClickListener { showChangeStat(false) }
            } else {
                var h = Login.oa(dStatusesSV, "alpha", dur, 0f)
                h.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        dStatusesSV.visibility = View.GONE
                    }
                })
                if (::dCover1.isInitialized) dCover1.setOnClickListener(null)
            }
            changeStatId = id
        }

        fun cover(i: Int, b: Boolean, dur: Long = 92) {
            var cov = dCover1
            if (b) {
                cov.visibility = View.VISIBLE
                Login.oa(cov, "alpha", dur, 1f)
            } else {
                var h = Login.oa(cov, "alpha", dur, 0f)
                h.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        cov.visibility = View.GONE
                    }
                })
            }
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
        setContentView(R.layout.delivery)

        dBody = findViewById(R.id.dBody)
        dMotor1 = findViewById(R.id.dMotor1)
        dToolbar = findViewById(R.id.dToolbar)
        dMap = supportFragmentManager.findFragmentById(R.id.dMap) as SupportMapFragment
        dNavBG = findViewById(R.id.dNavBG)
        dNavShadow = findViewById(R.id.dNavShadow)
        dNav = findViewById(R.id.dNav)
        dNavLogo = findViewById(R.id.dNavLogo)
        dNL0 = findViewById(R.id.dNL0)
        dNL1 = findViewById(R.id.dNL1)
        dNL2 = findViewById(R.id.dNL2)
        dOrdReload = findViewById(R.id.dOrdReload)
        dOrdReloadIV = findViewById(R.id.dOrdReloadIV)
        dOrders = findViewById(R.id.dOrders)
        dOrdEmpty = findViewById(R.id.dOrdEmpty)
        dCover1 = findViewById(R.id.dCover1)
        dStatusesSV = findViewById(R.id.dStatusesSV)
        dStatuses = findViewById(R.id.dStatuses)
        dChangeStatus = findViewById(R.id.dChangeStatus)
        dChangeStatusTV = findViewById(R.id.dChangeStatusTV)

        c = applicationContext
        if (intent.extras != null && intent.extras.containsKey(Login.exUser)
            && intent.extras.containsKey(Login.exPass)
        ) {
            user = intent.extras.getString(Login.exUser)
            pass = intent.extras.getString(Login.exPass)
            update(user, pass)
        } else {
            Toast.makeText(c, R.string.incomInfo, Toast.LENGTH_LONG).show()
            // STOP EVERYTHING
        }
        windowManager.defaultDisplay.getMetrics(dm)
        if (!Login.dirLtr) dBody.layoutDirection = View.LAYOUT_DIRECTION_RTL


        // Request Location
        flpc = LocationServices.getFusedLocationProviderClient(c)
        flpc.lastLocation.addOnSuccessListener { here = it }
        lr = LocationRequest.create()
        lr.interval = interval
        lr.fastestInterval = lr.interval / 2
        lr.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        var lrBuilder = LocationSettingsRequest.Builder().addLocationRequest(lr)
        var lrTask = LocationServices.getSettingsClient(c).checkLocationSettings(lrBuilder.build())
        lrTask.addOnSuccessListener { showMe() }
        lrTask.addOnFailureListener {
            if (it is ResolvableApiException) try {
                var resolvable = it as ResolvableApiException
                resolvable.startResolutionForResult(this, reqCheckSettings)
            } catch (ignored: IntentSender.SendIntentException) {
            }
        }

        // Toolbar
        setSupportActionBar(dToolbar)
        dToolbar.overflowIcon = ContextCompat.getDrawable(c, R.drawable.overflow_2)
        for (g in 0 until dToolbar.childCount) {
            var getTitle = dToolbar.getChildAt(g)
            if (getTitle.javaClass.name.equals("androidx.appcompat.widget.AppCompatTextView", true))
                when ((getTitle as TextView).text.toString()) {
                    resources.getString(R.string.dTBTitle) -> dTBTitle = getTitle
                }
        }
        dTBTitle.typeface = Typeface.DEFAULT_BOLD
        //dTBTitle.textSize = dm.density * 18
        dToolbar.setNavigationOnClickListener { nav(!navOpened) }
        if (!Login.dirLtr) dNavShadow.rotationY = 180f

        // Navigation Menu
        dNavBG.setOnClickListener { nav(false) }
        dOrders.setHasFixedSize(false)
        dOrdLManager = LinearLayoutManager(c)
        dOrders.layoutManager = dOrdLManager
        dNavLogo.setOnClickListener {}
        dOrdReload.setOnClickListener { loadNav() }

        // Map
        dMap.getMapAsync {
            map = it
            var theme = R.raw.day
            var date = Calendar.getInstance()
            var sun = when (date.get(Calendar.MONTH)) {
                Calendar.JANUARY -> arrayOf(7, 17)
                Calendar.FEBRUARY -> arrayOf(7, 18)
                Calendar.MARCH -> arrayOf(7, 18)
                Calendar.APRIL -> arrayOf(6, 18)
                Calendar.MAY -> arrayOf(5, 18)
                Calendar.JUNE -> arrayOf(5, 19)
                Calendar.JULY -> arrayOf(5, 19)
                Calendar.AUGUST -> arrayOf(5, 19)
                Calendar.SEPTEMBER -> arrayOf(6, 18)
                Calendar.OCTOBER -> arrayOf(6, 18)
                Calendar.NOVEMBER -> arrayOf(7, 17)
                Calendar.DECEMBER -> arrayOf(7, 17)
                else -> arrayOf(6, 18)
            }
            val hour = date.get(Calendar.HOUR_OF_DAY)
            if (hour < sun[0] || hour >= sun[1]) {
                theme = R.raw.night
                motorcycle = R.drawable.motorcycle_1_night
                if (me != null) me?.setIcon(BitmapDescriptorFactory.fromResource(motorcycle))
            }
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(c, theme))
            map.setMinZoomPreference(6f)

            /*try {
                var gc = Geocoder(c)
                var la = gc.getFromLocationName("Kazakhstan", 1)
                coCu = LatLng(la[0].latitude, la[0].longitude)
            } catch (e: Exception) {

                Toast.makeText(
                    c,
                    e.javaClass.name + (if (e.message != null) ": " + e.message else ""),
                    Toast.LENGTH_LONG
                ).show()
            }*/
            coCu = LatLng(29.375737, 47.967231)
            cu =
                map.addMarker(MarkerOptions().position(coCu).title(resources.getString(R.string.dCus)))
            cu!!.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_2_customer))
            cu!!.isVisible = false

            map.isMyLocationEnabled = true
        }

        // WooCommerce
        wc = WooCommerce.Builder()
            .setSiteUrl(resources.getString(R.string.site) + "index.php")
            .setApiVersion(WooCommerce.API_V3)
            .setConsumerKey(conKey)
            .setConsumerSecret(conSec)
            .build()
        loadNav()

        // Change Status
        statuses = resources.getStringArray(R.array.statuses)
        statusesCodes = resources.getStringArray(R.array.statusesCodes)
        if (statuses != null && statusesCodes != null) for (s in 0 until statuses!!.size) {
            var statVG = ConstraintLayout(c)
            var statVGLP = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Login.dp(50)
            )
            statVG.id = View.generateViewId()
            statVG.background = ContextCompat.getDrawable(c, R.drawable.m_cart_item_bg)
            dStatuses.addView(statVG, statVGLP)
            statVG.setOnClickListener {
                changeStat(changeStatId, statusesCodes!![s], this)
                showChangeStat(false)
            }

            var statIV = ImageView(c)
            var statIVLP =
                ConstraintLayout.LayoutParams(Login.dp(50), ViewGroup.LayoutParams.MATCH_PARENT)
            statIVLP.startToStart = statVG.id
            var statIVPad = Login.dp(12)
            statIV.setPadding(statIVPad, statIVPad, statIVPad, statIVPad)
            statIV.setImageResource(statusesRes[s])
            if (s == 1) statIV.setColorFilter(ContextCompat.getColor(c, R.color.dRVStatDis))
            statVG.addView(statIV, statIVLP)

            var statTV = TextView(c)
            var statTVLP = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            statTVLP.startToStart = statVG.id
            statTVLP.endToEnd = statVG.id
            statTVLP.marginStart = statIVLP.width
            statTV.setPaddingRelative(Login.dp(10), 0, Login.dp(10), 0)
            statTV.text = statuses!![s]
            statTV.gravity = Gravity.CENTER_VERTICAL
            statTV.textSize = 20f
            statTV.setTextColor(ContextCompat.getColor(c, R.color.dStatTV))
            statVG.addView(statTV, statTVLP)


            var statSpace = View(c)
            var statSpaceLP =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Login.dp(1))
            statSpace.setBackgroundColor(ContextCompat.getColor(c, R.color.dOrdSpace))
            dStatuses.addView(statSpace, statSpaceLP)
        }
    }

    override fun onBackPressed() {
        if (navOpened) nav(false)
        else {
            if (!firstBackToExit) {
                firstBackToExit = true
                Toast.makeText(c, R.string.toExit, Toast.LENGTH_LONG).show()
                val exit = Login.va(dMotor1, "translationX", 4000, 4f, 0f)
                exit.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        firstBackToExit = false
                    }
                })
            } else {
                moveTaskToBack(true)
                Process.killProcess(Process.myPid())
                exitProcess(1)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val anCon = Login.va(dMotor1, "translationX", Login.loadDur, 10f, 0f)
        anCon.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                windowManager.defaultDisplay.getMetrics(Login.dm)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            reqCheckSettings -> showMe()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.d_toolbar, menu)
        return super.onCreateOptionsMenu(menu)//DON"T PUT HERE THINGS THAT NEED THE LAYOUT LOADED.
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.dCall -> /*if (selected == -1)
                Toast.makeText(c, R.string.firstSpecifyCustomer, Toast.LENGTH_LONG).show()
            else*/ {
            }
            R.id.dLogOut -> {//CAREFUL: "this" NOT "c"!!!
                val bull = AlertDialog.Builder(this).apply {
                    setIcon(R.drawable.easy_logo_2_tiny)
                    setTitle(R.string.dLogOut)
                    setMessage(R.string.dLogOutSure)
                    setPositiveButton(R.string.yes) { _, _ ->
                        var spe = Login.sp.edit()
                        spe.remove(Login.exUser)
                        spe.remove(Login.exPass)
                        spe.apply()
                        startActivity(Intent(c, Login::class.java))
                        finish()
                    }
                    setNegativeButton(R.string.no) { _, _ -> }
                }
                bull.create().show()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }


    fun showMe() {
        lc = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) return
                for (location in locationResult.locations) if (location != null) {
                    here = location
                    if (firstShowMe) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(here!!.latitude, here!!.longitude), 16f))
                        firstShowMe = false
                    }
                }
            }
        }
        flpc.requestLocationUpdates(lr, lc, Looper.getMainLooper())
    }

    fun update(user: String, pass: String) {
        if (here != null) {
            val params = HashMap<String, String>()
            params["user"] = user
            params["pass"] = pass
            val url =
                "?action=whereIAm&latitude=${here?.latitude}&longtitude=${here?.longitude}&time=${here?.time}"
            Login.del(c, url, com.android.volley.Response.Listener { res ->
                if (res != "ok") Toast.makeText(c, res, Toast.LENGTH_LONG).show()
            }, com.android.volley.Response.ErrorListener {
                Toast.makeText(c, R.string.couldNotUpdate, Toast.LENGTH_LONG).show()
            }, params)
        }
        object : CountDownTimer(interval, interval / 2) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                update(user, pass)
            }
        }.start()
    }

    fun nav(b: Boolean) {
        if (navOpened == b) return
        navOpened = b
        var navDur: Long = 111
        if (b) {
            dNavBG.visibility = View.VISIBLE
            Login.oa(dNavBG, "alpha", navDur, 1f)
            dNav.translationX = whereNavBelongs()
            dNav.visibility = View.VISIBLE
            dNavShadow.translationX = whereNavBelongs()
            dNavShadow.visibility = View.VISIBLE
            Login.oa(dNav, "translationX", navDur, 0f)
            Login.oa(dNavShadow, "translationX", navDur, 0f)
        } else {
            Login.oa(dNav, "translationX", navDur, whereNavBelongs())
                .addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        dNav.visibility = View.GONE
                        dNavShadow.visibility = View.GONE
                    }
                })
            Login.oa(dNavShadow, "translationX", navDur, whereNavBelongs())
            Login.oa(dNavBG, "alpha", navDur, 0f).addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        dNavBG.visibility = View.GONE
                        dNavBG.alpha = 0f
                    }
                })
        }
    }

    fun whereNavBelongs() =
        if (!Login.dirLtr) dNav.width.toFloat() + 10f
        else 0f - (dNav.width.toFloat() + 10f)

    fun loadNav() {
        if (gettingOrders || filteringOrders || gettingCustomers) return
        whirlReload()
        navLoaded = false
        orders = null
        fOrders = null
        customers = null

        wcOrders()
        wcCustomers()
    }

    fun wcOrders() {
        gettingOrders = true
        wc.OrderRepository().orders().enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                gettingOrders = false
                var all = response.body()
                if (all != null) {
                    orders = all
                    filterOrders()
                }
                checkWC()
            }

            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                gettingOrders = false
                checkWC()
            }
        })
    }

    fun filterOrders() {
        filteringOrders = true
        var sb = StringBuilder()
        for (o in 0 until orders!!.size) {// What an ass!!!
            sb.append("${orders!![o].id}")
            if (o != orders!!.size - 1) sb.append(";")
        }
        val params = HashMap<String, String>()
        params["user"] = user
        params["pass"] = pass
        params["orders"] = sb.toString()
        Login.del(c, "?action=filterOrders", com.android.volley.Response.Listener { res ->
            filteringOrders = false
            if (res != null && res != "" && res.length >= 5 && res.substring(0, 5) == "aaaaa") {
                fOrders = ArrayList()
                var filtered = res.substring(5)
                if (filtered != "") {
                    var split = filtered.split(";")
                    for (s in split) try {
                        var ord = findOrdById(Integer.parseInt(s), orders)
                        if (ord != null) fOrders!!.add(ord)
                    } catch (e: Exception) {
                    }
                }
            }
            checkWC()
        }, com.android.volley.Response.ErrorListener {
            filteringOrders = false
            checkWC()
        }, params)
    }

    fun wcCustomers() {
        gettingCustomers = true
        wc.CustomerRepository().customers().enqueue(object : Callback<List<Customer>> {
            override fun onResponse(
                call: Call<List<Customer>>,
                response: Response<List<Customer>>
            ) {
                gettingCustomers = false
                var all = response.body()
                if (all != null) customers = all
                checkWC()
            }

            override fun onFailure(call: Call<List<Customer>>, t: Throwable) {
                gettingCustomers = false
                checkWC()
            }
        })
    }

    fun checkWC() {
        if (gettingOrders || filteringOrders || gettingCustomers) return
        if (orders == null || fOrders == null || customers == null) {
            if (!Login.isOnline(c)) Toast.makeText(c, R.string.noInternet, Toast.LENGTH_LONG).show()
            else {
                if (orders == null) wcOrders()
                else if (fOrders == null) filterOrders()
                if (customers == null) wcCustomers()
            }
        } else {
            navLoaded = true
            if (fOrders!!.size > 0) {
                dOrdEmpty.visibility = View.GONE
                dOrdEmpty.alpha = 0f
                dOrdEmpty.translationY = Login.dp(-111).toFloat()

                for (o in 0 until fOrders!!.size) {
                    var ord = fOrders!![o]
                    ord.stat = statusToStat(ord.status)
                    fOrders!![o] = ord
                }

                Collections.sort(fOrders, SortOrders())
                fOrders!!.reverse()
                Collections.sort(fOrders, SortOrders(2))
                dOrdCreated.clear()
                for (m in fOrders!!) dOrdCreated.add(false)
                if (::dOrdAdapter.isInitialized) dOrdAdapter.clear()
                dOrdAdapter = OrderAdapter(c, fOrders!!, wc, this)
                dOrders.adapter = dOrdAdapter
            } else {
                var mRVEmptyDur: Long = 179
                Login.oa(dOrdEmpty, "alpha", mRVEmptyDur, 1f)
                Login.oa(dOrdEmpty, "translationY", mRVEmptyDur, 0f)
                dOrdEmpty.visibility = View.VISIBLE
            }
        }
    }

    fun statusToStat(status: String) = when (status) {
        "out-for-delivery" -> 0
        "driver-assigned" -> 1
        "failed" -> 2
        "completed" -> 4
        "cancelled" -> 5
        else -> 3
    }

    fun whirlReload() {
        var wr = Login.va(dOrdReloadIV, "rotation", 522, 0f, 360f)
        wr.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (!navLoaded) whirlReload()
            }
        })
    }

    fun changeStat(id: Int, stat: String, that: AppCompatActivity) {
        if (fOrders == null) return
        var ord: Order? = findOrdById(id, fOrders) ?: return
        if (ord!!.status == stat) return
        ord.status = stat
        wc.OrderRepository().update(id, ord).enqueue(object : Callback<Order> {
            override fun onResponse(
                call: Call<Order>,
                response: Response<Order>
            ) {
                if (fOrders == null) return
                var pos = fOrders!!.indexOf(ord)
                fOrders!![pos].status = stat
                fOrders!![pos].stat = statusToStat(stat)
                if (dOrdLManager == null) return
                var item = dOrdLManager!!.findViewByPosition(pos) as LinearLayout
                var ll1 = item.getChildAt(0) as ConstraintLayout
                var iv = ll1.getChildAt(1) as ImageView
                OrderAdapter.statIcon(c, ord, iv, ll1, that)
            }

            override fun onFailure(call: Call<Order>, t: Throwable) {}
        })
    }


    class SortOrders(val by: Int = 0) : Comparator<Order> {
        override fun compare(a: Order, b: Order): Int {
            when (by) {
                1 -> return a.total.compareTo(b.total)
                2 -> return a.stat.compareTo(b.stat)
                else -> return a.dateCreated.time.compareTo(b.dateCreated.time)
            }
        }
    }
}
