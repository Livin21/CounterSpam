package com.lmntrx.android.counterspam

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.lmntrx.android.counterspam.messagefetch.AllMessagesActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        allMessagesButton.setOnClickListener {
            launchActivity(MODE_ALL)
        }

        spamMessagesButton.setOnClickListener {
            launchActivity(MODE_SPAM)
        }

        nonSpamMessagesButton.setOnClickListener {
            launchActivity(MODE_NON_SPAM)
        }

    }

    private fun launchActivity(mode: Int) {
            val intent = Intent(this@MainActivity, AllMessagesActivity::class.java)
            intent.putExtra(INTENT_MODE_CHOOSER, mode)
            startActivity(intent)
    }
}
