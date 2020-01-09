package com.zeroq.daudi4native.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.snackbar.Snackbar
import com.zeroq.daudi4native.R
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject
import com.zeroq.daudi4native.commons.BaseActivity
import com.zeroq.daudi4native.ui.MainActivity
import com.zeroq.daudi4native.ui.activate.ActivateActivity

import com.zeroq.daudi4native.vo.Status


class LoginActivity : BaseActivity() {

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    lateinit var loginViewModel: LoginViewModel


    companion object {
        fun startActivity(context: Context) {
            /**
             * start login activity and clear backstack completely
             * */
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginViewModel = getViewModel(LoginViewModel::class.java)

        sign_in_button.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, loginViewModel.RC_SIGN_IN)
        }

        operations()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loginViewModel.onResultFromActivity(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }


    var loginInvoked = false
    private fun operations() {
        /**
         * Authentication response
         * */
        loginViewModel.getLogin().observe(this, Observer {

            if (it.status == Status.LOADING) {
                progressBar2.visibility = View.VISIBLE
            } else {
                progressBar2.visibility = View.GONE
            }

            when (it.status) {
                Status.SUCCESS ->
                    Snackbar.make(
                        main_layout,
                        "Logged in successfully",
                        Snackbar.LENGTH_SHORT
                    ).show()

                else ->
                    Snackbar.make(
                        main_layout,
                        "Sorry an error occured, try again",
                        Snackbar.LENGTH_SHORT
                    ).show()
            }
        })

        loginViewModel.getUser().observe(this, Observer {
            loginInvoked = !loginInvoked
            if (loginInvoked)
                if (it.isSuccessful) {
                    MainActivity.startActivity(this)
                } else {
                    ActivateActivity.startActivity(this)
                }
            this.finish()
        })
    }

}