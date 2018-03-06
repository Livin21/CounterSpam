package com.lmntrx.android.counterspam.authentication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.lmntrx.android.counterspam.MainActivity
import com.lmntrx.android.counterspam.R
import kotlinx.android.synthetic.main.activity_create_pin.*

class CreatePinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pin)


        createPinButton.setOnClickListener {
            val pin = pinEntryField.text.toString()
            val confirmedPin = pinConfirmField.text.toString()
            if (pin.isEmpty()){
                Toast.makeText(this, "Pin cannot be empty", Toast.LENGTH_SHORT).show()
            }else{
                if (confirmedPin.isEmpty()){
                    Toast.makeText(this, "Please confirm entered pin", Toast.LENGTH_SHORT).show()
                }else{
                    if (pin.length != 4){
                        Toast.makeText(this, "Pin should contain 4 digits", Toast.LENGTH_SHORT).show()
                    }else{
                        val sharedPreferences: SharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
                        if (pin == confirmedPin){
                            Toast.makeText(this, "Pin Saved", Toast.LENGTH_SHORT).show()
                            sharedPreferences.edit().putString("PIN",pin).apply()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }else{
                            Toast.makeText(this, "Pins doesn't match", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    }
}
