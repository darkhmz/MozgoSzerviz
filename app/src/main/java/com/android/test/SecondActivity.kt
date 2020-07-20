package com.android.test

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_second)

        findViewById<LinearLayout>(R.id.button_order).setOnClickListener{
            val phone = findViewById<EditText>(R.id.edit_phone).text.trim().toString()
            val email = findViewById<EditText>(R.id.edit_email).text.trim().toString()

            if(phone.isEmpty() || !isValid(email)){
                AlertDialog.Builder(this).setMessage(R.string.label_error_contact).setPositiveButton(android.R.string.ok, null).create().show()
                return@setOnClickListener
            }

            val i = Intent(this, ThirdActivity::class.java).apply {
                putExtra("item", intent.extras?.get("item").toString())
            }
            startActivity(i)
            finish()
        }
    }

    private fun isValid(s: String): Boolean{
        return s.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()
    }

}
