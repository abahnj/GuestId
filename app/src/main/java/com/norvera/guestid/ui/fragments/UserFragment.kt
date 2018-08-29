package com.norvera.guestid.ui.fragments

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.norvera.guestid.R
import com.norvera.guestid.ui.interfaces.LoginFragmentInterface
import com.norvera.guestid.ui.viewmodels.UserViewModel
import com.norvera.guestid.utilities.onClick
import kotlinx.android.synthetic.main.user_fragment.*

class UserFragment : Fragment() {

    companion object {
        fun newInstance() = UserFragment()
    }

    private lateinit var viewModel: UserViewModel
    private var listener: LoginFragmentInterface? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LoginFragmentInterface) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.user_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(UserViewModel::class.java)
        // TODO: Use the ViewModel
    }

    // TODO: Rename method, update argument and hook method into UI event
    private fun onContinuePressed() : () -> Unit ={
        listener?.onContinueClick()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        next_button onClick onContinuePressed()

    }
}
