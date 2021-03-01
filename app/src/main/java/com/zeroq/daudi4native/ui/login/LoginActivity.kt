package com.zeroq.daudi4native.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.snackbar.Snackbar
import com.zeroq.daudi4native.R
import javax.inject.Inject
import com.zeroq.daudi4native.commons.BaseActivity
import com.zeroq.daudi4native.databinding.ActivityLoginBinding
import com.zeroq.daudi4native.ui.MainActivity
import com.zeroq.daudi4native.ui.activate.ActivateActivity

import com.zeroq.daudi4native.vo.Status


class LoginActivity : BaseActivity() {

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    lateinit var loginViewModel: LoginViewModel

    lateinit var binding: ActivityLoginBinding

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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loginViewModel = getViewModel(LoginViewModel::class.java)

        binding.signInButton.setOnClickListener {
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
                binding.progressBar2.visibility = View.VISIBLE
            } else {
                binding.progressBar2.visibility = View.GONE
            }

            when (it.status) {
                Status.SUCCESS ->
                    Snackbar.make(
                        binding.mainLayout,
                        "Logged in successfully",
                        Snackbar.LENGTH_SHORT
                    ).show()

                else ->
                    Snackbar.make(
                        binding.mainLayout,
                        "Sorry an error occurred, try again",
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