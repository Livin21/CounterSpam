package com.lmntrx.android.counterspam.classifier

import android.content.Context
import android.util.Log
import com.lmntrx.android.counterspam.*
import com.lmntrx.android.counterspam.messagefetch.AllMessagesActivity
import java.io.BufferedReader
import java.io.FileNotFoundException
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

    fun filterMessages(context: Context, mode: Int, messages: ArrayList<AllMessagesActivity.Message>): ArrayList<AllMessagesActivity.Message> =
            when (mode){
                MODE_SPAM -> {
                    val modifiedArray = ArrayList<AllMessagesActivity.Message>()
                    messages.forEach{
                        if (classify(SMS(it.source,it.content,""))){
                            modifiedArray.add(it)
                        }
                    }
                    fineTuneSpamSet(context, modifiedArray)
                }
                MODE_NON_SPAM -> {
                    val modifiedArray = ArrayList<AllMessagesActivity.Message>()
                    messages.forEach{
                        if (!classify(SMS(it.source,it.content,""))){
                            if (!it.source.toLowerCase().contains("wappush"))
                                modifiedArray.add(it)
                        }
                    }
                    fineTuneHamSet(context, modifiedArray)
                }
                MODE_OTP -> {
                    val modifiedArray = ArrayList<AllMessagesActivity.Message>()
                    messages.forEach{
                        if (
                                it.content.toLowerCase().contains("otp") ||
                                it.content.toLowerCase().contains("one time password") ||
                                it.content.toLowerCase().contains("verification code") ||
                                it.content.toLowerCase().contains("whatsapp code") ||
                                it.content.toLowerCase().contains("telegram code") ||
                                it.content.toLowerCase().contains("messenger code") ||
                                it.content.toLowerCase().contains("connection code") ||
                                it.content.toLowerCase().contains("registration code") ||
                                it.content.toLowerCase().contains("reset code")
                        ){
                            modifiedArray.add(it)
                        }
                    }
                    modifiedArray
                }
                MODE_MISSED_CALL -> {
                    val modifiedArray = ArrayList<AllMessagesActivity.Message>()
                    messages.forEach{
                        if (
                                it.content.toLowerCase().contains("missed call")
                        ){
                            modifiedArray.add(it)
                        }
                    }
                    modifiedArray
                }
                else -> {
                    messages
                }
            }

    fun updateDataSet(context: Context, sms: SMS) {
        val delimiter = "::->"
        if (sms.label == "spam"){
            try{
                val spamFile =  context.openFileInput("spam")
                var spamData = ""
                val spamReader = spamFile.bufferedReader()
                spamReader.readLines().forEach {
                    spamData += "$it\n"
                }
                spamData += "${sms.source} $delimiter ${sms.body}"
                Log.d("spam msg", spamData)
                context.openFileOutput("spam", Context.MODE_PRIVATE).use {
                    it.write(spamData.toByteArray())
                }
            }catch (e: FileNotFoundException){
                val spamData = "${sms.source} $delimiter ${sms.body}"
                context.openFileOutput("spam", Context.MODE_PRIVATE).use {
                    it.write(spamData.toByteArray())
                }
            }
        }else{
                try{
                    val hamFile =  context.openFileInput("ham")
                    var hamData = ""
                    val hamReader = hamFile.bufferedReader()
                    hamReader.readLines().forEach {
                        hamData += "$it\n"
                    }
                    hamData += "${sms.source} $delimiter ${sms.body}"
                    Log.d("spam msg", hamData)
                    context.openFileOutput("ham", Context.MODE_PRIVATE).use {
                        it.write(hamData.toByteArray())
                    }
                }catch (e: FileNotFoundException){
                    val hamData = "${sms.source} $delimiter ${sms.body}"
                    context.openFileOutput("ham", Context.MODE_PRIVATE).use {
                        it.write(hamData.toByteArray())
                    }
                }
        }

    }

    private fun getUserSpamDataSet(context: Context): ArrayList<String>{
        val userDefinedSpamDataArray = ArrayList<String>()
        return try{
            val spamFile =  context.openFileInput("spam")
            val spamReader = spamFile.bufferedReader()
            spamReader.readLines().forEach {
                userDefinedSpamDataArray.add(it)
            }
            userDefinedSpamDataArray
        }catch (e: FileNotFoundException){
            userDefinedSpamDataArray
        }
    }

    private fun getUserHamDataSet(context: Context): ArrayList<String>{
        val userDefinedHamDataArray = ArrayList<String>()
        return try{
            val hamFile =  context.openFileInput("ham")
            val hamReader = hamFile.bufferedReader()
            hamReader.readLines().forEach {
                userDefinedHamDataArray.add(it)
            }
            userDefinedHamDataArray
        }catch (e: FileNotFoundException){
            userDefinedHamDataArray
        }
    }

    private fun fineTuneSpamSet(context: Context, modifiedArray: ArrayList<AllMessagesActivity.Message>): ArrayList<AllMessagesActivity.Message> {
        val delimiter = "::->"
        val userDefinedSpam = getUserSpamDataSet(context)
        if (!userDefinedSpam.isEmpty()){
            userDefinedSpam.forEach {
                Log.d("spam msg", it)
                val spamMessage: AllMessagesActivity.Message = try {
                    AllMessagesActivity.Message(it.split(" $delimiter ")[0], it.split(" $delimiter ")[1])
                }catch (e: IndexOutOfBoundsException){
                    AllMessagesActivity.Message("Unknown",it)
                }
                if (!modifiedArray.contains(spamMessage)){
                    modifiedArray.add(spamMessage)
                }
            }
        }
        val userDefinedHam = getUserHamDataSet(context)
        if (!userDefinedHam.isEmpty()){
            userDefinedHam.forEach {
                Log.d("spam msg", it)
                val hamMessage: AllMessagesActivity.Message = try {
                    AllMessagesActivity.Message(it.split(" $delimiter ")[0], it.split(" $delimiter ")[1])
                }catch (e: IndexOutOfBoundsException){
                    AllMessagesActivity.Message("Unknown",it)
                }
                if (modifiedArray.contains(hamMessage)){
                    modifiedArray.remove(hamMessage)
                }
            }
        }
        return modifiedArray
    }

    private fun fineTuneHamSet(context: Context, modifiedArray: ArrayList<AllMessagesActivity.Message>): ArrayList<AllMessagesActivity.Message> {
        val delimiter = "::->"
        val userDefinedSpam = getUserSpamDataSet(context)
        if (!userDefinedSpam.isEmpty()){
            userDefinedSpam.forEach {
                Log.d("spam msg", it)
                val spamMessage: AllMessagesActivity.Message = try {
                    AllMessagesActivity.Message(it.split(" $delimiter ")[0], it.split(" $delimiter ")[1])
                }catch (e: IndexOutOfBoundsException){
                    AllMessagesActivity.Message("Unknown",it)
                }
                if (modifiedArray.contains(spamMessage)){
                    modifiedArray.remove(spamMessage)
                }
            }
        }
        val userDefinedHam = getUserHamDataSet(context)
        if (!userDefinedHam.isEmpty()){
            userDefinedHam.forEach {
                Log.d("spam msg", it)
                val hamMessage: AllMessagesActivity.Message = try {
                    AllMessagesActivity.Message(it.split(" $delimiter ")[0], it.split(" $delimiter ")[1])
                }catch (e: IndexOutOfBoundsException){
                    AllMessagesActivity.Message("Unknown",it)
                }
                if (!modifiedArray.contains(hamMessage)){
                    modifiedArray.add(hamMessage)
                }
            }
        }
        return modifiedArray
    }
}
