/**
 * BasePrint for printing
 *
 * @author Brother Industries, Ltd.
 * @version 2.2
 */

package com.norvera.guestid.printprocess

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.SharedPreferences
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Message
import android.preference.PreferenceManager

import com.brother.ptouch.sdk.JNIStatus.BatteryTernary
import com.brother.ptouch.sdk.LabelInfo
import com.brother.ptouch.sdk.Printer
import com.brother.ptouch.sdk.PrinterInfo
import com.brother.ptouch.sdk.PrinterInfo.ErrorCode
import com.brother.ptouch.sdk.PrinterInfo.Model
import com.brother.ptouch.sdk.PrinterStatus
import com.brother.ptouch.sdk.printdemo.R
import com.brother.ptouch.sdk.printdemo.common.Common
import com.brother.ptouch.sdk.printdemo.common.MsgDialog
import com.brother.ptouch.sdk.printdemo.common.MsgHandle

abstract class BasePrint internal constructor(private val mContext: Context, internal val mHandle: MsgHandle, internal val mDialog: MsgDialog) {
    private val sharedPreferences: SharedPreferences
    /**
     * get Printer
     */
    /**
     * get Printer
     */
    var printResult: PrinterStatus
    private var customSetting: String? = null
    private var mPrinterInfo: PrinterInfo? = null

    /**
     * get PrinterInfo
     */
    val printerInfo: PrinterInfo?
        get() {
            getPreferences()
            return mPrinterInfo
        }

    /**
     * get Printer
     */
    val printer: Printer?
        get() = mPrinter

    /**
     * show information of battery
     */
    val battery: String
        get() {

            if (printResult.isACConnected == BatteryTernary.Yes) {
                return mContext.getString(R.string.ac_adapter)
            }

            if (printResult.maxOfBatteryResidualQuantityLevel == 0) {
                return mContext.getString(R.string.battery_full)
            } else if (printResult.maxOfBatteryResidualQuantityLevel == 2) {
                when (printResult.batteryResidualQuantityLevel) {
                    0 -> return mContext.getString(R.string.battery_weak)
                    1 -> return mContext.getString(R.string.battery_middle)
                    2 -> return mContext.getString(R.string.battery_full)
                    else -> {
                    }
                }
            } else if (printResult.maxOfBatteryResidualQuantityLevel == 3) {
                when (printResult.batteryResidualQuantityLevel) {
                    0 -> return mContext.getString(R.string.battery_charge)
                    1 -> return mContext.getString(R.string.battery_weak)
                    2 -> return mContext.getString(R.string.battery_middle)
                    3 -> return mContext.getString(R.string.battery_full)
                    else -> {
                    }
                }
            } else if (printResult.maxOfBatteryResidualQuantityLevel == 4) {
                when (printResult.batteryResidualQuantityLevel) {
                    0 -> return mContext.getString(R.string.battery_charge)
                    1 -> return mContext.getString(R.string.battery_weak)
                    2 -> return mContext.getString(R.string.battery_middle)
                    3 -> return mContext.getString(R.string.battery_middle)
                    4 -> return mContext.getString(R.string.battery_full)
                    else -> {
                    }
                }
            } else if (printResult.maxOfBatteryResidualQuantityLevel == 100) {
                if (printResult.batteryResidualQuantityLevel > 80) {
                    return mContext.getString(R.string.battery_full)
                } else if (30 <= printResult.batteryResidualQuantityLevel && printResult.batteryResidualQuantityLevel <= 80) {
                    return mContext.getString(R.string.battery_middle)
                } else if (0 <= printResult.batteryResidualQuantityLevel && printResult.batteryResidualQuantityLevel < 30) {
                    return mContext.getString(R.string.battery_weak)
                }
            } else {
                val ratio = printResult.batteryResidualQuantityLevel.toDouble() / printResult.maxOfBatteryResidualQuantityLevel
                if (ratio > 0.8) {
                    return mContext.getString(R.string.battery_full)
                } else if (0.3 <= ratio && ratio <= 0.8) {
                    return mContext.getString(R.string.battery_middle)
                } else if (0 <= ratio && ratio < 0.3) {
                    return mContext.getString(R.string.battery_weak)
                }
            }
            return ""
        }

    val batteryDetail: String
        get() = String.format("%d/%d(AC=%s,BM=%s)",
                printResult.batteryResidualQuantityLevel,
                printResult.maxOfBatteryResidualQuantityLevel,
                printResult.isACConnected.name,
                printResult.isBatteryMounted.name)

    init {
        mDialog.setHandle(mHandle)
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext)
        mCancel = false
        // initialization for print
        mPrinterInfo = PrinterInfo()
        mPrinter = Printer()
        mPrinterInfo = mPrinter!!.printerInfo
        mPrinter!!.setMessageHandle(mHandle, Common.MSG_SDK_EVENT)
    }

    protected abstract fun doPrint()

    /**
     * set PrinterInfo
     */
    fun setPrinterInfo() {

        getPreferences()
        setCustomPaper()
        mPrinter!!.printerInfo = mPrinterInfo
        if (mPrinterInfo!!.port == PrinterInfo.Port.USB) {
            while (true) {
                if (Common.mUsbRequest !== 0)
                    break
            }
            if (Common.mUsbRequest !== 1) {
            }
        }
    }

    fun setBluetoothAdapter(bluetoothAdapter: BluetoothAdapter) {

        mPrinter!!.setBluetooth(bluetoothAdapter)
    }

    @TargetApi(12)
    fun getUsbDevice(usbManager: UsbManager): UsbDevice {
        return mPrinter!!.getUsbDevice(usbManager)
    }

    /**
     * get the printer settings from the SharedPreferences
     */
    private fun getPreferences() {
        if (mPrinterInfo == null) {
            mPrinterInfo = PrinterInfo()
            return
        }
        var input: String?
        mPrinterInfo!!.printerModel = PrinterInfo.Model.valueOf(sharedPreferences
                .getString("printerModel", ""))
        mPrinterInfo!!.port = PrinterInfo.Port.valueOf(sharedPreferences
                .getString("port", ""))
        mPrinterInfo!!.ipAddress = sharedPreferences.getString("address", "")
        mPrinterInfo!!.macAddress = sharedPreferences.getString("macAddress", "")
        if (isLabelPrinter(mPrinterInfo!!.printerModel)) {
            mPrinterInfo!!.paperSize = PrinterInfo.PaperSize.CUSTOM
            when (mPrinterInfo!!.printerModel) {
                PrinterInfo.Model.QL_710W, PrinterInfo.Model.QL_720NW, PrinterInfo.Model.QL_800, PrinterInfo.Model.QL_810W, PrinterInfo.Model.QL_820NWB -> {
                    mPrinterInfo!!.labelNameIndex = LabelInfo.QL700.valueOf(
                            sharedPreferences.getString("paperSize", "")).ordinal
                    mPrinterInfo!!.isAutoCut = java.lang.Boolean.parseBoolean(sharedPreferences
                            .getString("autoCut", ""))
                    mPrinterInfo!!.isCutAtEnd = java.lang.Boolean
                            .parseBoolean(sharedPreferences.getString("endCut", ""))
                }
                PrinterInfo.Model.QL_1100, PrinterInfo.Model.QL_1110NWB -> {
                    mPrinterInfo!!.labelNameIndex = LabelInfo.QL1100.valueOf(
                            sharedPreferences.getString("paperSize", "")).ordinal
                    mPrinterInfo!!.isAutoCut = java.lang.Boolean.parseBoolean(sharedPreferences
                            .getString("autoCut", ""))
                    mPrinterInfo!!.isCutAtEnd = java.lang.Boolean
                            .parseBoolean(sharedPreferences.getString("endCut", ""))
                }
                PrinterInfo.Model.QL_1115NWB -> {
                    mPrinterInfo!!.labelNameIndex = LabelInfo.QL1115.valueOf(
                            sharedPreferences.getString("paperSize", "")).ordinal
                    mPrinterInfo!!.isAutoCut = java.lang.Boolean.parseBoolean(sharedPreferences
                            .getString("autoCut", ""))
                    mPrinterInfo!!.isCutAtEnd = java.lang.Boolean
                            .parseBoolean(sharedPreferences.getString("endCut", ""))
                }
                PrinterInfo.Model.PT_E550W, PrinterInfo.Model.PT_E500, PrinterInfo.Model.PT_P750W, PrinterInfo.Model.PT_P710BT, PrinterInfo.Model.PT_D800W, PrinterInfo.Model.PT_E800W, PrinterInfo.Model.PT_E850TKW, PrinterInfo.Model.PT_P900W, PrinterInfo.Model.PT_P950NW -> {
                    val paper = sharedPreferences.getString("paperSize", "")
                    mPrinterInfo!!.labelNameIndex = LabelInfo.PT.valueOf(paper)
                            .ordinal
                    mPrinterInfo!!.isAutoCut = java.lang.Boolean.parseBoolean(sharedPreferences
                            .getString("autoCut", ""))
                    mPrinterInfo!!.isCutAtEnd = java.lang.Boolean
                            .parseBoolean(sharedPreferences.getString("endCut", ""))
                    mPrinterInfo!!.isHalfCut = java.lang.Boolean.parseBoolean(sharedPreferences
                            .getString("halfCut", ""))
                    mPrinterInfo!!.isSpecialTape = java.lang.Boolean
                            .parseBoolean(sharedPreferences.getString(
                                    "specialType", ""))
                }
                PrinterInfo.Model.PT_P300BT -> {
                    mPrinterInfo!!.labelNameIndex = LabelInfo.PT3.valueOf(
                            sharedPreferences.getString("paperSize", "")).ordinal
                    mPrinterInfo!!.isCutMark = java.lang.Boolean
                            .parseBoolean(sharedPreferences.getString(
                                    "cutMark", ""))
                    mPrinterInfo!!.isCutAtEnd = java.lang.Boolean
                            .parseBoolean(sharedPreferences.getString("endCut", ""))

                    input = sharedPreferences.getString("labelMargin", "")
                    if (input == "")
                        input = "0"
                    mPrinterInfo!!.labelMargin = Integer.parseInt(input)
                }
                else -> {
                }
            }
        } else {
            mPrinterInfo!!.paperSize = PrinterInfo.PaperSize
                    .valueOf(sharedPreferences.getString("paperSize", ""))
        }
        mPrinterInfo!!.orientation = PrinterInfo.Orientation
                .valueOf(sharedPreferences.getString("orientation", ""))
        input = sharedPreferences.getString("numberOfCopies", "1")
        if (input == "")
            input = "1"
        mPrinterInfo!!.numberOfCopies = Integer.parseInt(input)
        mPrinterInfo!!.halftone = PrinterInfo.Halftone.valueOf(sharedPreferences
                .getString("halftone", ""))
        mPrinterInfo!!.printMode = PrinterInfo.PrintMode
                .valueOf(sharedPreferences.getString("printMode", ""))
        mPrinterInfo!!.pjCarbon = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("pjCarbon", ""))
        input = sharedPreferences.getString("pjDensity", "")
        if (input == "")
            input = "5"
        mPrinterInfo!!.pjDensity = Integer.parseInt(input)
        mPrinterInfo!!.pjFeedMode = PrinterInfo.PjFeedMode
                .valueOf(sharedPreferences.getString("pjFeedMode", ""))
        mPrinterInfo!!.align = PrinterInfo.Align.valueOf(sharedPreferences
                .getString("align", ""))
        input = sharedPreferences.getString("leftMargin", "")
        if (input == "")
            input = "0"
        mPrinterInfo!!.margin.left = Integer.parseInt(input)
        mPrinterInfo!!.valign = PrinterInfo.VAlign.valueOf(sharedPreferences
                .getString("valign", ""))
        input = sharedPreferences.getString("topMargin", "")
        if (input == "")
            input = "0"
        mPrinterInfo!!.margin.top = Integer.parseInt(input)
        input = sharedPreferences.getString("customPaperWidth", "")
        if (input == "")
            input = "0"
        mPrinterInfo!!.customPaperWidth = Integer.parseInt(input)

        input = sharedPreferences.getString("customPaperLength", "0")
        if (input == "")
            input = "0"

        mPrinterInfo!!.customPaperLength = Integer.parseInt(input)
        input = sharedPreferences.getString("customFeed", "")
        if (input == "")
            input = "0"
        mPrinterInfo!!.customFeed = Integer.parseInt(input)

        customSetting = sharedPreferences.getString("customSetting", "")
        mPrinterInfo!!.paperPosition = PrinterInfo.Align
                .valueOf(sharedPreferences.getString("paperPosition", "LEFT"))
        mPrinterInfo!!.dashLine = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("dashLine", "false"))

        mPrinterInfo!!.rjDensity = Integer.parseInt(sharedPreferences.getString(
                "rjDensity", ""))
        mPrinterInfo!!.rotate180 = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("rotate180", ""))
        mPrinterInfo!!.peelMode = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("peelMode", ""))

        mPrinterInfo!!.mode9 = java.lang.Boolean.parseBoolean(sharedPreferences.getString(
                "mode9", ""))
        mPrinterInfo!!.dashLine = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("dashLine", ""))
        input = sharedPreferences.getString("pjSpeed", "2")
        mPrinterInfo!!.pjSpeed = Integer.parseInt(input)

        mPrinterInfo!!.pjPaperKind = PrinterInfo.PjPaperKind
                .valueOf(sharedPreferences.getString("pjPaperKind",
                        "PJ_CUT_PAPER"))

        mPrinterInfo!!.rollPrinterCase = PrinterInfo.PjRollCase
                .valueOf(sharedPreferences.getString("printerCase",
                        "PJ_ROLLCASE_OFF"))

        mPrinterInfo!!.skipStatusCheck = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("skipStatusCheck", "false"))

        mPrinterInfo!!.checkPrintEnd = PrinterInfo.CheckPrintEnd
                .valueOf(sharedPreferences.getString("checkPrintEnd", "CPE_CHECK"))
        mPrinterInfo!!.printQuality = PrinterInfo.PrintQuality
                .valueOf(sharedPreferences.getString("printQuality",
                        "NORMAL"))
        mPrinterInfo!!.overwrite = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("overwrite", "true"))

        mPrinterInfo!!.trimTapeAfterData = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("trimTapeAfterData", "false"))

        input = sharedPreferences.getString("imageThresholding", "")
        if (input == "")
            input = "127"
        mPrinterInfo!!.thresholdingValue = Integer.parseInt(input)

        input = sharedPreferences.getString("scaleValue", "")
        if (input == "")
            input = "0"
        try {
            mPrinterInfo!!.scaleValue = java.lang.Double.parseDouble(input)
        } catch (e: NumberFormatException) {
            mPrinterInfo!!.scaleValue = 1.0
        }

        if (mPrinterInfo!!.printerModel == Model.TD_4000 || mPrinterInfo!!.printerModel == Model.TD_4100N) {
            mPrinterInfo!!.isAutoCut = java.lang.Boolean.parseBoolean(sharedPreferences
                    .getString("autoCut", ""))
            mPrinterInfo!!.isCutAtEnd = java.lang.Boolean.parseBoolean(sharedPreferences
                    .getString("endCut", ""))
        }

        input = sharedPreferences.getString("savePrnPath", "")
        mPrinterInfo!!.savePrnPath = input


        mPrinterInfo!!.workPath = sharedPreferences.getString("workPath", "")
        mPrinterInfo!!.softFocusing = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("softFocusing", "false"))
        mPrinterInfo!!.enabledTethering = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("enabledTethering", "false"))
        mPrinterInfo!!.rawMode = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("rawMode", "false"))


        input = sharedPreferences.getString("processTimeout", "")
        if (input == "")
            input = "0"
        mPrinterInfo!!.timeout.processTimeoutSec = Integer.parseInt(input)

        input = sharedPreferences.getString("sendTimeout", "")
        if (input == "")
            input = "60"
        mPrinterInfo!!.timeout.sendTimeoutSec = Integer.parseInt(input)

        input = sharedPreferences.getString("receiveTimeout", "")
        if (input == "")
            input = "180"
        mPrinterInfo!!.timeout.receiveTimeoutSec = Integer.parseInt(input)

        input = sharedPreferences.getString("connectionTimeout", "")
        if (input == "")
            input = "0"
        mPrinterInfo!!.timeout.connectionWaitMSec = Integer.parseInt(input)

        input = sharedPreferences.getString("closeWaitTime", "")
        if (input == "")
            input = "3"
        mPrinterInfo!!.timeout.closeWaitDisusingStatusCheckSec = Integer.parseInt(input)

        mPrinterInfo!!.useLegacyHalftoneEngine = java.lang.Boolean.parseBoolean(sharedPreferences
                .getString("useLegacyHalftoneEngine", "false"))
    }

    /**
     * Launch the thread to print
     */
    fun print() {
        mCancel = false
        val printTread = PrinterThread()
        printTread.start()
    }

    /**
     * Launch the thread to get the printer's status
     */
    fun getPrinterStatus() {
        mCancel = false
        val getTread = getStatusThread()
        getTread.start()
    }

    /**
     * Launch the thread to print
     */
    fun sendFile() {


        val getTread = SendFileThread()
        getTread.start()
    }

    /**
     * set custom paper for RJ and TD
     */
    private fun setCustomPaper() {

        when (mPrinterInfo!!.printerModel) {
            PrinterInfo.Model.RJ_4030, PrinterInfo.Model.RJ_4030Ai, PrinterInfo.Model.RJ_4040, PrinterInfo.Model.RJ_3050, PrinterInfo.Model.RJ_3150, PrinterInfo.Model.TD_2020, PrinterInfo.Model.TD_2120N, PrinterInfo.Model.TD_2130N, PrinterInfo.Model.TD_4100N, PrinterInfo.Model.TD_4000, PrinterInfo.Model.RJ_2030, PrinterInfo.Model.RJ_2140, PrinterInfo.Model.RJ_2150, PrinterInfo.Model.RJ_2050, PrinterInfo.Model.RJ_3050Ai, PrinterInfo.Model.RJ_3150Ai, PrinterInfo.Model.RJ_4230B -> mPrinterInfo!!.customPaper = Common.CUSTOM_PAPER_FOLDER + customSetting!!
            else -> {
            }
        }
    }

    /**
     * get the end message of print
     */
    fun showResult(): String {

        val result: String
        if (printResult.errorCode == ErrorCode.ERROR_NONE) {
            result = mContext.getString(R.string.error_message_none)
        } else {
            result = printResult.errorCode.toString()
        }

        return result
    }

    private fun isLabelPrinter(model: PrinterInfo.Model): Boolean {
        when (model) {
            PrinterInfo.Model.QL_710W, PrinterInfo.Model.QL_720NW, PrinterInfo.Model.PT_E550W, PrinterInfo.Model.PT_E500, PrinterInfo.Model.PT_P750W, PrinterInfo.Model.PT_D800W, PrinterInfo.Model.PT_E800W, PrinterInfo.Model.PT_E850TKW, PrinterInfo.Model.PT_P900W, PrinterInfo.Model.PT_P950NW, PrinterInfo.Model.QL_810W, PrinterInfo.Model.QL_800, PrinterInfo.Model.QL_820NWB, PrinterInfo.Model.PT_P300BT, PrinterInfo.Model.QL_1100, PrinterInfo.Model.QL_1110NWB, PrinterInfo.Model.QL_1115NWB, PrinterInfo.Model.PT_P710BT -> return true
            else -> return false
        }
    }

    /**
     * Thread for printing
     */
    private inner class PrinterThread : Thread() {
        override fun run() {

            // set info. for printing
            setPrinterInfo()

            // start message
            var msg = mHandle.obtainMessage(Common.MSG_PRINT_START)
            mHandle.sendMessage(msg)

            printResult = PrinterStatus()

            mPrinter!!.startCommunication()
            if (!mCancel) {
                doPrint()
            } else {
                printResult.errorCode = ErrorCode.ERROR_CANCEL
            }
            mPrinter!!.endCommunication()

            // end message
            mHandle.setResult(showResult())
            mHandle.setBattery(batteryDetail)
            msg = mHandle.obtainMessage(Common.MSG_PRINT_END)
            mHandle.sendMessage(msg)
        }
    }

    /**
     * Thread for getting the printer's status
     */
    private inner class getStatusThread : Thread() {
        override fun run() {

            // set info. for printing
            setPrinterInfo()

            // start message
            var msg = mHandle.obtainMessage(Common.MSG_PRINT_START)
            mHandle.sendMessage(msg)

            printResult = PrinterStatus()
            if (!mCancel) {
                printResult = mPrinter!!.printerStatus
            } else {
                printResult.errorCode = ErrorCode.ERROR_CANCEL
            }
            // end message
            mHandle.setResult(showResult())
            mHandle.setBattery(batteryDetail)
            msg = mHandle.obtainMessage(Common.MSG_PRINT_END)
            mHandle.sendMessage(msg)

        }
    }

    /**
     * Thread for getting the printer's status
     */
    private inner class SendFileThread : Thread() {
        override fun run() {

            // set info. for printing
            setPrinterInfo()

            // start message
            var msg = mHandle.obtainMessage(Common.MSG_PRINT_START)
            mHandle.sendMessage(msg)

            printResult = PrinterStatus()

            mPrinter!!.startCommunication()

            doPrint()

            mPrinter!!.endCommunication()
            // end message
            mHandle.setResult(showResult())
            mHandle.setBattery(batteryDetail)
            msg = mHandle.obtainMessage(Common.MSG_PRINT_END)
            mHandle.sendMessage(msg)

        }
    }

    companion object {

        internal var mPrinter: Printer? = null
        internal var mCancel: Boolean = false

        fun cancel() {
            if (mPrinter != null)
                mPrinter!!.cancel()
            mCancel = true
        }
    }
}
