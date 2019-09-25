package com.yooking.socket

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.yooking.websocket.WebSocketUtils
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ChatActivity : AppCompatActivity() {

    //静态全局变量
    companion object {
        //ChatActivity.class.getName()的写法
        private const val TAG: String = "WebSocketLog"
    }

    private var handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            val data = ChatData(ChatAdapter.TYPE_LEFT)
            data.name = msg?.data?.getString("name")
            data.content = msg?.data?.getString("message")
            data.time = msg?.data?.getString("time")
            chatAdapter.addData(data)
        }
    }

    private lateinit var webSocketUtils: WebSocketUtils

    private lateinit var mContext: Context

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var tvSend: AppCompatTextView
    //lateinit不可修饰val
//    private lateinit val etMessage: AppCompatEditText
    private lateinit var etMessage: AppCompatEditText
    //lateinit不可修饰基础数据类型
//    private lateinit var mInt:Int

    private lateinit var friendsName: String
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        mContext = this
        userName = intent.getStringExtra("userName")
        friendsName = intent.getStringExtra("friendsName")
        //设置标题栏为toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)//利用Toolbar代替ActionBar
        //设置标题
        supportActionBar?.title = "$userName->$friendsName:五毛不用找"
        //findViewById
        findViews()
        //定义RecyclerView
        initRecyclerView()
        //listener监听
        initListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun findViews() {
        recyclerView = findViewById(R.id.rv_chat_content)
        tvSend = findViewById(R.id.actv_chat_send)
        etMessage = findViewById(R.id.acet_chat_content)
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        chatAdapter = ChatAdapter(defaultItem())
        chatAdapter.bindToRecyclerView(recyclerView)
    }

    private fun initListener() {
        tvSend.setOnClickListener {
            val message: String = etMessage.text.toString()
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(mContext, "不能发送空消息", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val chatData = ChatData(ChatAdapter.TYPE_RIGHT)
            chatData.name = "$userName->$friendsName"
            chatData.content = message
            chatData.time = format(Date())

            val params = HashMap<String, Any>()
            params["seqId"] = Date().time
            params["from"] = chatData.name!!
            params["to"] = friendsName
            params["cmd"] = 11
            params["createTime"] = Date().time
            params["chatType"] = 2
            params["msgType"] = "0"
            params["content"] = message

            chatAdapter.addData(chatData)

            webSocketUtils.send(Gson().toJson(params))
            etMessage.setText("")
        }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_chat_user -> {
                    val editText = AppCompatEditText(mContext)
                    AlertDialog.Builder(mContext)
                        .setTitle("切换Id")
                        .setView(editText)
                        .setNegativeButton(
                            "确定"
                        ) { _, _ -> changeUser(editText.text.toString()) }
                        .create().show()
                }
                R.id.action_chat_friend -> {
                    val editText = AppCompatEditText(mContext)
                    AlertDialog.Builder(mContext)
                        .setTitle("切换好友Id")
                        .setView(editText)
                        .setNegativeButton(
                            "确定"
                        ) { _, _ -> changeFriend(editText.text.toString()) }
                        .create().show()
                }
                R.id.action_chat_about -> {
                    Toast.makeText(mContext, "关于", Toast.LENGTH_LONG).show()
                }
            }
            return@setOnMenuItemClickListener false
        }

        webSocketUtils = WebSocketUtils(
            getUrl(userName),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    Log.d(TAG, "onOpen")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    Log.d(TAG, "onMessage-->text:$text")

                    val d: Map<String, Any> = Gson().fromJson<Map<String, Any>>(text, Map::class.java)
                    if (d.containsKey("command")) {
                        when ((d["command"] as Double).toInt()) {
                            6 -> return //消息发送结果反馈
                            12 -> return //登录成功反馈
                            13 -> return //心跳成功反馈
                        }
                    }
                    val params: Map<*, *> = d["data"] as Map<*, *>
                    val message = Message()
                    message.data.putString("message", params["content"] as String?)
                    val time: Long = (params["createTime"] as Double?)!!.toLong()
                    message.data.putString("time", format(Date(time)))
                    val name: String = params["from"] as String
                    if (friendsName == name) {//判断是否为对方发送，是则接收
                        message.data.putString("name", "$name->${params["to"] as String}")
                        handler.sendMessage(message)
                    }
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    super.onMessage(webSocket, bytes)
                    Log.d(TAG, "onMessage-->bytes:$bytes")
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    Log.d(TAG, "onClosed-->code:$code\treason$reason")
                    webSocketUtils.endHeart()
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosing(webSocket, code, reason)
                    Log.d(TAG, "onClosing-->code:$code\treason$reason")
                    webSocketUtils.endHeart()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    Log.d(TAG, "onFailure-->throwable:$t")
//                    webSocketUtils.reConnect()
                    webSocketUtils.endHeart()
                }
            })
        webSocketUtils.startHeart("{\"cmd\":\"13\",\"hbbyte\":\"0\"}", 1000)
    }

    private fun defaultItem(): List<ChatData> {
//        val data1 = ChatData(ChatAdapter.TYPE_LEFT)
//        data1.name = "你啊"
//        data1.content = "现在我们是好友啦,开始聊天吧"
//        data1.time = format(Date())
//
//        val data2 = ChatData(ChatAdapter.TYPE_LEFT)
//        data2.name = "你啊"
//        data2.content = "Come On 快给我发消息吧娃儿"
//        data2.time = format(Date())
//
//        chatList.add(data1)
//        chatList.add(data2)

        // ?= 修饰 即允许为空
        // ?.即 即使 Object为空，也不报错
//        val mList: ArrayList<String>? = null
//        Log.d(TAG, "ListSize = ${mList?.size}")
        return ArrayList()
    }

    private fun format(time: Date): String {
        return SimpleDateFormat("YYYY-MM-dd HH:mm:ss", Locale.CHINA).format(time)
    }

    private fun changeFriend(friendsName: String) {
        this.friendsName = friendsName
        supportActionBar?.title = "$userName->$friendsName:五毛不用找"
    }

    private fun changeUser(userName: String) {
        this.userName = userName
        supportActionBar?.title = "$userName->$friendsName:五毛不用找"
        webSocketUtils.reConnect(getUrl(userName))
    }

	//原使用公司路径：故删去ip地址
	//正在构建自己的通讯服务器，敬请期待
    private fun getUrl(userName: String): String {
        return "ws://***.***.***?username=$userName&password=123"
    }
}
