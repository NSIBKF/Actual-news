package com.example.myfirstapp

import io.reactivex.Observable
import java.lang.RuntimeException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

fun createRequest(url:String) = Observable.create<String>{
    val urlConnection = URL(url).openConnection() as HttpsURLConnection
    try{
        urlConnection.connect()
        if(urlConnection.responseCode != HttpsURLConnection.HTTP_OK)
            it.onError(RuntimeException(urlConnection.responseMessage))
        else{
            val str = urlConnection.inputStream.bufferedReader().readText()
            it.onNext(str)
        }
    } finally {
        urlConnection.disconnect()
    }
}