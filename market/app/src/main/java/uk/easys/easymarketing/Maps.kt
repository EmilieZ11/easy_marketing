package uk.easys.easymarketing

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class Maps : AppCompatActivity() {
    lateinit var pBody: ConstraintLayout
    lateinit var pMotor1: View
    lateinit var pToolbar: Toolbar
    lateinit var pMap: SupportMapFragment

    lateinit var c: Context
    lateinit var map: GoogleMap
    lateinit var pTBTitle: TextView
    lateinit var pTBSubtitle: TextView
    lateinit var delivery: Marker
    lateinit var customer: Marker
    lateinit var coDelivery: LatLng
    lateinit var coCustomer: LatLng

    var dm = DisplayMetrics()
    val interval: Long = 15000
    var id = -1
    var call: String? = null
    var notFoundCall = false
    var backPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps)

        pBody = findViewById(R.id.pBody)
        pMotor1 = findViewById(R.id.pMotor1)
        pToolbar = findViewById(R.id.pToolbar)
        pMap = supportFragmentManager.findFragmentById(R.id.pMap) as SupportMapFragment

        c = applicationContext
        windowManager.defaultDisplay.getMetrics(dm)
        if (intent.extras != null && intent.extras.containsKey(Main.orderID)) {
            id = intent.extras.getInt(Main.orderID)
            check(id.toString())
            call(id.toString())
        } else {
            Toast.makeText(c, R.string.incomInfo, Toast.LENGTH_LONG).show()
            onBackPressed()
        }
        if (!Main.dirLtr) pBody.layoutDirection = View.LAYOUT_DIRECTION_RTL


        // Toolbar
        setSupportActionBar(pToolbar)
        for (g in 0 until pToolbar.childCount) {
            var getTitle = pToolbar.getChildAt(g)
            if (getTitle.javaClass.name.equals("androidx.appcompat.widget.AppCompatTextView", true))
                when ((getTitle as TextView).text.toString()) {
                    resources.getString(R.string.pTBTitle) -> pTBTitle = getTitle
                    resources.getString(R.string.pTBSubtitle) -> pTBSubtitle = getTitle
                }
        }
        pTBTitle.typeface = Typeface.DEFAULT_BOLD
        //pTBTitle.typeface = tfLalezar
        //pTBTitle.textSize = dm.density * 15
        //pTBSubtitle.typeface = Typeface.SANS_SERIF
        //pTBSubtitle.textSize = dm.density * 15
        pToolbar.setNavigationOnClickListener { onBackPressed() }
        if (!Main.dirLtr) pToolbar.navigationIcon = BitmapDrawable(
            resources,
            Main.rtlBmp((pToolbar.navigationIcon as BitmapDrawable).bitmap)
        )

        // Map
        pMap.getMapAsync {
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
            if (hour < sun[0] || hour >= sun[1]) theme = R.raw.night
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(c, theme))
            map.setMinZoomPreference(6f)

            coCustomer = LatLng(29.375737, 47.967231)
            customer =
                map.addMarker(MarkerOptions().position(coCustomer).title(resources.getString(R.string.coCustomer)))
            customer.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_2_customer))
            zoom()

            map.isMyLocationEnabled = true
        }
    }


    override fun onBackPressed() {
        backPressed = true
        try {//Sometimes an unknown error occurs when an activity with a map tries to close itself.
            super.onBackPressed()
        } catch (ignored: Exception) {
        }
        finish()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val anCon = Main.va(pMotor1, "translationX", Main.loadDur, 10f, 0f)
        anCon.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                windowManager.defaultDisplay.getMetrics(dm)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.maps, menu)
        return super.onCreateOptionsMenu(menu)//DON"T PUT HERE THINGS THAT NEED THE LAYOUT LOADED.
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.pCall -> {
                if (call == null) {
                    if (notFoundCall) Toast.makeText(
                        c,
                        R.string.notFoundCall,
                        Toast.LENGTH_LONG
                    ).show()
                    else Toast.makeText(c, R.string.cannotCall, Toast.LENGTH_LONG).show()
                    call(id.toString())
                } else try {
                    var goCall = Intent(Intent.ACTION_DIAL)
                    goCall.data = Uri.parse("tel:$call")
                    startActivity(goCall)
                } catch (ignored: Exception) {
                    Toast.makeText(c, R.string.unableToCall, Toast.LENGTH_LONG).show()
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }


    fun zoom() {
        if (::delivery.isInitialized && ::customer.isInitialized) {
            var builder = LatLngBounds.Builder()
            builder.include(delivery.position)
            builder.include(customer.position)
            var bounds = builder.build()
            var pad = (dm.widthPixels * 0.10).toInt()// offset from edges of the map 10% of screen
            var cu =
                CameraUpdateFactory.newLatLngBounds(bounds, dm.widthPixels, dm.heightPixels, pad)
            map.animateCamera(cu)
        } else {
            if (::delivery.isInitialized) map.moveCamera(CameraUpdateFactory.newLatLng(coDelivery))
            if (::customer.isInitialized) map.moveCamera(CameraUpdateFactory.newLatLng(coCustomer))
        }
    }

    fun del(
        lis: com.android.volley.Response.Listener<String>,
        err: com.android.volley.Response.ErrorListener?,
        hashMap: HashMap<String, String>,
        tag: String
    ) {
        var req = Volley.newRequestQueue(c)
        var srt = object :
            StringRequest(Method.POST, resources.getString(R.string.delManager), lis, err) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                return hashMap
            }
        }
        srt.tag = tag
        req.add(srt)
    }

    fun check(id: String) {
        val params = HashMap<String, String>()
        params["findDelivery"] = id
        del(com.android.volley.Response.Listener { res ->
            if (res == null || res == "") return@Listener
            if (res == "notAssigned") {
                Toast.makeText(c, R.string.notAssigned, Toast.LENGTH_LONG).show()
                onBackPressed()
                return@Listener
            }
            if (res.length > 5 && res.substring(0, 5) == "aaaaa") {
                val split = res.substring(5).split(":")
                try {
                    coDelivery = LatLng(split[0].toDouble(), split[1].toDouble())
                } catch (ignored: Exception) {
                    //....
                }
                if (!(::map.isInitialized)) return@Listener
                if (::delivery.isInitialized) delivery.position = coDelivery
                else {
                    delivery = map.addMarker(MarkerOptions().apply {
                        position(coDelivery)
                        title(resources.getString(R.string.coDelivery))
                    })
                    delivery.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_2_base))
                    zoom()
                }
                try {
                    delivery.snippet = Main.compileTime(split[2].toLong())
                } catch (ignored: Exception) {
                    delivery.snippet = null// CHECK THIS..............
                }
            }
        }, com.android.volley.Response.ErrorListener {
        }, params, "delManager")

        if (!backPressed) object : CountDownTimer(interval, interval / 2) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                check(id)
            }
        }.start()
    }

    fun call(id: String) {
        val params = HashMap<String, String>()
        params["call"] = id
        del(com.android.volley.Response.Listener { res ->
            if (res == null || res == "") return@Listener
            if (res.length > 5 && res.substring(0, 5) == "aaaaa") call = res.substring(5)
            else if (res == "not found") notFoundCall = true
        }, com.android.volley.Response.ErrorListener {
        }, params, "call")
    }
}
