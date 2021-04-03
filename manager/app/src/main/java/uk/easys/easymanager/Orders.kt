package uk.easys.easymanager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.easys.easymanager.Main.Companion.dp
import uk.easys.easymanager.Main.Companion.liUser
import uk.easys.easymanager.Main.Companion.man
import uk.easys.easymanager.woo.WooCommerce
import uk.easys.easymanager.woo.models.Customer
import uk.easys.easymanager.woo.models.Order
import java.io.ByteArrayInputStream
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class Orders : AppCompatActivity() {
    lateinit var oBody: ConstraintLayout
    lateinit var oMotor1: View
    lateinit var oToolbar: View
    lateinit var oBack: ImageView
    lateinit var oReload: View
    lateinit var oReloadIV: ImageView
    lateinit var oTitle: LinearLayout
    lateinit var oTitle1: TextView
    lateinit var oTitle2: TextView
    lateinit var oTitle3: TextView
    lateinit var oRVEmpty: TextView
    lateinit var oBNV: BottomNavigationView

    lateinit var c: Context
    lateinit var wc: WooCommerce

    var wcLoaded = false
    var gettingOrders = false
    var gettingCustomers = false
    var gettingDrivers = false
    var iBNV = 0

    companion object {
        lateinit var oRV0: RecyclerView
        lateinit var oRV1: RecyclerView
        lateinit var oRV2: RecyclerView

        lateinit var mAdapter0: CartAdapter
        lateinit var mAdapter1: CartAdapter
        lateinit var mAdapter2: CartAdapter
        lateinit var hReload: Handler

        var dm = DisplayMetrics()
        val mCICreated = ArrayList<Boolean>()
        var orders: ArrayList<Order>? = null
        var orders0: ArrayList<Order>? = null
        var orders1: ArrayList<Order>? = null
        var orders2: ArrayList<Order>? = null
        var customers: ArrayList<Customer>? = null
        var drivers: ArrayList<Driver>? = null
        var mRV0LManager: LinearLayoutManager? = null
        var mRV1LManager: LinearLayoutManager? = null
        var mRV2LManager: LinearLayoutManager? = null

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

        fun arrangeSublists(c: Context, wc: WooCommerce, that: AppCompatActivity) {
            orders0 = ArrayList()
            orders1 = ArrayList()
            orders2 = ArrayList()
            for (m in orders!!) {
                if (m.stat == 2 || m.stat == 4 || m.stat == 5) orders2!!.add(m)
                else if (m.stat == 0 || m.stat == 1) orders1!!.add(m)
                else orders0!!.add(m)
            }
            Collections.sort(orders0, SortOrders())
            Collections.sort(orders1, SortOrders())
            Collections.sort(orders2, SortOrders())
            mAdapter0 = CartAdapter(c, orders0!!, wc, that)
            mAdapter1 = CartAdapter(c, orders1!!, wc, that)
            mAdapter2 = CartAdapter(c, orders2!!, wc, that)
            oRV0.adapter = mAdapter0
            oRV1.adapter = mAdapter1
            oRV2.adapter = mAdapter2
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.orders)

        oBody = findViewById(R.id.oBody)
        oMotor1 = findViewById(R.id.oMotor1)
        oToolbar = findViewById(R.id.oToolbar)
        oBack = findViewById(R.id.oBack)
        oReload = findViewById(R.id.oReload)
        oReloadIV = findViewById(R.id.oReloadIV)
        oTitle = findViewById(R.id.oTitle)
        oTitle1 = oTitle.getChildAt(0) as TextView
        oTitle2 = oTitle.getChildAt(1) as TextView
        oTitle3 = oTitle.getChildAt(2) as TextView
        oRV0 = findViewById(R.id.oRV0)
        oRV1 = findViewById(R.id.oRV1)
        oRV2 = findViewById(R.id.oRV2)
        oRVEmpty = findViewById(R.id.oRVEmpty)
        oBNV = findViewById(R.id.oBNV)

        c = applicationContext
        windowManager.defaultDisplay.getMetrics(dm)
        if (!Main.dirLtr) oBody.layoutDirection = View.LAYOUT_DIRECTION_RTL


        // Lists
        oBack.setOnClickListener { onBackPressed() }
        oRV0.setHasFixedSize(false)
        oRV1.setHasFixedSize(false)
        oRV2.setHasFixedSize(false)
        mRV0LManager = LinearLayoutManager(c)
        mRV1LManager = LinearLayoutManager(c)
        mRV2LManager = LinearLayoutManager(c)
        oRV0.layoutManager = mRV0LManager
        oRV1.layoutManager = mRV1LManager
        oRV2.layoutManager = mRV2LManager
        hReload = object : Handler() {
            override fun handleMessage(msg: Message?) {
                loadWC()
            }
        }
        if (!Main.dirLtr) oBack.rotationY = 180f

        // WooCommerce
        wc = WooCommerce.Builder()
            .setSiteUrl(resources.getString(R.string.site) + "index.php")
            .setApiVersion(WooCommerce.API_V3)
            .setConsumerKey(Main.conKey)
            .setConsumerSecret(Main.conSec)
            .build()
        oReload.setOnClickListener { loadWC() }
        loadWC()

        // BottomNavigationView
        oBNV.setOnNavigationItemSelectedListener {
            var ruturn = true
            when (it.itemId) {
                R.id.mBNVWaiting -> bnv(0)
                R.id.mBNVAssigned -> bnv(1)
                R.id.mBNVEnded -> bnv(2)
                else -> ruturn = false
            }
            return@setOnNavigationItemSelectedListener ruturn
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val anCon = Main.va(oMotor1, "translationX", Main.loadDur, 10f, 0f)
        anCon.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                windowManager.defaultDisplay.getMetrics(Main.dm)
            }
        })
    }


    fun loadWC() {
        if (gettingOrders || gettingCustomers || gettingDrivers) return
        whirlReload()
        wcLoaded = false
        orders = null
        customers = null
        drivers = null

        wcOrders()
        wcCustomers()
        getDrivers()
    }

    fun wcOrders() {
        gettingOrders = true
        wc.OrderRepository().orders().enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                gettingOrders = false
                orders = response.body() as ArrayList<Order>?
                if (orders != null && orders!!.size > 0)
                    for (o in 0 until orders!!.size) {
                        var ord = orders!![o]
                        ord.stat = statusToStat(ord.status)
                        orders!![o] = ord
                    }
                checkWC()
            }

            //Use this shitty "t" if any error occurred!
            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                gettingOrders = false
                checkWC()
            }
        })
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
                if (all != null) customers = all as ArrayList<Customer>?
                checkWC()
            }

            override fun onFailure(call: Call<List<Customer>>, t: Throwable) {
                gettingCustomers = false
                checkWC()
            }
        })
    }

    fun getDrivers() {
        if (liUser == null) return
        gettingDrivers = true
        drivers = ArrayList()
        val params = HashMap<String, String>()
        params["user"] = liUser!!
        man(c, "?action=drivers", com.android.volley.Response.Listener { res ->
            gettingDrivers = false
            if (res != null) if (res[0].toString() == "[") {
                if (res == "[]") drivers = ArrayList()
                else {
                    drivers =
                        Driver.readDrivers(ByteArrayInputStream(res.toByteArray(Charset.forName("UTF-8"))))
                    Collections.sort(drivers, SortDrivers())
                }
            }
            checkWC()
        }, com.android.volley.Response.ErrorListener {
            gettingDrivers = false
            checkWC()
        }, params, "drivers")
    }

    fun checkWC() {
        if (gettingOrders || gettingCustomers || gettingDrivers) return
        if (orders == null || customers == null || drivers == null) {
            if (!Main.isOnline(c)) Toast.makeText(c, R.string.noInternet, Toast.LENGTH_LONG).show()
            else {
                if (orders == null) wcOrders()
                if (customers == null) wcCustomers()
                if (drivers == null) getDrivers()
            }
        } else {
            if (orders!!.size > 0) {
                oRVEmpty.visibility = View.GONE
                oRVEmpty.alpha = 0f
                oRVEmpty.translationY = dp(-111).toFloat()

                mCICreated.clear()
                for (m in orders!!) mCICreated.add(false)
                arrangeSublists(c, wc, this)
                wcLoaded = true
            } else {
                var mRVEmptyDur: Long = 179
                Main.oa(oRVEmpty, "alpha", mRVEmptyDur, 1f)
                Main.oa(oRVEmpty, "translationY", mRVEmptyDur, 0f)
                oRVEmpty.visibility = View.VISIBLE
            }
        }
    }

    fun whirlReload() {
        var wr = Main.va(oReloadIV, "rotation", 522, 0f, 360f)
        wr.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (!wcLoaded) whirlReload()
            }
        })
    }

    fun bnv(i: Int) {
        if (iBNV == i) return
        iBNV = i
        var mRVs = arrayOf(oRV0, oRV1, oRV2)
        for (r in 0 until mRVs.size) mRVs[r].visibility = if (r == i) View.VISIBLE else View.GONE
    }


    class SortOrders : Comparator<Order> {
        override fun compare(a: Order, b: Order) = b.dateCreated.time.compareTo(a.dateCreated.time)
    }

    class SortDrivers : Comparator<Driver> {
        override fun compare(a: Driver, b: Driver) = a.name.compareTo(b.name)
    }
}
