package com.kallibr44.mediawallet

interface history_items
data class history_item(var tx_id : String?, var tx_type : String?, var date: String?, var amount: String?, var balance: Long?) : history_items
data class history_item_send(var tx_id : String?, var tx_type : String?, var date: String?, var amount: String?, var balance: Long?) : history_items
data class loader_item(var tx_id: String?) : history_items