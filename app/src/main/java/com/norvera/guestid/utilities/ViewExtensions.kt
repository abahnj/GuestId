package com.norvera.guestid.utilities

import android.view.View



internal infix fun View.onClick(function: () -> Unit) {
    setOnClickListener { function() }
}


