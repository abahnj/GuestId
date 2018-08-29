package com.norvera.guestid.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.norvera.guestid.utilities.InjectorUtils
import androidx.fragment.app.transaction
import com.norvera.guestid.ui.fragments.LoginFragment
import com.norvera.guestid.ui.viewmodels.MainActivityViewModel
import com.norvera.guestid.R
import com.norvera.guestid.ui.fragments.CameraFragment
import com.norvera.guestid.ui.fragments.UserFragment
import com.norvera.guestid.ui.interfaces.LoginFragmentInterface

class MainActivity : AppCompatActivity(), LoginFragmentInterface {
    override fun onContinueClick() {
        supportFragmentManager.transaction(allowStateLoss = true) {
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            replace(R.id.fragment_container, CameraFragment.newInstance("me", "you"), CameraFragment.toString())
        }    }

    override fun onPhoneContinueClick() {
        supportFragmentManager.transaction(allowStateLoss = true) {
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            replace(R.id.fragment_container, UserFragment.newInstance(), UserFragment.toString())
        }
    }

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        supportFragmentManager.transaction(allowStateLoss = true) {
            add(R.id.fragment_container, LoginFragment.newInstance(), LoginFragment.toString())
        }

        val factory = InjectorUtils.provideMainActivityViewModelFactory(this)

        viewModel = ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)
    }


}
