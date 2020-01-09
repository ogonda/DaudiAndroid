package com.zeroq.daudi4native.ui.loading


import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.adapters.LoadingTrucksAdapter
import com.zeroq.daudi4native.commons.BaseFragment
import com.zeroq.daudi4native.data.models.OrderModel
import com.zeroq.daudi4native.events.LoadingEvent
import com.zeroq.daudi4native.ui.dialogs.LoadingDialogFragment
import com.zeroq.daudi4native.ui.dialogs.TimeDialogFragment
import com.zeroq.daudi4native.ui.loading_order.LoadingOrderActivity
import com.zeroq.daudi4native.utils.ActivityUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_loading.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class LoadingFragment : BaseFragment() {

    @Inject
    lateinit var activityUtil: ActivityUtil

    private lateinit var adapter: LoadingTrucksAdapter
    private var _TAG: String = "LoadingFragment"

    lateinit var viewModel: LoadingViewModel
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel(LoadingViewModel::class.java)

        viewModel.getUser().observe(this, Observer {
            if (it.isSuccessful) {
                val user = it.data()
                viewModel.setDepoId(user?.config?.app?.depotid.toString())
            } else {
                Timber.e(it.error()!!)
            }
        })

        initRecyclerView()
        createProgress()
        activityUtil.showProgress(spin_kit_l, true)
    }

    lateinit var progressDialog: Dialog
    private fun createProgress() {
        progressDialog = Dialog(activity!!)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(R.layout.custom_progress_dialog)
        progressDialog.setCancelable(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageEvent(event: LoadingEvent) {
        activityUtil.showProgress(spin_kit_l, false)

        if (event.error == null) {

            if (event.orders.isNullOrEmpty()) {
                adapter.clear()

                activityUtil.showTextViewState(
                    empty_view_l, true, "No trucks are in Loading",
                    ContextCompat.getColor(activity!!, R.color.colorPrimaryText)
                )
            } else {
                activityUtil.showTextViewState(
                    empty_view_l, false, null, null
                )
                adapter.replaceTrucks(event.orders)
            }
        } else {
            adapter.clear()

            activityUtil.showTextViewState(
                empty_view_l, true,
                "Something went wrong please, close the application to see if the issue wll be solved",
                ContextCompat.getColor(activity!!, R.color.pms)
            )
        }
    }

    private fun initRecyclerView() {
        adapter = LoadingTrucksAdapter(activityUtil)

        loading_view.layoutManager = LinearLayoutManager(activity)
        loading_view.adapter = adapter
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        consumeEvents()
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        compositeDisposable.clear()
        super.onStop()
    }


    private fun consumeEvents() {
        val clickSub: Disposable = adapter.expireTvClick
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                expireTimePicker(it.order)
            }


        val cardBodyClick: Disposable = adapter.cardBodyClick
            .subscribe {
                sealForm(it.order)
            }

        compositeDisposable.add(clickSub)
        compositeDisposable.add(cardBodyClick)
    }

    var expireSub: Disposable? = null
    private fun expireTimePicker(order: OrderModel) {
        expireSub?.dispose()
        expireSub = null

        val expireDialog = TimeDialogFragment("Enter Additional Time", order)
        expireSub = expireDialog.timeEvent.subscribe {
            viewModel.updateExpire(it.order.Id!!, it.minutes.toLong())
                .observe(this, Observer { result ->
                    if (!result.isSuccessful) {
                        Toast.makeText(
                            activity,
                            "Error occurred when adding expire", Toast.LENGTH_SHORT
                        ).show()

                        Timber.e(result.error())
                    }
                })
        }

        expireDialog.show(fragmentManager!!, _TAG)
    }

    var sealSub: Disposable? = null
    private fun sealForm(order: OrderModel) {
        sealSub?.dispose()
        sealSub = null


        if (order.truckStageData?.get("4") == null) {
            val sealDialog = LoadingDialogFragment(order)
            sealSub = sealDialog.loadingEvent.subscribe {
                sealDialog.dismiss() // hide dialog
                progressDialog.show() // show dialog

                viewModel.updateSeals(order.Id!!, it).observe(this, Observer { result ->
                    if (result.isSuccessful) {
                        progressDialog.hide() // hide progress
                        startLoadingOrderActivity(order.Id!!)
                    } else {
                        progressDialog.hide() // hide progress
                        Toast.makeText(
                            activity,
                            "An error occurred while posting seal data",
                            Toast.LENGTH_LONG
                        ).show()
                        Timber.e(result.error())
                    }
                })

            }

            sealDialog.show(fragmentManager!!, _TAG)
        } else {
            startLoadingOrderActivity(order.Id!!)
        }
    }


    private fun startLoadingOrderActivity(idTruck: String) {
        val intent = Intent(activity, LoadingOrderActivity::class.java)
        intent.putExtra(LoadingOrderActivity.ID_TRUCK_EXTRA, idTruck)

        startActivity(intent)

    }
}
