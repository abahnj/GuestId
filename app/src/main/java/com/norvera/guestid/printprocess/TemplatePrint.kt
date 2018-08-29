/**
 * TemplatePrint for printing
 *
 * @author Brother Industries, Ltd.
 * @version 2.2
 */

package com.norvera.guestid.printprocess

import android.content.Context

import com.brother.ptouch.sdk.PrinterInfo.ErrorCode
import com.brother.ptouch.sdk.printdemo.common.Common
import com.brother.ptouch.sdk.printdemo.common.MsgDialog
import com.brother.ptouch.sdk.printdemo.common.MsgHandle
import com.norvera.guestid.common.Common
import com.norvera.guestid.common.MsgDialog
import com.norvera.guestid.common.MsgHandle

import java.util.ArrayList
import java.util.HashMap

class TemplatePrint(context: Context, mHandle: MsgHandle, mDialog: MsgDialog) : BasePrint(context, mHandle, mDialog) {

    private var mPrintData: ArrayList<HashMap<String, Any>>? = null
    private var mEncoding: String? = null

    /**
     * set print data
     */
    fun setPrintData(list: ArrayList<HashMap<String, Any>>) {

        mPrintData = list
    }

    /**
     * set encode for startPTTPrint
     */
    fun setEncoding(encoding: String) {

        if (encoding.equals(Common.ENCODING_JPN, ignoreCase = true)) {
            mEncoding = "SJIS"
        } else if (encoding.equals(Common.ENCODING_CHN, ignoreCase = true)) {
            mEncoding = "GB18030"
        } else {
            mEncoding = null
        }

    }

    /**
     * do the particular print
     */
    override fun doPrint() {

        val count = mPrintData!!.size
        var mapData: Map<String, Any>
        var printError = false

        var i = 0
        while (i < count && !BasePrint.mCancel) {
            mapData = mPrintData!![i]
            when (Integer.parseInt(mapData[Common.TEMPLATE_REPLACE_TYPE]
                    .toString())) {
                Common.TEMPLATE_REPLACE_TYPE_START // start for the pdz print
                -> {
                    val templateKey = Integer.parseInt(mapData[Common.TEMPLATE_KEY].toString())
                    BasePrint.mPrinter?.startPTTPrint(templateKey, mEncoding)
                }
                Common.TEMPLATE_REPLACE_TYPE_END // end for the pdz print
                -> {
                    printResult = BasePrint.mPrinter.flushPTTPrint()

                    // if error, stop the next print
                    if (printResult.errorCode != ErrorCode.ERROR_NONE) {
                        printError = true
                    }
                }

                Common.TEMPLATE_REPLACE_TYPE_TEXT // replaceText
                -> BasePrint.mPrinter.replaceText(mapData[Common.TEMPLATE_REPLACE_TEXT]
                        .toString())

                Common.TEMPLATE_REPLACE_TYPE_INDEX // replaceTextIndex
                -> BasePrint.mPrinter.replaceTextIndex(
                        mapData[Common.TEMPLATE_REPLACE_TEXT].toString(),
                        Integer.parseInt(mapData[Common.TEMPLATE_OBJECTNAME_INDEX].toString()))

                Common.TEMPLATE_REPLACE_TYPE_NAME // replaceTextName
                -> BasePrint.mPrinter.replaceTextName(
                        mapData[Common.TEMPLATE_REPLACE_TEXT].toString(),
                        mapData[Common.TEMPLATE_OBJECTNAME_INDEX]
                                .toString())

                else -> {
                }
            }

            if (printError) {
                break
            }
            i++
        }
        if (BasePrint.mCancel && ErrorCode.ERROR_NONE == printResult.errorCode) {
            printResult.errorCode = ErrorCode.ERROR_CANCEL
        }
    }

}