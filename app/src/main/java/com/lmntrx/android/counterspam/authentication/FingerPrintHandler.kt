package com.lmntrx.android.counterspam.authentication


/***
 * Created by Livin Mathew <livin@acoustike.com> on 6/3/18.
 */

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.lmntrx.android.counterspam.MainActivity
import com.lmntrx.android.counterspam.R


@RequiresApi(Build.VERSION_CODES.M)
class FingerprintHandler(private val context: Context) : FingerprintManager.AuthenticationCallback() {


    fun startAuth(manager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject) {
        val cancellationSignal = CancellationSignal()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    }


    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        this.update("Fingerprint Authentication error\n$errString", false)
    }


    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
        this.update("Fingerprint Authentication help\n$helpString", false)
    }


    override fun onAuthenticationFailed() {
        this.update("Fingerprint Authentication failed.", false)
    }


    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        this.update("Fingerprint Authentication succeeded.", true)
    }


    private fun update(e: String, success: Boolean) {
        val textView = (context as Activity).findViewById<View>(R.id.errorText) as TextView
        textView.text = e
        if (success) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
            context.startActivity(Intent(context, MainActivity::class.java))
            context.finish()
        }
    }
}