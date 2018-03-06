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
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.lmntrx.android.counterspam.*
import com.lmntrx.android.counterspam.classifier.SMS
import kotlinx.android.synthetic.main.activity_all_messages.*
import kotlinx.android.synthetic.main.message_layout.view.*


class AllMessagesActivity : AppCompatActivity() {

    private var MODE = 0

    private val PERMISSIONS_REQUEST_READ_SMS = 0

    private lateinit var filteredMessages: ArrayList<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_messages)

        MODE = intent.getIntExtra(INTENT_MODE_CHOOSER, MODE_ALL)

        if (checkSmsPermission()) {
            loadMessages()

            setUpSearch()
        }




    }

    private fun setUpSearch() {
        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(key: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val modifiedArray: ArrayList<Message> = ArrayList()
                filteredMessages.forEach {
                    if (it.source.toLowerCase().contains(key.toString().toLowerCase()) ||
                            it.content.toLowerCase().contains(key.toString().toLowerCase())){
                        modifiedArray.add(it)
                    }
                }
                if (key.isNullOrEmpty()){
                    allMessagesRecyclerView.swapAdapter(MessageAdapter(this@AllMessagesActivity, filteredMessages, MODE), true)
                }else{
                    allMessagesRecyclerView.swapAdapter(MessageAdapter(this@AllMessagesActivity, modifiedArray, MODE), true)
                }
            }
        })
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
            Log.d("From", "number: ${cursor.getString(1)}")
        }

        cursor.close()

        filteredMessages = classifier.filterMessages(this, MODE, messages)
        showMessages(filteredMessages)

        progressDialog.dismiss()

    }

    private fun showMessages(messages: ArrayList<Message>) {
        allMessagesRecyclerView.layoutManager = LinearLayoutManager(this)
        allMessagesRecyclerView.adapter = MessageAdapter(this, messages, MODE)
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

    class MessageAdapter(private val context: Context, private val messages: ArrayList<Message>, private val mode: Int) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            return ViewHolder(layoutInflater.inflate(R.layout.message_layout, parent, false))
        }

        override fun getItemCount(): Int = messages.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.sourceTextView.text = messages[position].source
            holder.contentTextView.text = messages[position].content
            holder.card.setOnClickListener {
                android.support.v7.app.AlertDialog.Builder(context)
                        .setMessage(messages[position].content)
                        .setTitle(messages[position].source)
                        .show()
            }
            holder.card.setOnLongClickListener {
                if (mode == MODE_SPAM){
                    android.support.v7.app.AlertDialog.Builder(context)
                            .setMessage("Remove this message from spam category?")
                            .setTitle("Not Spam?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                classifier.updateDataSet(context, SMS(messages[position].source, messages[position].content,"ham"))
                                notifyItemRemoved(position)
                                messages.removeAt(position)
                                notifyDataSetChanged()
                                dialog.dismiss()
                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.cancel()
                            }
                            .show()
                }else if (mode ==  MODE_NON_SPAM){
                    android.support.v7.app.AlertDialog.Builder(context)
                            .setMessage("Add this message to spam category?")
                            .setTitle("Spam?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                classifier.updateDataSet(context, SMS(messages[position].source, messages[position].content,"spam"))
                                notifyItemRemoved(position)
                                messages.removeAt(position)
                                notifyDataSetChanged()
                                dialog.dismiss()
                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.cancel()
                            }
                            .show()
                }
                true
            }
        }

        class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
            var sourceTextView: TextView = rootView.messageSourceTextView
            var contentTextView: TextView = rootView.messageContentTextView
            var card: CardView = rootView.cardLayout
        }

    }

    data class Message(val source: String, val content: String)

}