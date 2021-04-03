package uk.easys.easydelivery

import android.animation.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.*
import kotlin.system.exitProcess

class Login : AppCompatActivity() {
    lateinit var lBody: ConstraintLayout
    lateinit var lMotor1: View
    lateinit var lLogoBox: ConstraintLayout
    lateinit var lTitle1: ImageView
    lateinit var lTitle2: ImageView
    lateinit var lLogoCover: View
    lateinit var lLogo: ImageView
    lateinit var lUser: EditText
    lateinit var lPass: EditText
    lateinit var lLogin: TextView

    lateinit var c: Context

    val supportedRtlLangs = listOf("ar")
    var firstBackToExit = false
    var pullUpDur: Long = 650
    var pulledUp = false

    companion object {
        const val loadDur: Long = 125
        const val exUser = "user"
        const val exPass = "pass"
        var dm = DisplayMetrics()
        var dirLtr = true

        lateinit var sp: SharedPreferences

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

        fun del(
            c: Context,
            get: String,
            lis: com.android.volley.Response.Listener<String>,
            err: com.android.volley.Response.ErrorListener?,
            hashMap: HashMap<String, String>
        ) {
            var req = Volley.newRequestQueue(c)
            var srt = object : StringRequest(
                Method.POST,
                c.resources.getString(R.string.delManager) + get, lis, err
            ) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String> {
                    return hashMap
                }
            }
            srt.tag = "delManager"
            req.add(srt)
        }

        fun compileTime(time: Long): String {
            val lm = Calendar.getInstance()
            lm.timeInMillis = time
            var y = lm.get(Calendar.YEAR)
            var m = lm.get(Calendar.MONTH)
            var d = lm.get(Calendar.DAY_OF_MONTH)
            var h = lm.get(Calendar.HOUR_OF_DAY)
            var n = lm.get(Calendar.MINUTE)
            return "$y." + (if (m < 10) "0" else "") + "$m." + (if (d < 10) "0" else "") + "$d - " +
                    (if (h < 10) "0" else "") + "$h:" + (if (n < 10) "0" else "") + "$n"
        }

        fun isOnline(c: Context): Boolean {
            val cm = c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            var nwi: NetworkInfo? = null
            if (cm != null) nwi = cm.activeNetworkInfo
            return (nwi != null && nwi.isConnected)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppThemeConverse)
        setContentView(R.layout.login)

        lBody = findViewById(R.id.lBody)
        lMotor1 = findViewById(R.id.lMotor1)
        lLogoBox = findViewById(R.id.lLogoBox)
        lTitle1 = findViewById(R.id.lTitle1)
        lTitle2 = findViewById(R.id.lTitle2)
        lLogoCover = findViewById(R.id.lLogoCover)
        lLogo = findViewById(R.id.lLogo)
        lUser = findViewById(R.id.lUser)
        lPass = findViewById(R.id.lPass)
        lLogin = findViewById(R.id.lLogin)

        c = applicationContext
        windowManager.defaultDisplay.getMetrics(dm)
        for (lang in supportedRtlLangs)
            if (Locale.getDefault().language == lang) {
                lBody.layoutDirection = View.LAYOUT_DIRECTION_RTL
                dirLtr = false
            }
        sp = PreferenceManager.getDefaultSharedPreferences(c)


        // Loading
        val anLoad1 = va(lMotor1, "translationX", loadDur, 10f, 0f, 1970)
        anLoad1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                var dest = lLogo.width * 0.78f
                var tvDest = (lTitle2.width - dest) + (dm.density * 13)
                val drop = AnimatorSet().setDuration(209)
                drop.playTogether(
                    ObjectAnimator.ofFloat(lLogo, "translationX", 0f - dest),
                    ObjectAnimator.ofFloat(lLogoCover, "translationX", 0f - dest),
                    ObjectAnimator.ofFloat(lTitle1, "translationX", tvDest),
                    ObjectAnimator.ofFloat(lTitle2, "translationX", tvDest)
                )
                drop.start()

                val kkk = va(lMotor1, "translationX", 1000, 10f, 0f, 1970)
                kkk.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        var online = isOnline(c)
                        if (sp.contains(exUser) && sp.contains(exPass) && online)
                            login(sp.getString(exUser, ""), sp.getString(exPass, ""))
                        else {
                            if (!online) Toast.makeText(
                                c,
                                R.string.noInternet,
                                Toast.LENGTH_LONG
                            ).show()
                            pullUp()
                        }
                    }
                })
            }
        })

        // Log In
        lLogin.setOnClickListener {
            if (lUser.text.toString() == "" || lPass.text.toString() == "") return@setOnClickListener
            login(lUser.text.toString(), lPass.text.toString())
        }
        if (sp.contains(exUser)) lUser.setText(sp.getString(exUser, ""))
        if (sp.contains(exPass)) lPass.setText(sp.getString(exPass, ""))
    }

    override fun onBackPressed() {
        if (!firstBackToExit) {
            firstBackToExit = true
            Toast.makeText(c, R.string.toExit, Toast.LENGTH_LONG).show()
            val exit = va(lMotor1, "translationX", 4000, 4f, 0f)
            exit.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    firstBackToExit = false
                }
            })
        } else {
            moveTaskToBack(true)
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val anCon = va(lMotor1, "translationX", loadDur, 10f, 0f)
        anCon.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                windowManager.defaultDisplay.getMetrics(dm)
            }
        })
    }


    fun pullUp() {
        pulledUp = true
        val scale = 0.79f
        oa(lLogoBox, "scaleX", pullUpDur, scale)
        oa(lLogoBox, "scaleY", pullUpDur, scale)

        lUser.translationY = dm.heightPixels * 0.75f
        lPass.translationY = lUser.translationY
        lLogin.translationY = lUser.translationY
        lUser.visibility = View.VISIBLE
        lPass.visibility = View.VISIBLE
        lLogin.visibility = View.VISIBLE
        var dataHeight = lLogoBox.height + mar(lUser, "t") + lUser.height +
                mar(lPass, "t") + lPass.height + mar(lLogin, "t") + lLogin.height
        var expected = (dm.heightPixels - dataHeight) / 2f
        var theirY = (dm.heightPixels - lLogoBox.height) / 2f
        var t = (expected - theirY) - (dataHeight / 2.5f)//!?!?!?
        oa(lLogoBox, "translationY", pullUpDur, t)
        oa(lUser, "translationY", pullUpDur, t)
        oa(lPass, "translationY", pullUpDur, t, 78)
        oa(lLogin, "translationY", pullUpDur, t, 156)
    }

    fun mar(v: View, which: String): Int {
        var mlp = v.layoutParams as ViewGroup.MarginLayoutParams
        return when (which) {
            "t" -> mlp.topMargin
            "l" -> mlp.leftMargin
            "r" -> mlp.rightMargin
            "b" -> mlp.bottomMargin
            "s" -> mlp.marginStart
            "e" -> mlp.marginEnd
            else -> 0
        }
    }

    fun login(user: String, pass: String) {
        val params = HashMap<String, String>()
        params["user"] = user
        params["pass"] = pass
        del(c, "?action=login", com.android.volley.Response.Listener { res ->
            if (res == "loggedIn") {
                var spe = sp.edit()
                spe.putString(exUser, user)
                spe.putString(exPass, pass)
                spe.apply()

                var intent = Intent(c, Delivery::class.java)
                intent.putExtra(exUser, user)
                intent.putExtra(exPass, pass)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(c, res, Toast.LENGTH_LONG).show()
                if (!pulledUp) pullUp()
            }
        }, com.android.volley.Response.ErrorListener {
            Toast.makeText(c, R.string.anError, Toast.LENGTH_LONG).show()
            if (!pulledUp) pullUp()
        }, params)
    }
}
