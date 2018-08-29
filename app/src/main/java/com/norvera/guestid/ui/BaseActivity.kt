/**
 * Base Activity for printing
 *
 * @author Brother Industries, Ltd.
 * @version 2.2
 */

package com.norvera.guestid.ui

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Message
import android.view.KeyEvent

import com.brother.ptouch.sdk.PrinterInfo
import com.norvera.guestid.common.Common
import com.norvera.guestid.common.MsgDialog
import com.norvera.guestid.common.MsgHandle

abstract class BaseActivity : Activity() {
    private val mUsbReceiver = object : BroadcastReceiver() {
        @TargetApi(12)
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    if (intent.getBooleanExtra(
                                    UsbManager.EXTRA_PERMISSION_GRANTED, false))
                        Common.mUsbRequest = 1
                    else
                        Common.mUsbRequest = 2
                }
            }
        }
    }
    internal var myPrint: BasePrint? = null
    internal var mHandle: MsgHandle? = null
    internal var mDialog: MsgDialog? = null

    /**
     * get the BluetoothAdapter
     */
    internal val bluetoothAdapter: BluetoothAdapter?
        get() {
            val bluetoothAdapter = BluetoothAdapter
                    .getDefaultAdapter()
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(enableBtIntent)
            }
            return bluetoothAdapter
        }

    abstract fun selectFileButtonOnClick()

    abstract fun printButtonOnClick()

    /**
     * Called when [Printer Settings] button is tapped
     */
    internal fun printerSettingsButtonOnClick() {
        startActivity(Intent(this, Activity_Settings::class.java))
    }

    /**
     * show message when BACK key is clicked
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            showTips()
        }
        return false
    }

    /**
     * show the BACK message
     */
    private fun showTips() {

        val alertDialog = AlertDialog.Builder(this)
                .setTitle(R.string.end_title)
                .setMessage(R.string.end_message)
                .setCancelable(false)
                .setPositiveButton(R.string.button_ok,
                        DialogInterface.OnClickListener { dialog, which -> finish() })
                .setNegativeButton(R.string.button_cancel,
                        DialogInterface.OnClickListener { dialog, which -> }).create()
        alertDialog.show()
    }

    @TargetApi(12)
    internal fun getUsbDevice(usbManager: UsbManager): UsbDevice? {
        if (myPrint!!.getPrinterInfo().port !== PrinterInfo.Port.USB) {
            return null
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            val msg = mHandle!!.obtainMessage(Common.MSG_WRONG_OS)
            mHandle!!.sendMessage(msg)
            return null
        }
        val usbDevice = myPrint!!.getUsbDevice(usbManager)
        if (usbDevice == null) {
            val msg = mHandle!!.obtainMessage(Common.MSG_NO_USB)
            mHandle!!.sendMessage(msg)
            return null
        }

        return usbDevice
    }

    @TargetApi(12)
    internal fun checkUSB(): Boolean {
        if (myPrint!!.getPrinterInfo().port !== PrinterInfo.Port.USB) {
            return true
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            val msg = mHandle!!.obtainMessage(Common.MSG_WRONG_OS)
            mHandle!!.sendMessage(msg)
            return false
        }
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDevice = myPrint!!.getUsbDevice(usbManager)
        if (usbDevice == null) {
            val msg = mHandle!!.obtainMessage(Common.MSG_NO_USB)
            mHandle!!.sendMessage(msg)
            return false
        }
        val permissionIntent = PendingIntent.getBroadcast(this, 0,
                Intent(ACTION_USB_PERMISSION), 0)
        registerReceiver(mUsbReceiver, IntentFilter(ACTION_USB_PERMISSION))
        if (!usbManager.hasPermission(usbDevice)) {
            Common.mUsbRequest = 0
            usbManager.requestPermission(usbDevice, permissionIntent)
        } else {
            Common.mUsbRequest = 1
        }
        return true
    }

    companion object {

        internal val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }
}