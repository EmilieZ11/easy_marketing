package uk.easysy.easydelivery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import uk.easys.easydelivery.Delivery
import uk.easys.easydelivery.Login
import uk.easys.easydelivery.R
import uk.easys.easydelivery.woo.WooCommerce
import uk.easys.easydelivery.woo.models.Customer
import uk.easys.easydelivery.woo.models.Order
import java.util.ArrayList

class OrderAdapter(
    private val c: Context,
    private val data: ArrayList<Order>,
    private val wc: WooCommerce,
    private val that: AppCompatActivity
) :
    RecyclerView.Adapter<OrderAdapter.MyViewHolder>() {

    class MyViewHolder(val ll: LinearLayout) : RecyclerView.ViewHolder(ll)

    override fun onCreateViewHolder(parent: ViewGroup, ciewType: Int): MyViewHolder {
        var v = LayoutInflater.from(parent.context)
            .inflate(R.layout.order, parent, false) as LinearLayout
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        if (!Delivery.dOrdCreated[i]) {
            val ass = AnimatorSet().setDuration(400)
            ass.startDelay = (i * 60).toLong()
            var hllTY =
                ObjectAnimator.ofFloat(h.ll, "translationY", Login.dp(-100).toFloat(), 0f)
            hllTY.startDelay = ass.duration / 4
            ass.playTogether(ObjectAnimator.ofFloat(h.ll, "alpha", 1f), hllTY)
            ass.start()
            Delivery.dOrdCreated[i] = true
        } else {
            h.ll.translationY = 0f
            h.ll.alpha = 1f
        }

        val ll1 = h.ll.getChildAt(0) as ConstraintLayout
        val ll2 = h.ll.getChildAt(2) as ConstraintLayout
        val list = ll2.getChildAt(0) as LinearLayout
        ll1.setOnClickListener { Delivery.select(c, i) }

        val tv1 = ll1.getChildAt(0) as TextView
        var cus = c.resources.getString(R.string.unknown)
        if (data[i].customerId != null) {
            var cust: Customer? = Delivery.findCusById(data[i].customerId!!, Delivery.customers)
            if (cust != null) cus = "${cust.firstName} ${cust.lastName}"
        }
        tv1.text = "${i + 1}. ${cus} (${Delivery.fixPrice(data[i].total)} " +
                "${Delivery.currency(c, data[i].currency)})"
        if (!Login.dirLtr) tv1.textDirection = TextView.TEXT_DIRECTION_RTL
        val statIV = ll1.getChildAt(1) as ImageView
        statIV.id = View.generateViewId()
        statIcon(c, data[i], statIV, ll1, that)
        statIV.setOnClickListener { Delivery.showChangeStat(true, data[i].id) }
        val desc1 = ll1.getChildAt(2) as TextView
        desc1.text = "#${data[i].id}"
        val desc2 = ll1.getChildAt(3) as TextView
        desc2.text = Login.compileTime(data[i].dateCreated.time)


        val lineItems = data[i].lineItems
        list.removeAllViews()
        for (l in lineItems) {
            var item = LinearLayout(c)
            var itemLP = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            item.orientation = LinearLayout.HORIZONTAL
            item.weightSum = 3f
            item.setPaddingRelative(Login.dp(10), 0, Login.dp(10), 0)
            list.addView(item, itemLP)

            var li0 = TextView(ContextThemeWrapper(c, R.style.mCILITV), null, 0)
            var li0LP = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f)
            li0.text = "${lineItems.indexOf(l) + 1}. ${l.name} (x${l.quantity})"
            item.addView(li0, li0LP)

            var li1 = TextView(ContextThemeWrapper(c, R.style.mCILITV), null, 0)
            var li1LP = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            li1.text = "${Delivery.fixPrice(l.price)} ${Delivery.currency(c, data[i].currency)}"
            item.addView(li1, li1LP)
        }
    }

    override fun getItemCount() = data.size

    fun clear() {
        var size = data.size
        data.clear()
        notifyItemRangeRemoved(0, size)
    }

    companion object {
        fun statMsg(msg: String, that: AppCompatActivity) {
            val bull = AlertDialog.Builder(that).apply {
                setIcon(R.drawable.easy_logo_2_tiny)
                setTitle(R.string.mOrderStat)
                setMessage(msg)
                setNeutralButton(R.string.ok) { dialog, id -> }
            }
            bull.create().show()
        }

        fun pulse(c: Context, parent: ConstraintLayout?) {
            if (parent == null) return
            var iv = parent.getChildAt(1) as ImageView
            if (iv.colorFilter != null || iv.alpha == Delivery.nonMotorAlpha) return
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
                    var incresis = AnimatorSet().setDuration(22)
                    incresis.playTogether(
                        ObjectAnimator.ofFloat(iv, "scaleX", 1f),
                        ObjectAnimator.ofFloat(iv, "scaleY", 1f)
                    )
                    incresis.start()
                    incresis.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            if (parent != null) pulse(c, parent)//What an ass!

                            var pulsum = ImageView(c)
                            var pulsumLP = ConstraintLayout.LayoutParams(0, 0)
                            pulsumLP.topToTop = iv.id
                            pulsumLP.bottomToBottom = iv.id
                            pulsumLP.leftToLeft = iv.id
                            pulsumLP.rightToRight = iv.id
                            pulsum.setPaddingRelative(
                                iv.paddingStart, iv.paddingTop, iv.paddingEnd, iv.paddingBottom
                            )
                            pulsum.setImageResource(R.drawable.motorcycle_1)
                            pulsum.alpha = 0.65f
                            parent.addView(pulsum, pulsumLP)

                            var anPul = AnimatorSet().setDuration(1970)
                            var pulScale = 2f
                            anPul.playTogether(
                                ObjectAnimator.ofFloat(pulsum, "scaleX", pulScale),
                                ObjectAnimator.ofFloat(pulsum, "scaleY", pulScale),
                                ObjectAnimator.ofFloat(pulsum, "alpha", 0f)
                            )
                            anPul.addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    parent.removeView(pulsum)
                                }
                            })
                            anPul.start()
                        }
                    })
                }
            })
        }

        fun statIcon(
            c: Context, ord: Order, statIV: ImageView, ll1: ConstraintLayout,
            that: AppCompatActivity
        ) {
            statIV.alpha = if (ord.stat == 0 || ord.stat == 1) 1f else Delivery.nonMotorAlpha
            when (ord.stat) {
                0 -> {// Out For Delivery
                    statIV.setImageResource(R.drawable.motorcycle_1)
                    if (statIV.colorFilter != null) statIV.clearColorFilter()
                    pulse(c, ll1)
                }
                1 -> {// Driver Assigned
                    statIV.setImageResource(R.drawable.motorcycle_1)
                    statIV.setColorFilter(ContextCompat.getColor(c, R.color.dRVStatDis))
                }
                2 -> statIV.setImageResource(R.drawable.ord_failed_1)// Failed
                4 -> statIV.setImageResource(R.drawable.ord_completed_1)// Completed
                5 -> statIV.setImageResource(R.drawable.ord_cancelled_1)// Cancelled
                else -> {// Other
                    statIV.setImageResource(R.drawable.ord_processing_1)
                    statIV.setOnClickListener { statMsg(ord.status, that) }
                }
            }
        }
    }
}