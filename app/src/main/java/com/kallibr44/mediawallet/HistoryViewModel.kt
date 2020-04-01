package com.kallibr44.mediawallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HistoryViewModel: ViewModel() {
    var messages_list : MutableLiveData<List<history_items>> = MutableLiveData()

    init{
        messages_list.value = HistoryObject.GetData
    }

    fun getListHistory() = messages_list

    fun updateListHistory(){
        messages_list.value = HistoryObject.GetData
    }
}