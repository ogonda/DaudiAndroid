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
import com.zeroq.daudi4native.data.models.DepotModel
import com.zeroq.daudi4native.data.models.OrderModel
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.databinding.FragmentLoadingBinding
import com.zeroq.daudi4native.events.LoadingEvent
import com.zeroq.daudi4native.ui.dialogs.LoadingDialogFragment
import com.zeroq.daudi4native.ui.dialogs.TimeDialogFragment
import com.zeroq.daudi4native.ui.loading_order.LoadingOrderActivity
import com.zeroq.daudi4native.utils.ActivityUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class LoadingFragment : BaseFragment() {

    private var _binding: FragmentLoadingBinding? = null

    private val binding get() = _binding!!

    @Inject
    lateinit var activityUtil: ActivityUtil

    private lateinit var adapter: LoadingTrucksAdapter
    private var _TAG: String = "LoadingFragment"

    lateinit var viewModel: LoadingViewModel
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userModel: UserModel? = null
    private var depot: DepotModel? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel(LoadingViewModel::class.java)

        viewModel.getUser().observe(viewLifecycleOwner, Observer {
            if (it.isSuccessful) {
                userModel = it.data()
                viewModel.setSwitchUser(userModel!!)
                viewModel.setDepoId(userModel?.config?.app?.depotid.toString())
            } else {
                Timber.e(it.error())
            }
        })

        viewModel.getDepot().observe(viewLifecycleOwner, Observer {
            if (it.isSuccessful) {
                depot = it.data()
            } else {
                depot = null
                Timber.e(it.error())
            }
        })

        initRecyclerView()
        createProgress()
        activityUtil.showProgress(binding.spinKitL, true)
    }

    lateinit var progressDialog: Dialog
    private fun createProgress() {
        progressDialog = Dialog(requireActivity())
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(R.layout.custom_progress_dialog)
        progressDialog.setCancelable(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageEvent(event: LoadingEvent) {
        activityUtil.showProgress(binding.spinKitL, false)

        if (event.error == null) {

            if (event.orders.isNullOrEmpty()) {
                adapter.clear()

                activityUtil.showTextViewState(
                    binding.emptyViewL, true, "No trucks are in Loading",
                    ContextCompat.getColor(requireActivity(), R.color.colorPrimaryText)
                )
            } else {
                activityUtil.showTextViewState(
                    binding.emptyViewL, false, null, null
                )
                adapter.replaceTrucks(event.orders)
            }
        } else {
            adapter.clear()

            activityUtil.showTextViewState(
                binding.emptyViewL, true,
                "Something went wrong please, close the application to see if the issue wll be solved",
                ContextCompat.getColor(requireActivity(), R.color.pms)
            )
        }
    }

    private fun initRecyclerView() {
        adapter = LoadingTrucksAdapter(activityUtil)

        binding.loadingView.layoutManager = LinearLayoutManager(activity)
        binding.loadingView.adapter = adapter
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
                val now = Calendar.getInstance();

                if (it.order.truckStageData!!["3"]?.expiry!![0].expiry!!.before(now.time)) {
                    expireTimePicker(it.order)
                }
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
            userModel?.let { u ->
                viewModel.updateExpire(u, order, it.minutes.toLong())
                    .observe(this, Observer { state ->
                        if (!state.isSuccessful) {
                            Toast.makeText(
                                activity,
                                "Error occurred when adding expire", Toast.LENGTH_SHORT
                            ).show()

                            Timber.e(state.error())
                        }
                    })
            }
        }

        expireDialog.show(requireFragmentManager(), _TAG)
    }

    var sealSub: Disposable? = null
    private fun sealForm(order: OrderModel) {
        sealSub?.dispose()
        sealSub = null

        order.seals?.range?.let { ranges ->
            if (ranges.isEmpty()) {
                val sealDialog = LoadingDialogFragment(order)
                sealSub = sealDialog.loadingEvent.subscribe {
                    sealDialog.dismiss() // hide dialog
                    progressDialog.show() // show dialog

                    userModel?.let { user ->
                        viewModel.updateSeals(user, it, order.Id!!, depot!!)
                            .observe(this, Observer { result ->
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
                }

                sealDialog.show(requireFragmentManager(), _TAG)
            } else {
                startLoadingOrderActivity(order.Id!!)
            }
        }

    }


    private fun startLoadingOrderActivity(orderId: String) {
        val intent = Intent(activity, LoadingOrderActivity::class.java)
        intent.putExtra(LoadingOrderActivity.ID_ORDER_EXTRA, orderId)

        startActivity(intent)
    }
}
