package com.android.test

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ThirdActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_third)

        findViewById<TextView>(R.id.tv_1).setText(getString(R.string.info_3, intent.extras?.get("item").toString()))

        findViewById<LinearLayout>(R.id.button_exit).setOnClickListener{
            finish()
        }
    }
}
