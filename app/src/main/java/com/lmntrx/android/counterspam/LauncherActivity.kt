package com.lmntrx.android.counterspam

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_launcher.*

class LauncherActivity : AppCompatActivity() {

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        val sharedPreferences: SharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val userPin = sharedPreferences.getString("PIN","")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
            if (fingerprintManager.isHardwareDetected){
                startActivity(Intent(this, FingerPrintActivity::class.java))
                finish()
            }else{
                if (userPin == ""){
                    startActivity(Intent(this, CreatePinActivity::class.java))
                    finish()
                }
            }

        }

        pinInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(pin: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!pin.isNullOrEmpty()){
                    if (pin!!.length == 4){
                        if (pin.toString() == userPin){
                            startActivity(Intent(this@LauncherActivity, MainActivity::class.java))
                            finish()
                        }else{
                            Toast.makeText(this@LauncherActivity, "Invalid Pin", Toast.LENGTH_SHORT).show()
                            pinInput.setText("")
                        }
                    }
                }
            }
        })

    }
}
