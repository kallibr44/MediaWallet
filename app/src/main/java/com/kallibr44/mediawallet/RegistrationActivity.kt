package com.kallibr44.mediawallet

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;

import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Error

class RegistrationActivity : AppCompatActivity() {

    lateinit var pref: SharedPreferences
    val APP_PREFERENCES = "mysettings"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref =  getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        setContentView(R.layout.activity_registration)
        intent.putExtra("completed",false)
    }

    fun registration(view: View){
        GlobalScope.launch(Dispatchers.Main){registration_worker()}
    }

    suspend fun registration_worker(){
        val login = input_login.text
        val password = input_password.text
        val res = requests().get("$server_address/new-user?login=$login&password=$password")
        val data = JSONObject(res)
        try{
        if (data["block_num"].toString().toInt() == 0){
            Toast.makeText(this,"Register success!",Toast.LENGTH_LONG).show()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmation")
                .setMessage("\n" +
                        "Remember your login and password! \n YOUR CREDS IS NOT RECOVARABLE!!!")
                .setCancelable(false)
                .setNegativeButton("OK",
                    DialogInterface.OnClickListener { dialog, id -> dialog.cancel();setResult(1337);finish() })
            val alert = builder.create()
            alert.show()
            pref.edit().putString("login",login.toString()).apply()
            pref.edit().putString("password",password.toString()).apply()
        }
        else{
            if (data["block_num"].toString().toInt() != 0){
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Confirmation")
                    .setMessage("\n" +
                            "Account already exists!")
                    .setCancelable(false)
                    .setNegativeButton("OK",
                        DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                val alert = builder.create()
                alert.show()
            }

        }

    }
        catch (e: Error){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmation")
                .setMessage("\n" +
                        "Account already exists!")
                .setCancelable(false)
                .setNegativeButton("OK",
                    DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(1337)
        finish()
    }
}
