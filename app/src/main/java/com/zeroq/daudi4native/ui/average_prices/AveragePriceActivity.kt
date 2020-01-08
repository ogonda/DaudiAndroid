package com.zeroq.daudi4native.ui.average_prices

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.commons.BaseActivity
import com.zeroq.daudi4native.data.models.AveragePriceModel
import com.zeroq.daudi4native.data.models.OmcModel
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.ui.dialogs.AverageDialogFragment
import com.zeroq.daudi4native.ui.dialogs.data.AverageDialogEvent
import kotlinx.android.synthetic.main.activity_average_price.*
import kotlinx.android.synthetic.main.ago_average_card.*
import kotlinx.android.synthetic.main.ik_average_card.*
import kotlinx.android.synthetic.main.pms_average_card.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.toast
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class AveragePriceActivity : BaseActivity() {


    lateinit var viewModel: AverageViewModel
    private var omcs: List<OmcModel>? = null
    private var userModel: UserModel? = null

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override

    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_average_price)

        viewModel = getViewModel(AverageViewModel::class.java)


        setupToolbar()

        pmsAverageParent.setOnClickListener { toggleSlide(pmsPriceList) }
        agoAverageParent.setOnClickListener { toggleSlide(agoPriceList) }
        ikAverageParent.setOnClickListener { toggleSlide(ikPriceList) }

        viewModel.getOmcs().observe(this, Observer {
            if (it.isSuccessful) {
                omcs = it.data()
            } else {
                omcs = null
                Timber.e(it.error())
            }
        })

        viewModel.getUser().observe(this, Observer {
            if (it.isSuccessful) {
                userModel = it.data()

                viewModel.setDeportId(userModel?.config?.app?.depotid.toString())
            } else {
                userModel = null
                Timber.e(it.error())
            }
        })

        populateFuelView()

        addFuel.setOnClickListener {
            if (!omcs.isNullOrEmpty()) {
                val dialog = AverageDialogFragment(omcs!!)
                dialog.show(supportFragmentManager, "average")

                dialog.averageEvent.subscribe {
                    if (userModel != null) {
                        submitFuelPrices(it)
                    } else {
                        toast("user data is not yet fetched")
                    }
                }

            } else {
                toast("No Omcs vailable, wait and then try again")
            }
        }


    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)

        supportActionBar?.title = "Average Prices"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun toggleSlide(view: View) {
        if (view.visibility == View.VISIBLE) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    private fun submitFuelPrices(data: AverageDialogEvent) {
        viewModel.postOmcAveragePrice(data).observe(this, Observer {
            if (it.isSuccessful) {
                toast("success")
            } else {
                toast("An error occurred while posting fuel information")
                Timber.e(it.error())
            }
        })
    }


    private fun populateFuelView() {
        viewModel.getTodayPrices().observe(this, Observer {
            if (it.isSuccessful) {
                val pmsPrices: ArrayList<AveragePriceModel> = ArrayList()
                val agoPrices: ArrayList<AveragePriceModel> = ArrayList()
                val ikPrices: ArrayList<AveragePriceModel> = ArrayList()

                it.data()?.forEach { avgModel ->
                    when (avgModel.fueltytype) {
                        "pms" -> {
                            pmsPrices.add(avgModel)
                        }

                        "ago" -> {
                            agoPrices.add(avgModel)
                        }

                        "ik" -> {
                            ikPrices.add(avgModel)
                        }
                    }
                }

                pmsView(pmsPrices)
                agoView(agoPrices)
                ikView(ikPrices)

            } else {
                Timber.e(it.error())
            }
        })
    }

    private fun getOmcName(omcId: String): String? {
        var v: OmcModel? = null

        omcs?.forEach {
            if (it.snapshotid == omcId) v = it
        }

        v.let { return it?.name }
    }

    private fun getFormattedDateUser(d: Date, u: String): String? {
        val dateFormat = SimpleDateFormat("hh:mm a")
        val dateString = dateFormat.format(d)
        return "$u @ $dateString"
    }

    private fun pmsView(pmsPrices: ArrayList<AveragePriceModel>) {
        if (pmsPrices.isEmpty()) {
            // empty values
            pmsPriceAverage.amount = 0.00f
            pmsLastEdit.text = "Never"
            pmsPriceList.removeAllViews()
        } else {

            pmsLastEdit.text = getFormattedDateUser(
                pmsPrices[pmsPrices.lastIndex].user?.time!!,
                pmsPrices[pmsPrices.lastIndex].user?.name!!
            )

            var totalPrice: Double = 0.0
            pmsPrices.forEach {
                totalPrice += it.price!!
            }

            pmsPriceAverage.amount = (totalPrice / pmsPrices.size).toFloat()

            pmsPriceList.removeAllViews()
            pmsPrices.forEach {
                val view: View = layoutInflater.inflate(R.layout.single_price_row, null)
                val fuelPrice: TextView = view.findViewById(R.id.fuelPrice)
                val fuelOmc: TextView = view.findViewById(R.id.fuelOmc)
                val userAdd: TextView = view.findViewById(R.id.userAdd)
                val priceId: TextView = view.findViewById(R.id.priceId)


                fuelPrice.text = it.price.toString()
                fuelOmc.text = getOmcName(it.omcId!!)
                userAdd.text = getFormattedDateUser(it.user?.time!!, it.user?.name!!)


                val priceRow: LinearLayout = view.findViewById(R.id.priceRow)
                priceId.text = it.snapshotid

                priceRow.setOnLongClickListener { _ ->
                    it.user?.name.let { n ->
                        if (n!! == firebaseAuth.currentUser?.displayName) {
                            deletePrice(priceId.text.toString())
                        } else {
                            toast("Sorry you cant delete this record")
                        }
                    }
                    true
                }

                pmsPriceList.addView(view)
            }
        }
    }

    private fun agoView(agoPrices: ArrayList<AveragePriceModel>) {
        if (agoPrices.isEmpty()) {
            // empty values
            agoPriceAverage.amount = 0.00f
            agoLastEdit.text = "Never"
            agoPriceList.removeAllViews()
        } else {

            agoLastEdit.text = getFormattedDateUser(
                agoPrices[agoPrices.lastIndex].user?.time!!,
                agoPrices[agoPrices.lastIndex].user?.name!!
            )

            var totalPrice: Double = 0.0
            agoPrices.forEach {
                totalPrice += it.price!!
            }

            agoPriceAverage.amount = (totalPrice / agoPrices.size).toFloat()

            agoPriceList.removeAllViews()
            agoPrices.forEach {
                val view: View = layoutInflater.inflate(R.layout.single_price_row, null)
                val fuelPrice: TextView = view.findViewById(R.id.fuelPrice)
                val fuelOmc: TextView = view.findViewById(R.id.fuelOmc)
                val userAdd: TextView = view.findViewById(R.id.userAdd)

                fuelPrice.text = it.price.toString()
                fuelOmc.text = getOmcName(it.omcId!!)



                userAdd.text = getFormattedDateUser(it.user?.time!!, it.user?.name!!)

                val priceId: TextView = view.findViewById(R.id.priceId)
                val priceRow: LinearLayout = view.findViewById(R.id.priceRow)
                priceId.text = it.snapshotid


                priceRow.setOnLongClickListener { _ ->
                    it.user?.name.let { n ->
                        if (n!! == firebaseAuth.currentUser?.displayName) {
                            deletePrice(priceId.text.toString())
                        } else {
                            toast("Sorry you cant delete this record")
                        }
                    }

                    true
                }


                agoPriceList.addView(view)
            }
        }
    }


    private fun ikView(ikPrices: ArrayList<AveragePriceModel>) {
        if (ikPrices.isEmpty()) {
            // empty values
            ikPriceAverage.amount = 0.00f
            ikLastEdit.text = "Never"
            ikPriceList.removeAllViews()
        } else {

            ikLastEdit.text = getFormattedDateUser(
                ikPrices[ikPrices.lastIndex].user?.time!!,
                ikPrices[ikPrices.lastIndex].user?.name!!
            )

            var totalPrice: Double = 0.0
            ikPrices.forEach {
                totalPrice += it.price!!
            }

            ikPriceAverage.amount = (totalPrice / ikPrices.size).toFloat()

            ikPriceList.removeAllViews()
            ikPrices.forEach {
                val view: View = layoutInflater.inflate(R.layout.single_price_row, null)
                val fuelPrice: TextView = view.findViewById(R.id.fuelPrice)
                val fuelOmc: TextView = view.findViewById(R.id.fuelOmc)
                val userAdd: TextView = view.findViewById(R.id.userAdd)

                fuelPrice.text = it.price.toString()
                fuelOmc.text = getOmcName(it.omcId!!)

                userAdd.text = getFormattedDateUser(it.user?.time!!, it.user?.name!!)

                val priceId: TextView = view.findViewById(R.id.priceId)
                val priceRow: LinearLayout = view.findViewById(R.id.priceRow)
                priceId.text = it.snapshotid

                priceRow.setOnLongClickListener { _ ->
                    it.user?.name.let { n ->
                        if (n!! == firebaseAuth.currentUser?.displayName) {
                            deletePrice(priceId.text.toString())
                        } else {
                            toast("Sorry you cant delete this record")
                        }
                    }
                    true
                }


                ikPriceList.addView(view)
            }
        }
    }

    private fun deletePrice(avg: String) {
        viewModel.deletePrice(avg).observe(this, Observer {
            if (it.isSuccessful) {
                toast("Deleted")
            } else {
                Timber.e(it.error())
            }
        })
    }
}
