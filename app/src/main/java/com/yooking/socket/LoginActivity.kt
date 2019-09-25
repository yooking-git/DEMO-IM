package com.yooking.socket

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * create by yooking on 2019/8/10
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val mContext: Context = this

        //设置标题栏为toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)//利用Toolbar代替ActionBar
        //设置标题
        supportActionBar?.title = "聊五毛呀"

        findViewById<View>(R.id.tv_login_login).setOnClickListener {
            val userName: String = findViewById<EditText>(R.id.et_login_user).text.toString()
            val friendsName: String = findViewById<EditText>(R.id.et_login_friend).text.toString()
            val intent = Intent(mContext, ChatActivity::class.java)
            intent.putExtra("userName", userName)
            intent.putExtra("friendsName", friendsName)
            startActivity(intent)
            finish()
        }
    }
}