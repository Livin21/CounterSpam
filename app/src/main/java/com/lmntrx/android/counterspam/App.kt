package com.lmntrx.android.counterspam

import android.app.Application
import com.lmntrx.android.counterspam.classifier.Classifier

/***
 * Created by Livin Mathew <livin@acoustike.com> on 18/1/18.
 */

val MODE_ALL = 0
val MODE_SPAM = 1
val MODE_NON_SPAM = 2
val MODE_OTP = 3
val MODE_MISSED_CALL = 4

val INTENT_MODE_CHOOSER = "CHOSEN_MODE"

lateinit var classifier: Classifier

class App: Application(){
    override fun onCreate() {
        super.onCreate()
        classifier = Classifier()
        classifier.initialize(this)
    }
}