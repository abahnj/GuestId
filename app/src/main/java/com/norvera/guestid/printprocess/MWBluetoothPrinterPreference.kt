package com.norvera.guestid.printprocess

import android.content.Context
import android.os.Message

import com.brother.ptouch.sdk.BluetoothPreference
import com.brother.ptouch.sdk.PrinterInfo
import com.brother.ptouch.sdk.PrinterStatus
import com.norvera.guestid.common.Common
import com.norvera.guestid.common.MsgDialog
import com.norvera.guestid.common.MsgHandle


import java.util.EventListener

class MWBluetoothPrinterPreference(private val context: Context, mHandle: MsgHandle,
                                   mDialog: MsgDialog) : BasePrint(context, mHandle, mDialog) {
    private var btPref: BluetoothPreference? = null
    private var commandType = 0
    private var listener: PrinterPreListener? = null

    /**
     * Updating the printer settings The results are reported in listener
     *
     * @param btPref
     */
    fun updatePrinterSetting(btPref: BluetoothPreference) {
        BasePrint.mCancel = false
        this.commandType = COMMAND_SEND
        this.btPref = btPref
        val pref = PrinterPrefThread()
        pref.start()
    }

    /**
     * Getting the printer settings
     *
     * @param listener
     */
    fun getPrinterSetting(listener: PrinterPreListener?) {
        if (listener == null) {
            mDialog.showAlertDialog(
                    context.getString(R.string.msg_title_warning),
                    context.getString(R.string.error_input))
            return
        }
        BasePrint.mCancel = false
        this.listener = listener

        this.commandType = COMMAND_RECEIVE
        val pref = PrinterPrefThread()
        pref.start()
    }

    override fun doPrint() {}

    interface PrinterPreListener : EventListener {
        fun finish(status: PrinterStatus, btPre: BluetoothPreference)
    }

    private inner class PrinterPrefThread : Thread() {
        override fun run() {

            // set info. for printing
            setPrinterInfo()

            // start message
            var msg = mHandle.obtainMessage(Common.MSG_TRANSFER_START)
            mHandle.sendMessage(msg)
            mHandle.setFunction(MsgHandle.FUNC_SETTING)

            printResult = PrinterStatus()
            if (!BasePrint.mCancel) {
                if (commandType == COMMAND_SEND) {
                    printResult = BasePrint.mPrinter.updateBluetoothPreference(btPref)

                } else if (commandType == COMMAND_RECEIVE) {
                    btPref = BluetoothPreference()
                    printResult = BasePrint.mPrinter.getBluetoothPreference(btPref)
                }

                if (listener != null) {
                    listener!!.finish(printResult, btPref)
                }
            } else {
                printResult.errorCode = PrinterInfo.ErrorCode.ERROR_CANCEL
            }
            // end message
            mHandle.setResult(showResult())
            mHandle.setBattery(batteryDetail)

            msg = mHandle.obtainMessage(Common.MSG_DATA_SEND_END)
            mHandle.sendMessage(msg)
        }
    }

    companion object {
        private val COMMAND_SEND = 0
        private val COMMAND_RECEIVE = 1
    }

}
