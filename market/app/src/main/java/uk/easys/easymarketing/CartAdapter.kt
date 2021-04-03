package uk.easys.easymarketing

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
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
import uk.easys.easymarketing.woo.WooCommerce
import uk.easys.easymarketing.woo.models.Order
import java.util.*
import kotlin.collections.ArrayList

class CartAdapter(
    val c: Context,
    val data: ArrayList<Order>,
    val wc: WooCommerce,
    val that: AppCompatActivity
) : RecyclerView.Adapter<CartAdapter.MyViewHolder>() {

    class MyViewHolder(val ll: LinearLayout) : RecyclerView.ViewHolder(ll)

    override fun onCreateViewHolder(parent: ViewGroup, ciewType: Int): MyViewHolder {
        var v = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item, parent, false) as LinearLayout
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        if (!Main.mCICreated[i]) {
            val ass = AnimatorSet().setDuration(400)
            ass.startDelay = (i * 60).toLong()
            var hllTY = ObjectAnimator.ofFloat(h.ll, "translationY", Main.dp(-100).toFloat(), 0f)
            hllTY.startDelay = ass.duration / 4
            ass.playTogether(ObjectAnimator.ofFloat(h.ll, "alpha", 1f), hllTY)
            ass.start()
            Main.mCICreated[i] = true
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
            Main.menu(c, it, wc, data[i], that)
            return@setOnLongClickListener true
        }
        (ll1.getChildAt(0) as TextView).text = (i + 1).toString()// + "."
        var td1 = ll1.getChildAt(1) as LinearLayout
        (td1.getChildAt(0) as TextView).text = Main.compileTime(data[i].dateCreated.time)
        (td1.getChildAt(1) as TextView).text =
            c.resources.getString(R.string.mCIID) + data[i].id
        (ll1.getChildAt(2) as TextView).text =
            Main.fixPrice(data[i].total) + " " + Main.currency(c, data[i].currency)
        val status = ll1.getChildAt(3) as ConstraintLayout
        val statIV = status.getChildAt(0) as ImageView
        statIV.id = View.generateViewId()
        statIcon(c, data[i], statIV, status, that)

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

    fun sort(by: Int = 0, asc: Boolean = false) {
        Main.sortBy = by
        Main.ascending = asc
        var ed = Main.sp.edit()
        ed.putInt(Main.exSortBy, Main.sortBy)
        ed.putBoolean(Main.exAscending, Main.ascending)
        ed.apply()
        Collections.sort(data, Main.SortOrders())
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

        fun pulse(c: Context, parent: ConstraintLayout?) {
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
        }

        fun statIcon(
            c: Context,
            ord: Order,
            statIV: ImageView,
            status: ConstraintLayout,
            that: AppCompatActivity
        ) {
            statIV.colorFilter = null
            statIV.alpha = 1f
            var statRes: Int
            when (ord.stat) {
                0 -> {// Out For Delivery
                    statRes = R.drawable.marker_intent_2
                    status.setOnClickListener {
                        var intent = Intent(c, Maps::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra(Main.orderID, ord.id)
                        c.startActivity(intent)
                    }
                    statIV.alpha = Main.kiriAlpha
                    pulse(c, status)
                }
                1 -> {// Driver Assigned
                    statRes = R.drawable.marker_intent_2
                    statIV.setColorFilter(ContextCompat.getColor(c, R.color.mCartItemGoDis))
                    status.setOnClickListener {
                        statMsg(that, c.resources.getString(R.string.mStatDeliveryAssigned))
                    }
                }
                2 -> {// Failed
                    statRes = R.drawable.ord_failed_1
                    status.setOnClickListener {
                        statMsg(that, c.resources.getString(R.string.mStatFailed))
                    }
                }
                4 -> {// Completed
                    statRes = R.drawable.ord_completed_1
                    status.setOnClickListener {
                        statMsg(that, c.resources.getString(R.string.mStatCompleted))
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
                    var msg = when (ord.status) {
                        "pending" -> c.resources.getString(R.string.mStatPending)
                        "processing" -> c.resources.getString(R.string.mStatProcessing)
                        "order-returned" -> c.resources.getString(R.string.mStatOrderReturned)
                        "on-hold" -> c.resources.getString(R.string.mStatOnHold)
                        "refunded" -> c.resources.getString(R.string.mStatRefunded)
                        else -> ord.status
                    }
                    status.setOnClickListener { statMsg(that, msg) }
                }
            }
            if (statRes != -1) statIV.setImageResource(statRes)
        }
    }
}