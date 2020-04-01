package com.kallibr44.mediawallet

import android.Manifest
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.*
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import io.ktor.client.features.BadResponseStatusException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.json.JSONObject
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.jayway.jsonpath.JsonPath
import kotlinx.android.synthetic.main.tx_item_receive.view.*
import kotlinx.android.synthetic.main.tx_item_send.view.*
import java.lang.Exception
import java.security.Timestamp
import java.time.Instant
import java.util.*
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import kotlinx.android.synthetic.main.activity_tx_full_item.*
import org.json.JSONArray

class RecyclerItemClickListenr(context: Context, recyclerView: androidx.recyclerview.widget.RecyclerView, private val mListener: OnItemClickListener?) : androidx.recyclerview.widget.RecyclerView.OnItemTouchListener {

    private val mGestureDetector: GestureDetector

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)

        fun onItemLongClick(view: View?, position: Int)
    }

    init {

        mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val childView = recyclerView.findChildViewUnder(e.x, e.y)

                if (childView != null && mListener != null) {
                    mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView))
                }
            }

        })
    }

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)

        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
        }

        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}}



val server_address: String = "https://mediacoin.pro/rest"

suspend fun server_ts(): Long?{
        val res = requests().get("$server_address/info")
        val data = JSONObject(res)
        val last_block = JSONObject(data["last_block"].toString())
        return last_block["timestamp"].toString().toLong()

}

class MainActivity : AppCompatActivity() {

    lateinit var mFirebaseAnalytics : FirebaseAnalytics
    val APP_PREFERENCES = "mysettings"
    lateinit var pref: SharedPreferences
    var login: String? = ""
    var password : String? = ""
    var address : String? = ""
    var balance: String? = "0"
    var balance_string: String? = ""
    var last_tx_id : String? = "0"
    var balance_2 : String? = ""
    var isLoading : Boolean = false
    var DataServerKey : String? = ""
    var tx_layout_adapter = HistoryAdapter()
    private lateinit var mInterstitialAd: InterstitialAd

    private val historyViewModel by lazy { ViewModelProviders.of(this).get(HistoryViewModel::class.java) }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Confirmation")
                    .setMessage("\n" +
                            "You really want to exit?")
                    .setCancelable(false)
                    .setNegativeButton("No",
                         { dialog, id -> dialog.cancel() })
                    .setPositiveButton("Yes", {dialog, id -> dialog.cancel();destroy_data()
                        val login_intent = Intent(this@MainActivity, LoginActivity::class.java);startActivity(login_intent);finish()})
                val alert = builder.create()
                alert.show()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                Toast.makeText(this,"Under developing...",Toast.LENGTH_LONG).show()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                Toast.makeText(this,"Under developing...",Toast.LENGTH_LONG).show()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val circularProgressDrawable = CircularProgressDrawable(history_progressbar.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val tx_layout = LinearLayoutManager(this)
        tx_history_list.layoutManager= tx_layout
        tx_history_list.adapter = tx_layout_adapter
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        pref =  getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        //var ad_checker = pref.getInt("ad_checker",0)
        val edit = pref.edit()
        /*
        //Интеграция рекламы в историю транзакций (с версии 3.0 является deprecated)
        mInterstitialAd =  InterstitialAd(this).apply {
            adListener = (object : AdListener() {
                override fun onAdLoaded() {
                    if (ad_checker != 0){if (ad_checker == 3){ad_checker=0} else{ad_checker++}}
                    else{
                    mInterstitialAd.show()
                    ad_checker ++
                    }
                    edit.putInt("ad_checker",ad_checker)
                    edit.apply()
                }

                override fun onAdFailedToLoad(errorCode: Int) {

                }

                override fun onAdClosed() {
                    //none
                }
            })
        }
        mInterstitialAd.setAdUnitId("идентификатор рекламы")
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        */
        //destroy_data()
        try{
            DataServerKey = pref.getString("DataServerKey","")
        }
        catch(e: Exception){}
        login = pref.getString("login","")
        if (login == ""){GlobalScope.launch(Dispatchers.Main) {  val login_intent = Intent(this@MainActivity, LoginActivity::class.java);startActivity(login_intent);finish()}}
        address = pref.getString("address","")
        if (address!!.length < 1){
          GlobalScope.launch(Dispatchers.Main) {get_address_info(); wallet_address.text = address}
        }
        else{
            wallet_address.text = address
        }
        if (balance == null){
            GlobalScope.launch(Dispatchers.Main) {get_address_info_2(); wallet_balance.text = balance_string.toString(); balance_kopeiky.text = ("."+balance_2)}
        }
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        GlobalScope.launch(Dispatchers.Main){isLoading = true;get_tx_history()
            historyViewModel.getListHistory().observe(this@MainActivity, Observer { it?.let {tx_layout_adapter.refreshList(it)} })
            historyViewModel.updateListHistory()
            tx_history_list.addOnItemTouchListener(RecyclerItemClickListenr(this@MainActivity,
                androidx.recyclerview.widget.RecyclerView(this@MainActivity), object : RecyclerItemClickListenr.OnItemClickListener {

                override fun onItemClick(view: View, position: Int) {
                    println("Clicked on "+ position.toString())
                    try{
                       val tx_id = view.tx_id_receive.text
                        println("transaction id: $tx_id")
                        val tx_intent = Intent(this@MainActivity,tx_full_item::class.java)
                        tx_intent.putExtra("tx_id",tx_id)
                        startActivity(tx_intent)
                    }
                    catch(e: Exception) {
                        val tx_id = view.tx_id_send.text
                        println("transaction id: $tx_id")
                        val tx_intent = Intent(this@MainActivity, tx_full_item::class.java)
                        tx_intent.putExtra("tx_id", tx_id)
                        startActivity(tx_intent)
                    }


                }
                override fun onItemLongClick(view: View?, position: Int) {
                    TODO("do nothing")
                }

            }))}
        val scrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val last = tx_layout.findLastVisibleItemPosition()
                val totalItemCount = tx_layout.getItemCount()
                if (!isLoading) {
                    if (last+1 >= totalItemCount) {
                        isLoading = true
                        val lenght = HistoryObject.GetData.size
                        HistoryObject.GetData.add(loader_item("1"))
                        tx_layout_adapter.notifyDataSetChanged()
                        GlobalScope.launch(Dispatchers.Main) { get_tx_history();if(HistoryObject.GetData.size > 1){HistoryObject.GetData.removeAt(lenght);tx_layout_adapter.notifyDataSetChanged()}  }
                    }
                }
            }
        }
        tx_history_list.addOnScrollListener(scrollListener)
        GlobalScope.launch(Dispatchers.Main) {msg_service() }
        mInterstitialAd.show()
    }

    suspend fun msg_service(){
        while(true){
            try{get_address_info_2(); wallet_balance.text = balance_string.toString(); balance_kopeiky.text = ("."+balance_2);msg_service_worker();if(HistoryObject.GetData.size > 1){tx_layout_adapter.notifyDataSetChanged()};delay(5000)}catch(e: IndexOutOfBoundsException){delay(2000)}
        history_progressbar.visibility = View.INVISIBLE
             }
    }

    fun open_receiver(view: View){
        val int = Intent(this,receive_layout::class.java)
        int.putExtra("address",address)
        startActivity(int)
    }
    fun offset_history(element: history_items,index: Int){
        val lenght = HistoryObject.GetData.size
        for(i in index+1..lenght-1){
            HistoryObject.GetData[i] = HistoryObject.GetData[i-1]
        }
        HistoryObject.GetData[index] = element
    }

    suspend fun msg_service_worker(){
        try {
            val ticker = requests().get("https://api.livecoin.net/exchange/ticker?currencyPair=MDC/USD")
            val ticker_data = JSONObject(ticker)
            val mdc_usd_price = ticker_data["last"]
            try{
            converted_balance.text = String.format("~ %.2f $",((balance!!.toLong() / coin_multiplier.Coin) * mdc_usd_price.toString().toFloat()))}
            catch(e: Exception){
                converted_balance.text = "0.00 $"
            }
            if (balance!!.toLong() == 0L){converted_balance.text = "0.00 $"}
            val res = requests().get("$server_address/txs/?address=$address&order=desc&limit=10")
            val blocks : List<Map<String,*>> = JsonPath.read(res, "$.results[*]")
            last_tx_id = "0x"+blocks[0].get("id").toString()
            var counter = 0
            for (i in blocks){
                val tx_id = i.get("id")
                val outs: List<Map<String,*>> = JsonPath.read(i, "$.obj.outs[*]")
                if (i.get("type") == 1){
                    for (j in outs){
                        if (j.get("address") == address){
                            val qq : List<Map<String,*>> = JsonPath.read(i,"$.state[*]")
                            var bal= ""
                            for (jj in qq){if (jj.get("address") == address) {bal = jj.get("balance").toString();break}}
                            val amount = j.get("amount").toString()
                            if (HistoryObject.GetData.contains(history_item(tx_id.toString(),j.get("type").toString(),getDateTime(i.get("block_ts").toString()),amount,bal.toLong())) == false){
                                offset_history(history_item(tx_id.toString(),j.get("type").toString(),getDateTime(i.get("block_ts").toString()),amount,bal.toLong()),counter)}


                        }
                    }
                }
                else{ if (i.get("type")==2){
                    for (j in outs){
                        println("second")
                        val qq : List<Map<String,*>> = JsonPath.read(i,"$.state[*]")
                        var bal= ""
                        for (jj in qq){if (jj.get("address") == address) {bal = jj.get("balance").toString();break}}
                        var check = false
                        if (j.get("to_nick") == login){check = true}
                        val amount = j.get("amount").toString()
                        println("complete!!!")
                        if (check == false){
                            if (HistoryObject.GetData.contains(history_item_send(tx_id.toString(),"@"+j.get("to_nick"),getDateTime(i.get("block_ts").toString()),amount,bal.toLong()))==false){
                                offset_history(history_item_send(tx_id.toString(),"@"+j.get("to_nick"),getDateTime(i.get("block_ts").toString()),amount,bal.toLong()),counter)}

                        }
                        else{
                            if (HistoryObject.GetData.contains(history_item(tx_id.toString(),"@${i.get("sender_nick")}",getDateTime(i.get("block_ts").toString()),amount,bal.toLong()))==false){
                                offset_history(history_item(tx_id.toString(),"@${i.get("sender_nick")}",getDateTime(i.get("block_ts").toString()),amount,bal.toLong()),counter)}
                        }}
                }};counter++
            }
        }
        catch (e: Exception){
            println(e)
            println("err")
        }
        catch (e: BadResponseStatusException){
            println("bad response")
            delay(1000)
        }
        catch (e: java.net.SocketTimeoutException){
            //none
        }
        catch (e:java.net.UnknownHostException){
            //none
        }
        if (HistoryObject.GetData.size > 0){tx_history_list.visibility = View.VISIBLE;empty_tx.visibility = View.INVISIBLE}
    }
    //стереть все данные для проверки логина
    fun destroy_data(){
        val editor = pref.edit()
        editor.putString("login","")
        editor.putString("password","")
        editor.putString("address","")
        editor.apply()
        Toast.makeText(this@MainActivity,"Data cleared!",Toast.LENGTH_LONG).show()
    }

    //функция копирования драеса в буффер обмена
    fun copy_address(view: View){
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", address.toString())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this,"Address copied!",Toast.LENGTH_LONG).show()
    }
    fun refresh_click(view: View){
        GlobalScope.launch(Dispatchers.Main) {
            val circularProgressDrawable = CircularProgressDrawable(history_progressbar.context)
            history_progressbar.visibility = View.VISIBLE
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()
            isLoading = true;get_address_info_2();get_address_info();last_tx_id="0";HistoryObject.GetData.clear();tx_layout_adapter.notifyDataSetChanged();get_tx_history();wallet_balance.text = balance_string.toString(); balance_kopeiky.text = ("."+balance_2);isLoading=false;tx_layout_adapter.notifyDataSetChanged();history_progressbar.visibility = View.INVISIBLE}
    }
    //получение списка истории транзакций (по 10 за запрос)
    suspend fun get_tx_history(){
        var checker_offset: String
        while (true){
        try {
            var mdc_usd_price = "x.xx"
            //println("$server_address/txs/?address=$address&order=desc&limit=10&offset=$last_tx_id")
            val res = requests().get("$server_address/txs/?address=$address&order=desc&limit=10&offset=$last_tx_id")
            val data = JSONObject(res)
            try{
            val ticker = requests().get("https://api.livecoin.net/exchange/ticker?currencyPair=MDC/USD")
            val ticker_data = JSONObject(ticker)
             mdc_usd_price = ticker_data["last"].toString()}
            catch (e: BadResponseStatusException){
                converted_balance.text = "Unavailable"
            }
            checker_offset = data["next_offset"].toString()
            try{
                converted_balance.text = String.format("~ %.2f $",((balance!!.toLong() / coin_multiplier.Coin) * mdc_usd_price.toString().toFloat()))}
            catch(e: Exception){
                if (converted_balance.text == "Unavailable"){
                    //none
                }
                else{
                converted_balance.text = "0.00 $"}
            }
            if (balance!!.toLong() == 0L){converted_balance.text = "0.00 $"}
            val blocks : List<Map<String,*>> = JsonPath.read(res, "$.results[*]")
            last_tx_id = "0x"+blocks[0].get("id").toString()
            for (i in blocks){

                val tx_id = i.get("id")
                val outs: List<Map<String,*>> = JsonPath.read(i, "$.obj.outs[*]")
                if (i.get("type") == 1){
                for (j in outs){
                    if (j.get("address") == address){
                        val qq : List<Map<String,*>> = JsonPath.read(i,"$.state[*]")
                        var bal= ""
                        for (jj in qq){if (jj.get("address") == address) {bal = jj.get("balance").toString();break}}
                        val amount = j.get("amount").toString()
                        if (HistoryObject.GetData.contains(history_item(tx_id.toString(),j.get("type").toString(),getDateTime(i.get("block_ts").toString()),amount,bal.toLong())) == false){
                        HistoryObject.GetData.add(history_item(tx_id.toString(),j.get("type").toString(),getDateTime(i.get("block_ts").toString()),amount,bal.toLong()))}


                    }
                }
                }
                else{ if (i.get("type")==2){
                    println("found!!!!")
                    for (j in outs){
                            println("second")
                            val qq : List<Map<String,*>> = JsonPath.read(i,"$.state[*]")
                            var bal= ""
                            for (jj in qq){if (jj.get("address") == address) {bal = jj.get("balance").toString();break}}
                        var check = false
                        if (j.get("to_nick") == login){check = true}
                        val amount = j.get("amount").toString()
                            println("complete!!!")
                        if (check == false){
                            if (HistoryObject.GetData.contains(history_item_send(tx_id.toString(),"@"+j.get("to_nick"),getDateTime(i.get("block_ts").toString()),amount,bal.toLong()))==false){
                            HistoryObject.GetData.add(history_item_send(tx_id.toString(),"@"+j.get("to_nick"),getDateTime(i.get("block_ts").toString()),amount,bal.toLong()))}

                    }
                    else{
                            if (HistoryObject.GetData.contains(history_item(tx_id.toString(),"@${i.get("sender_nick")}",getDateTime(i.get("block_ts").toString()),amount,bal.toLong()))==false){
                            HistoryObject.GetData.add(history_item(tx_id.toString(),"@${i.get("sender_nick")}",getDateTime(i.get("block_ts").toString()),amount,bal.toLong()))}
                        }}
                }}
            }
            last_tx_id = checker_offset
            isLoading = false
            break
        }
        catch (e: Exception){
            println(e)
        }
        catch (e:java.net.UnknownHostException){
            //none
        }
        catch (e: BadResponseStatusException){
            //none
            println("bad response")
            delay(1000)
        }
            catch (e: java.net.SocketTimeoutException){
                //none
            }
    }
        if (HistoryObject.GetData.size > 0){tx_history_list.visibility = View.VISIBLE;empty_tx.visibility = View.INVISIBLE}
    }
    private fun getDateTime(s: String): String?{
        val res2 = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = Date(s.toLong() / 1000)
        return res2.format(date)
    }
    //получение первичной информации (адреса кошелька, логина)
    suspend fun get_address_info(){
        try{
            val res = requests().get("$server_address/address/?address=@$login")
            val data = JSONObject(res)
            address = data["address"].toString()
            val editor = pref.edit()
            editor.putString("address",address)
            editor.putString("login",login)
            editor.apply()

    }
        catch (e: BadResponseStatusException){
           //nothing
        }
        catch(e: java.net.ConnectException){
            delay(1000)
        }
        catch (e: java.net.SocketTimeoutException){
            //none
        }
        catch (e:java.net.UnknownHostException){
            //none
        }
    }

    fun open_graph(view: View){
        val intt = Intent(this, graphic_daily::class.java)
        intt.putExtra("address",address)
        intt.putExtra("login",login)
        startActivity(intt)
    }
    //получение расширенной инфы по аккаунту (баланс кошелька, время последней операции и т.д.)
    suspend fun get_address_info_2(){
        while (pref.getString("login","") == ""){}
        while (true){
        try {
            val res = requests().get("$server_address/address/?address=$address")
            val data = JSONObject(res)
            balance = data["balance"].toString()
            if (balance!!.length < 2){balance_string = "0"}
            else{
            balance_string = balance!!.substring(0,balance!!.length-9)
            last_tx_id = data["last_tx_id"].toString()
            balance_2 = (balance!!.substring(balance!!.length-9,balance!!.length-7))}
            break
        }
        catch (e: BadResponseStatusException){
            delay(1000)
        }
        catch(e: java.net.ConnectException){
            delay(1000)
        }
        catch (e: java.net.SocketTimeoutException){
            //none
        }
        catch (e:java.net.UnknownHostException){
            //none
        }
    }
    }


override fun onDestroy() {
   moveTaskToBack(true)

    super.onDestroy()

    System.runFinalizersOnExit(true)
    System.exit(0)
  }
}
