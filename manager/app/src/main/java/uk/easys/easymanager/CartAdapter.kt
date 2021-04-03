package uk.easys.easymanager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.easys.easymanager.woo.WooCommerce
import uk.easys.easymanager.woo.models.Customer
import uk.easys.easymanager.woo.models.Order
import java.util.*
import kotlin.collections.ArrayList

class CartAdapter(
    val c: Context,
    val data: ArrayList<Order>,
    val wc: WooCommerce,
    val that: AppCompatActivity
) : RecyclerView.Adapter<CartAdapter.MyViewHolder>() {
    var changingStat = false

    class MyViewHolder(val ll: LinearLayout) : RecyclerView.ViewHolder(ll)

    override fun onCreateViewHolder(parent: ViewGroup, ciewType: Int): MyViewHolder {
        var v = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item, parent, false) as LinearLayout
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        if (!Orders.mCICreated[i]) {
            val ass = AnimatorSet().setDuration(400)
            ass.startDelay = (i * 60).toLong()
            var hllTY = ObjectAnimator.ofFloat(h.ll, "translationY", Main.dp(-100).toFloat(), 0f)
            hllTY.startDelay = ass.duration / 4
            ass.playTogether(ObjectAnimator.ofFloat(h.ll, "alpha", 1f), hllTY)
            ass.start()
            Orders.mCICreated[i] = true
        } else {
            h.ll.translationY = 0f
            h.ll.alpha = 1f
        }

        val ll1 = h.ll.getChildAt(0) as LinearLayout
        val ll2 = h.ll.getChildAt(2) as ConstraintLayout
        val list = ll2.getChildAt(0) as LinearLayout
        ll1.setOnClickListener {
            if (list.childCount == 0) return@setOnClickListener
            if (ll2.visibility == View.VISIBLE) ll2.visibility = View.GONE
            else {
                list.alpha = 0f
                ll2.scaleY = 0f
                ll2.visibility = View.VISIBLE
                var scroll = Main.oa(ll2, "scaleY", 222, 1f)
                scroll.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        Main.oa(list, "alpha", 92, 1f)
                    }
                })
            }
        }
        ll1.setOnLongClickListener {
            //Main.menu(c, it, wc, data[i], that)
            return@setOnLongClickListener true
        }

        var tv1 = ll1.getChildAt(0) as TextView
        var cus = c.resources.getString(R.string.unknown)
        if (data[i].customerId != null) {
            var cust: Customer? = Main.findCusById(data[i].customerId!!, Orders.customers)
            if (cust != null) cus = "${cust.firstName} ${cust.lastName}"
        }
        tv1.text = "${i + 1}. $cus"
        tv1.textDirection =
            if (Main.dirLtr) TextView.TEXT_DIRECTION_LTR else TextView.TEXT_DIRECTION_RTL
        var td1 = ll1.getChildAt(1) as LinearLayout
        (td1.getChildAt(0) as TextView).text = Main.compileTime(data[i].dateCreated.time)
        (td1.getChildAt(1) as TextView).text = c.resources.getString(R.string.mCIID) + data[i].id
        val more = ll1.getChildAt(2) as ConstraintLayout
        more.id = View.generateViewId()

        if (data[i].stat == 2 || data[i].stat == 4 || data[i].stat == 5) {
            var statIV = ImageView(c)
            var statIVLP = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            statIVLP.leftToLeft = more.id
            statIVLP.rightToRight = more.id
            statIVLP.topMargin = Main.dp(18)
            statIVLP.bottomMargin = Main.dp(18)
            statIV.adjustViewBounds = true
            if (more.childCount == 0) more.addView(statIV, statIVLP)

            more.background = ContextCompat.getDrawable(c, R.drawable.hover_tp_to_cp_weak_1)
            statIcon(c, data[i], statIV, more, that, Main.findDrvByOrd(data[i], Orders.drivers))
        } else if (more.childCount == 0) {
            var spinner = Spinner(c)// styling didn't work!
            var spinnerLP = ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
            spinnerLP.topToTop = more.id
            spinnerLP.leftToLeft = more.id
            spinnerLP.rightToRight = more.id
            spinnerLP.bottomToBottom = more.id
            spinnerLP.matchConstraintPercentWidth = 0.92f
            spinner.background = ContextCompat.getDrawable(c, R.drawable.spinner_1_bg)
            spinner.id = View.generateViewId()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) spinner.elevation = 2f
            more.addView(spinner, spinnerLP)
            var spnClicked = false
            spinner.setOnTouchListener { view, motionEvent ->
                spnClicked = true
                return@setOnTouchListener changingStat
            }

            var drvNames = ArrayList<String>()
            drvNames.add("")
            for (d in Orders.drivers!!) drvNames.add(d.name)
            var dataAdapter = ArrayAdapter<String>(c, R.layout.spinner_1, drvNames)
            dataAdapter.setDropDownViewResource(R.layout.spinner_1_dd)
            spinner.adapter = dataAdapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    pos: Int,
                    id: Long
                ) {
                    if (!spnClicked) return
                    changingStat = true
                    val params = HashMap<String, String>()
                    params["user"] = Main.liUser!!
                    params["set_driver"] = "${data[i].id}"
                    params["new_driver"] = if (pos > 0) "${Orders.drivers!![pos - 1].id}" else "-1"
                    Main.man(c, "?action=set_driver", com.android.volley.Response.Listener { res ->
                        if (res != "done") {
                            changingStat = false
                            return@Listener
                        }
                        var lastDriver = Main.findDrvByOrd(data[i], Orders.drivers)
                        if (lastDriver != null) {
                            var lastOrds = lastDriver.ords
                            lastOrds.remove(data[i].id)
                            Orders.drivers!![Orders.drivers!!.indexOf(lastDriver)].ords = lastOrds
                        }
                        if (pos > 0) {
                            var newOrds = Orders.drivers!![pos - 1].ords
                            newOrds.add(data[i].id)
                            Orders.drivers!![Orders.drivers!!.indexOf(Orders.drivers!![pos - 1])].ords =
                                newOrds
                        }

                        var doChange = ((data[i].stat == 0 || data[i].stat == 1) && pos == 0) ||
                                ((data[i].stat != 0 && data[i].stat != 1) && pos > 0)
                        if (!doChange) {
                            changingStat = false
                            return@Listener
                        }
                        var fixed = data[i]
                        fixed.status = if (pos > 0) "driver-assigned" else "processing"
                        wc.OrderRepository().update(data[i].id, fixed)
                            .enqueue(object : Callback<Order> {
                                override fun onResponse(
                                    call: Call<Order>,
                                    response: Response<Order>
                                ) {
                                    if (!response.isSuccessful) {
                                        changingStat = false
                                        return
                                    }
                                    val order = response.body()!!
                                    order.stat = Orders.statusToStat(fixed.status)
                                    Orders.orders!!.set(Orders.orders!!.indexOf(data[i]), order)
                                    Orders.arrangeSublists(c, wc, that)
                                    changingStat = false
                                }

                                override fun onFailure(call: Call<Order>, t: Throwable) {
                                    changingStat = false
                                }
                            })
                    }, com.android.volley.Response.ErrorListener {
                        changingStat = false
                    }, params, "set_driver${data[i].id}")
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }
            var driver = Main.findDrvByOrd(data[i], Orders.drivers)
            spinner.setSelection(if (driver != null) Orders.drivers!!.indexOf(driver) + 1 else 0)

            var mark = ImageView(ContextThemeWrapper(c, R.style.mCISpnMark), null, 0)
            var markLP = ConstraintLayout.LayoutParams(Main.dp(30), Main.dp(30))
            markLP.endToEnd = spinner.id
            markLP.topToTop = spinner.id
            markLP.bottomToBottom = spinner.id
            mark.setColorFilter(ContextCompat.getColor(c, R.color.mCISpnMark))
            more.addView(mark, markLP)
        }


        val lineItems = data[i].lineItems
        list.removeAllViews()
        for (l in lineItems) {
            var item = LinearLayout(c)
            var itemLP = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            item.orientation = LinearLayout.HORIZONTAL
            item.weightSum = 10f
            list.addView(item, itemLP)

            var li0 = TextView(ContextThemeWrapper(c, R.style.mCILITV), null, 0)
            var li0LP = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            li0.text = "${lineItems.indexOf(l) + 1}. "
            item.addView(li0, li0LP)

            var li1 = TextView(ContextThemeWrapper(c, R.style.mCILITV), null, 0)
            var li1LP = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 6f)
            li1.text = l.name
            if (!Main.dirLtr) li1.textDirection = TextView.TEXT_DIRECTION_RTL
            item.addView(li1, li1LP)

            var li2 = TextView(ContextThemeWrapper(c, R.style.mCILITV), null, 0)
            var li2LP = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            li2.text = "x${l.quantity}"
            if (!Main.dirLtr) li2.textDirection = TextView.TEXT_DIRECTION_RTL
            item.addView(li2, li2LP)

            var li3 = TextView(ContextThemeWrapper(c, R.style.mCILITV), null, 0)
            var li3LP = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f)
            li3.text = Main.fixPrice(l.price) + " " + Main.currency(c, data[i].currency)
            if (!Main.dirLtr) li3.textDirection = TextView.TEXT_DIRECTION_RTL
            item.addView(li3, li3LP)
        }
    }

    override fun getItemCount() = data.size

    fun clear() {
        var size = data.size
        data.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun sort() {
        Collections.sort(data, Orders.SortOrders())
        notifyDataSetChanged()
    }

    companion object {
        fun statMsg(that: AppCompatActivity, msg: String) {
            val bull = AlertDialog.Builder(that).apply {
                setIcon(R.drawable.easy_logo_2_tiny)
                setTitle(R.string.mOrderStat)
                setMessage(msg)
                setNeutralButton(R.string.ok) { _, _ -> }
            }
            bull.create().show()
        }

        /*fun pulse(c: Context, parent: ConstraintLayout?) {
            if (parent == null) return
            var iv: ImageView? = parent.getChildAt(0) as ImageView? ?: return
            if (iv!!.alpha != Main.kiriAlpha) return
            var decresis = AnimatorSet().setDuration(920)
            decresis.startDelay = 1110
            var deScale = 0.69f
            decresis.playTogether(
                ObjectAnimator.ofFloat(iv, "scaleX", deScale),
                ObjectAnimator.ofFloat(iv, "scaleY", deScale)
            )
            decresis.start()
            decresis.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (iv.alpha != Main.kiriAlpha) {
                        iv.scaleX = 1f
                        iv.scaleY = 1f
                        return
                    }
                    var incresis = AnimatorSet().setDuration(22)
                    incresis.playTogether(
                        ObjectAnimator.ofFloat(iv, "scaleX", 1f),
                        ObjectAnimator.ofFloat(iv, "scaleY", 1f)
                    )
                    incresis.start()
                    incresis.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            if (iv.alpha != Main.kiriAlpha) return
                            pulse(c, parent)

                            var pulsum = ImageView(c)
                            var pulsumLP = ConstraintLayout.LayoutParams(0, 0)
                            pulsumLP.topToTop = iv.id
                            pulsumLP.bottomToBottom = iv.id
                            pulsumLP.leftToLeft = iv.id
                            pulsumLP.rightToRight = iv.id
                            pulsum.setImageResource(R.drawable.marker_intent_2)
                            pulsum.alpha = 0.65f
                            parent.addView(pulsum, pulsumLP)

                            var anPul = AnimatorSet().setDuration(1970)
                            var pulScale = 3f
                            anPul.playTogether(
                                ObjectAnimator.ofFloat(pulsum, "scaleX", pulScale),
                                ObjectAnimator.ofFloat(pulsum, "scaleY", pulScale),
                                ObjectAnimator.ofFloat(pulsum, "alpha", 0f)
                            )
                            anPul.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationStart(animation: Animator?) {
                                    pulsum.pivotX = iv.width / 2f
                                    pulsum.pivotY = (iv.height / 100f) * 40f
                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    parent.removeView(pulsum)
                                }
                            })
                            anPul.start()
                        }
                    })
                }
            })
        }*/

        fun statIcon(
            c: Context,
            ord: Order,
            statIV: ImageView,
            status: ConstraintLayout,
            that: AppCompatActivity,
            driver: Driver?
        ) {
            //statIV.colorFilter = null;statIV.alpha = 1f
            var statRes = -1
            when (ord.stat) {
                0 -> {// Out for Delivery: Impossible
                }
                1 -> {// Driver Assigned: Impossible
                }
                2 -> {// Failed
                    statRes = R.drawable.ord_failed_1
                    status.setOnClickListener {
                        statMsg(
                            that, c.resources.getString(R.string.mStatFailed) +
                                    (driver?.name ?: c.resources.getString(R.string.unknown)) + "."
                        )
                    }
                }
                4 -> {// Completed
                    statRes = R.drawable.ord_completed_1
                    status.setOnClickListener {
                        statMsg(
                            that, c.resources.getString(R.string.mStatCompleted) +
                                    (driver?.name ?: c.resources.getString(R.string.unknown)) + "."
                        )
                    }
                }
                5 -> {// Cancelled
                    statRes = R.drawable.ord_cancelled_1
                    status.setOnClickListener {
                        statMsg(that, c.resources.getString(R.string.mStatCancelled))
                    }
                }
                else -> {// Other
                    statRes = R.drawable.ord_processing_1
                    status.setOnClickListener {
                        statMsg(
                            that, when (ord.status) {
                                "pending" -> c.resources.getString(R.string.mStatPending)
                                "processing" -> c.resources.getString(R.string.mStatProcessing)
                                "order-returned" -> c.resources.getString(R.string.mStatOrderReturned)
                                "on-hold" -> c.resources.getString(R.string.mStatOnHold)
                                "refunded" -> c.resources.getString(R.string.mStatRefunded)
                                else -> ord.status
                            }
                        )
                    }
                }
            }
            if (statRes != -1) statIV.setImageResource(statRes)
        }
    }
}