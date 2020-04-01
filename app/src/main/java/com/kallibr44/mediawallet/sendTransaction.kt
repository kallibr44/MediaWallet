package com.kallibr44.mediawallet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_send_transaction.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class sendTransaction : AppCompatActivity() {

    var login = ""
    var password = ""
    var balance  = 0L
    var scannedResult =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_transaction)
        login = intent.getStringExtra("login")
        password = intent.getStringExtra("password")
        balance = intent.getLongExtra("balance",0L)
        error.visibility = View.INVISIBLE
        amount.hint = "Avaliable: %.8f MDC".format((balance / coin_multiplier.Coin).toString().toFloat())
        amount.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (amount.text.toString().toLong() > balance){
                    error.visibility = View.VISIBLE
                    error.text = "Enough balance"
                    send_btn.isClickable = false

                }
                else{send_btn.isClickable = true;error.visibility = View.INVISIBLE}
            }
        })

    }

    fun send_tx(view: View){
        GlobalScope.launch(Dispatchers.Main) {
          if (comment.text.isEmpty()){
              val res = requests().get("$server_address/new-transfer?login=$login&password=$password&address=${reciever_address.text}&amount=${amount.text.toString().toLong() * coin_multiplier.Coin}")
              Toast.makeText(this@sendTransaction,"Coin sent!",Toast.LENGTH_LONG).show()
              finish()
          }
            else{
              val res = requests().get("$server_address/new-transfer?login=$login&password=$password&address=${reciever_address.text}&comment=${comment.text}&amount=${amount.text.toString().toLong() * coin_multiplier.Coin}")
              Toast.makeText(this@sendTransaction,"Coin sent!",Toast.LENGTH_LONG).show()
              finish()
          }

        }
    }

    fun scan_code(view: View){
        IntentIntegrator(this@sendTransaction).initiateScan()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        var result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if(result != null){

            if(result.contents != null){
                scannedResult = result.contents
                reciever_address.setText(scannedResult)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}
