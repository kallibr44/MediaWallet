package com.kallibr44.mediawallet

import androidx.annotation.WorkerThread
import io.ktor.client.HttpClient
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import org.json.JSONObject


/*
Самописная библиотека для приведения веб запросов к формату Python


*/
class requests {

    @WorkerThread
    suspend fun get(url: String) : String{
        val client = HttpClient()

        // Get the content of an URL.
        val result = client.get<String>(url)


        client.close()
        return result
    }

    @WorkerThread
    suspend fun post(url: String, data: JSONObject): String {
        val client = HttpClient()
        try {
            val result = client.post<String> {
                url(url)
                this.body = data.toString()
            }
            client.close()
            return result
        }
        catch (e: BadResponseStatusException){
            return "error"
        }
        }



}

