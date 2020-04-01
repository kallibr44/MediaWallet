package com.kallibr44.mediawallet

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.tx_item_receive.view.*
import kotlinx.android.synthetic.main.tx_item_receive.view.summa_tx
import kotlinx.android.synthetic.main.tx_item_receive.view.tx_date
import kotlinx.android.synthetic.main.tx_item_send.view.*
import java.lang.Exception


class HistoryAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        val TYPE_RECEIVE = 0
        val TYPE_SEND = 1
        val TYPE_LOAD = 2
    }

    private var history: List<history_items> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_RECEIVE -> ReceiveHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.tx_item_receive, parent, false))
        TYPE_SEND -> SendHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.tx_item_send, parent, false))
        TYPE_LOAD -> LoaderHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.history_load_item, parent, false))
        else -> throw IllegalArgumentException()
    }

    override fun getItemViewType(position: Int): Int =
        when (history[position]) {
            is history_item -> TYPE_RECEIVE
            is history_item_send -> TYPE_SEND
            is loader_item -> TYPE_LOAD
            else -> throw IllegalArgumentException()
        }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int)= when (viewHolder.itemViewType) {
        TYPE_RECEIVE -> onBindReceive(viewHolder, history[position] as history_item)
        TYPE_SEND -> onBindSend(viewHolder, history[position] as history_item_send)
        TYPE_LOAD -> onLoadBind(viewHolder, history[position] as loader_item)
        else -> throw IllegalArgumentException()
    }

    override fun getItemCount() = history.size

    fun refreshList(messages: List<history_items>){
        this.history = messages
        notifyDataSetChanged()
    }


    private fun onLoadBind(holder: RecyclerView.ViewHolder, row: loader_item){
        val loadHolder = holder as LoaderHolder
        loadHolder.bind(row)
    }

    private fun onBindSend(holder: RecyclerView.ViewHolder, row: history_item_send){
        val sendholder = holder as SendHolder
        sendholder.bind(row)
    }
    private fun onBindReceive(holder: RecyclerView.ViewHolder, row: history_item) {
        val receiveholder = holder as ReceiveHolder
        receiveholder.bind(row)
    }


    class LoaderHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(history: loader_item) = with(itemView){

        }
    }

    class ReceiveHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(history: history_item) = with(itemView){
            //tx_id.text = history.tx_id
            println("RECEIVE")
            tx_id_receive.text = "0x"+history.tx_id.toString()
            tx_date.text = history.date.toString()
            if (history.tx_type == "1"){tx_username_sender.text = transaction_type.one}
            else{if (history.tx_type == "2"){tx_username_sender.text = transaction_type.two}
            else{if (history.tx_type == "3"){tx_username_sender.text = transaction_type.three}
            else{tx_username_sender.text = history.tx_type}
            }
            }

            var balance = history.amount!!.toString()
            var balance_string = ""
            try{
                balance_string = balance.substring(0,balance.length-9)}
            catch(e:Exception){
                while (balance.length < 10){
                    balance = "0"+balance
                }
                balance_string = balance.substring(0,balance.length-9)
            }
            if (balance_string.isEmpty()){balance_string = "0"}
            val balance_2 = balance.substring(balance.length-9,balance.length-7)
            summa_tx.text = balance_string
            summa_tx_kopeyki_receive.text = "."+balance_2
        }
    }

    class SendHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(history: history_item_send) = with(itemView){
            //tx_id.text = history.tx_id
            println("SEND")
            tx_id_send.text = "0x"+history.tx_id.toString()
            tx_date.text = history.date.toString()
            tx_username_receiver.text = history.tx_type
            var balance = history.amount!!.toString()
            var balance_string = ""
            try{
                balance_string = balance.substring(0,balance.length-9)}
            catch(e:Exception){
                while (balance.length < 10){
                    balance = "0"+balance
                }
                balance_string = balance.substring(0,balance.length-9)
            }
            if (balance_string.isEmpty()){balance_string = "0"}
            val balance_2 = balance.substring(balance.length-9,balance.length-7)
            summa_tx.text = balance_string
            summa_tx_kopeyki_send.text = "."+balance_2
        }
    }
}