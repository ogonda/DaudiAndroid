package com.zeroq.daudi4native.ui.splash

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.commons.BaseActivity
import com.zeroq.daudi4native.ui.MainActivity
import com.zeroq.daudi4native.ui.activate.ActivateActivity
import com.zeroq.daudi4native.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // get viewmodel
        viewModel = getViewModel(SplashViewModel::class.java)
        showProgress(true)


        /**
         * delay for better transition
         * */
        Handler().postDelayed({
            operations()
        }, 1000)
    }




    private fun operations() {
        var signTrigger = false
        viewModel.isSignedIn().observe(this, Observer {
            signTrigger = !signTrigger
            if (signTrigger)
                if (!it) {
                    showProgress(false)
                    LoginActivity.startActivity(this)
                    this.finish()
                }
        })

        // check if admin is allowed to go to the next step
        var adminTriggered: Boolean = false
        viewModel.getAdmin().observe(this, Observer {
            adminTriggered = !adminTriggered

            showProgress(false)
            if (adminTriggered)
                if (it.isSuccessful) {
                    MainActivity.startActivity(this)
                } else {
                    ActivateActivity.startActivity(this)
                }
            this.finish()
        })
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }
}