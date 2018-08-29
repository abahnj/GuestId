package com.norvera.guestid.ui.fragments

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.norvera.guestid.R
import com.norvera.guestid.ui.interfaces.LoginFragmentInterface
import com.norvera.guestid.ui.viewmodels.MainActivityViewModel
import com.norvera.guestid.utilities.onClick
import kotlinx.android.synthetic.main.login_fragment.*


class LoginFragment : Fragment() {

    private lateinit var model: MainActivityViewModel

    private lateinit var replaceFragmentListener: LoginFragmentInterface

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        replaceFragmentListener = activity as LoginFragmentInterface
    }

    companion object {
        fun newInstance() = LoginFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.login_fragment, container, false)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model = ViewModelProviders.of(activity!!).get(MainActivityViewModel::class.java)

        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        next_button onClick onNextButtonClick()

    }

    private fun onNextButtonClick() : () -> Unit = {
        val phoneNumber = eTvPhoneNumber.editText?.text.toString()
        Toast.makeText(activity, phoneNumber, Toast.LENGTH_SHORT ).show()
        val user = model.findUserByNumber(phoneNumber).value

        if (user == null) {
            Toast.makeText(activity, "Not Found", Toast.LENGTH_SHORT).show()
        }

        replaceFragmentListener.onPhoneContinueClick()
    }


}
