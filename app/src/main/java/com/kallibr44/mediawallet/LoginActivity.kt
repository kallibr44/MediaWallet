package com.kallibr44.mediawallet

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import io.ktor.client.features.BadResponseStatusException
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.R.string.cancel
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.jayway.jsonpath.JsonPath
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import io.ktor.client.HttpClient
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import java.lang.Exception
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.content.*
import java.nio.file.Files.exists
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.os.Environment
import java.io.File
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.opengl.Visibility
import android.view.ActionProvider
import android.widget.ProgressBar
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.microsoft.appcenter.distribute.Distribute
import kotlinx.android.synthetic.main.activity_main.*


class LoginActivity : AppCompatActivity() {

    val APP_PREFERENCES = "mysettings"
    val server_address: String = "https://mediacoin.pro/rest"
    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Distribute.setListener(MyDistributeListener())
        Distribute.setEnabled(true)
        Distribute.setEnabledForDebuggableBuild(true)
        //Инициализация AppCenter https://appcenter.ms/
        AppCenter.start(getApplication(), "a04f4e97-7bf7-4e13-b0a5-9f9aebdc8799", Analytics::class.java, Crashes::class.java)
        pref =  getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        intent.putExtra("login","")
        if (pref.getString("login","") != "" && pref.getString("login","") != null){val main = Intent(this,MainActivity::class.java); startActivity(main)}
        else{
        setContentView(R.layout.activity_login)
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Denial of responsibility")
                .setMessage("\n" +
                        "This application is unofficial and developed by third party developers. MediaCoin has no relation to this application and is not responsible for the consequences of using this software.")
                .setCancelable(false)
                .setNegativeButton("OK, i am understand.",
                    DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert = builder.create()
            alert.show()}
    }

    fun check_login(view: View){
        var login = ""
        if (input_login.text.startsWith("@")){login = input_login.text.substring(1)}
        else{login = input_login.text.toString()}
        login_progress.visibility = View.VISIBLE
        val circularProgressDrawable = CircularProgressDrawable(login_progress.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        progress_layout.visibility = View.VISIBLE
        LoginLayout.alpha = 0.2f
        GlobalScope.launch(Dispatchers.Main) {
            try{
                val res = requests().get("$server_address/address/@$login")
                val data = JSONObject(res)
                val adress = data["address"]
                pref.edit().putString("login",login).apply()
                Toast.makeText(this@LoginActivity,"Login successful!", Toast.LENGTH_LONG).show()
                start_main()
                finish()

            }
            catch (e: BadResponseStatusException){
                Toast.makeText(this@LoginActivity,"Incorrect Login", Toast.LENGTH_LONG).show()
                println(e)
            }
            catch (e: Exception){
                Toast.makeText(this@LoginActivity, "Unknown error. Please try again.", Toast.LENGTH_LONG).show()
                println(e)
            }
            catch (e: java.net.SocketTimeoutException){
                //none
            }
            catch (e:java.net.UnknownHostException){
                //none
            }
            LoginLayout.alpha = 1f
            circularProgressDrawable.stop()
            progress_layout.visibility = View.INVISIBLE
        }
    }

    fun make_register(view: View){
        val reg_intent = Intent(this,RegistrationActivity::class.java)
        startActivityForResult(reg_intent, 1337)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 1337){
        GlobalScope.launch (Dispatchers.Main){ start_main() }}
    }
    suspend fun start_main(){
        val main = Intent(this@LoginActivity,MainActivity::class.java); startActivity(main);finish()
    }
}
