package com.yooking.websocket

import android.os.Handler
import android.os.Message
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocketListener
import okhttp3.internal.ws.RealWebSocket
import okhttp3.logging.HttpLoggingInterceptor
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit


/**
 * create by yooking on 2019/8/10
 */
class WebSocketUtils(private val url: String, private val listener: WebSocketListener) {

    companion object {
        private const val DEFAULT_HEART_TIME: Long = 60//默认心跳时间

        private const val TAG: String = "WebSocketLog"//日志抬头
        private const val READ_TIMEOUT: Long = 30//读取超时
        private const val WRITE_TIMEOUT: Long = 30//写入超时
        private const val CONNECT_TIMEOUT: Long = 30//连接超时

        private const val WHAT_HEART: Int = 0//handler-what

        private class WithoutLeakHandler(wsu: WebSocketUtils) : Handler() {
            private val mWsu: WeakReference<WebSocketUtils> = WeakReference(wsu)
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                val webSocketUtils = mWsu.get()!!
                webSocketUtils.send(webSocketUtils.heartMessage)
                msg!!.target.sendEmptyMessageDelayed(WHAT_HEART, webSocketUtils.heartTime)
            }
        }
    }

    private var handler: Handler = WithoutLeakHandler(this)

    private var webSocket: RealWebSocket

    private val okHttpClient: OkHttpClient


    var heartMessage: String = ""//心跳消息
    var heartTime: Long = DEFAULT_HEART_TIME * 1000//心跳时间，默认1分钟


    init {
        //log日志
        val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.d(TAG, message)
            }
        })
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient = OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)//设置连接超时时间
            .retryOnConnectionFailure(true)
            .addInterceptor(interceptor)
            .build()
        val request: Request = Request.Builder().url(url).build()
        //创建webSocket
        webSocket = okHttpClient.newWebSocket(request, listener) as RealWebSocket

//        handler = Handler {
//            send(heartMessage)//handler引用外部方法，可能造成内存泄漏
//            it.target.sendEmptyMessageDelayed(WHAT_HEART, heartTime)
//        }
    }

    //发送方法
    fun send(message: String) {
        val isSend = webSocket.send(message)
        Log.d(TAG, "send-->isSend:$isSend message:$message")
    }

    fun reConnect(url: String) {
        webSocket.cancel()
        val request: Request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, listener) as RealWebSocket
    }

    fun reConnect() {
        reConnect(url)
    }

    fun cancel() {
        webSocket.cancel()
    }

    fun startHeart(heartMessage: String, current: Long?) {
        this.heartMessage = heartMessage
        heartTime = current!!
        handler.sendEmptyMessage(WHAT_HEART)
    }

    fun endHeart() {
        handler.removeMessages(WHAT_HEART)
    }

}