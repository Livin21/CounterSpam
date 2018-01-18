package com.lmntrx.android.counterspam.messagefetch

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.lmntrx.android.counterspam.INTENT_MODE_CHOOSER
import com.lmntrx.android.counterspam.MODE_ALL
import com.lmntrx.android.counterspam.R
import kotlinx.android.synthetic.main.activity_all_messages.*
import kotlinx.android.synthetic.main.message_layout.view.*


class AllMessagesActivity : AppCompatActivity() {

    private var MODE = 0

    private val PERMISSIONS_REQUEST_READ_SMS = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_messages)

        MODE = intent.getIntExtra(INTENT_MODE_CHOOSER, MODE_ALL)

        if (checkSmsPermission()){
            loadMessages()
        }


    }

    private fun loadMessages() {

        val messages = ArrayList<Message>()

        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Please Wait...")
        progressDialog.show()

        val uriSms = Uri.parse("content://sms/inbox")
        val cursor = contentResolver.query(uriSms,
                arrayOf("_id", "address", "date", "body"), null, null, null)

        while (cursor.moveToNext()) {
            messages.add(Message(cursor.getString(1), cursor.getString(3)))
            Log.d("From","number: ${cursor.getString(1)}")
        }

        cursor.close()

        showMessages(messages)

        progressDialog.dismiss()

    }

    private fun showMessages(messages: ArrayList<Message>) {
        allMessagesRecyclerView.layoutManager = LinearLayoutManager(this)
        allMessagesRecyclerView.adapter = MessageAdapter(this, messages)
    }

    private fun checkSmsPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.READ_SMS)) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.READ_SMS),
                        PERMISSIONS_REQUEST_READ_SMS)
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.READ_SMS),
                        PERMISSIONS_REQUEST_READ_SMS)
            }
            return false
        } else {
            return true
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {


        when (requestCode) {
            PERMISSIONS_REQUEST_READ_SMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
                                android.Manifest.permission.READ_SMS)
                        == PackageManager.PERMISSION_GRANTED) {

                    loadMessages()

                    return
                }
            } else {
                Toast.makeText(applicationContext, "permission denied", Toast.LENGTH_LONG).show()
            }
                return
            }
        }

    }

    class MessageAdapter(private val context: Context, private val messages: ArrayList<Message>): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder{
            val layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            return ViewHolder(layoutInflater.inflate(R.layout.message_layout, parent,false))
        }

        override fun getItemCount(): Int = messages.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.sourceTextView.text = messages[position].source
            holder.contentTextView.text = messages[position].content
        }

        class ViewHolder(rootView: View): RecyclerView.ViewHolder(rootView) {
            var sourceTextView: TextView = rootView.messageSourceTextView
            var contentTextView: TextView = rootView.messageContentTextView
        }

    }

    data class Message(val source: String, val content: String)

}
