package com.noisefit_commans.utils

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Inject

class ConnectionUtil
@Inject
constructor() {

    /*@Throws(InterruptedException::class, IOException::class)
    fun isConnected(): Boolean {
        val command = "ping -c 1 google.com"
        return Runtime.getRuntime().exec(command).waitFor() == 0
    }*/

    fun getIPAddress(): String {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr: String = addr.hostAddress.uppercase()
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4) {
                            return sAddr
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getPublicIPAddress(): String? {
        var value: String? = null
        val es: ExecutorService = Executors.newSingleThreadExecutor()
        val result: Future<String> = es.submit(object : Callable<String> {
            @Throws(Exception::class)
            override fun call(): String? {
                try {
                    val url = URL("http://whatismyip.akamai.com/")
                    val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    return try {
                        val `in`: InputStream = BufferedInputStream(urlConnection.inputStream)
                        val r = BufferedReader(InputStreamReader(`in`))
                        val total = StringBuilder()
                        var line: String?
                        while (r.readLine().also { line = it } != null) {
                            total.append(line)
                        }
                        urlConnection.disconnect()
                        total.toString()
                    } finally {
                        urlConnection.disconnect()
                    }
                } catch (e: Exception) {
                    //Log.e("Public IP: ", e.getMessage())
                }
                return null
            }
        })
        try {
            value = result.get()
        } catch (e: Exception) {
            // failed
        }
        es.shutdown()
        return value
    }
}