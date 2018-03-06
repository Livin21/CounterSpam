package com.lmntrx.android.counterspam.classifier

import android.content.Context
import android.util.Log
import com.lmntrx.android.counterspam.R
import java.io.BufferedReader
import java.io.InputStreamReader


/***
 * Created by Livin Mathew <livin@acoustike.com> on 6/3/18.
 */

class Classifier{

    private val trainData: ArrayList<SMS> = ArrayList()

    var pA = 0f
    var pNotA = 0f

    private val trainPositive: HashMap<String, Int> = HashMap()
    private val trainNegative: HashMap<String, Int> = HashMap()

    var positiveTotal = 0
    var negativeTotal = 0

    fun initialize(context: Context){
        readDataSet(context)
    }

    private fun readDataSet(context: Context){
        val reader = BufferedReader(InputStreamReader(context.resources.openRawResource(R.raw.english_big)))
        var line = reader.readLine()
        while (line != null){
            trainData.add(SMS("", line.split("-> ")[0], line.split("-> ")[1]))
            line = reader.readLine()
        }
        train()
    }

    private fun train(){
        var total = 0
        var numSpam = 0
        for (sms in trainData){
            if (sms.label == "spam")
                numSpam += 1
            total += 1
            processSms(sms.body, sms.label)
        }
        pA = numSpam / total.toFloat()
        pNotA = (total - numSpam) / total.toFloat()
    }

    private fun processSms(body: String, label: String){
        for (word in body.split(" "))
            if (label == "spam"){
                trainPositive[word] = if (trainPositive[word] == null) 1 else trainPositive[word]!!.plus(1)
                positiveTotal += 1
            }else{
                trainNegative[word] = if (trainNegative[word] == null) 1 else trainNegative[word]!!.plus(1)
                negativeTotal += 1
            }
    }

    private fun conditionalWord(word: String, spam: Boolean): Double? {
        //val correctedValue = if (trainPositive[word] == null) 1 else trainPositive[word]!!
        return if (spam) trainPositive[word]?.div(positiveTotal.toDouble()) else trainPositive[word]?.div(negativeTotal.toDouble())
    }

    private fun conditionalSms(body: String, spam: Boolean): Double {
        var result = 1.0
        for (word in body.split(" "))
            result *= conditionalWord(word, spam)?:1.0
        return result
    }

    fun classify(sms: SMS): Boolean{
        val isSpam = pA * conditionalSms(sms.body, true) // P (A | B)
        val notSpam = pNotA * conditionalSms(sms.body, false) // P (¬A | B)
        Log.d("SMS", sms.body)
        Log.d("spamCount", isSpam.toString())
        Log.d("notSpamCount", notSpam.toString())
        return isSpam > notSpam
    }
}
