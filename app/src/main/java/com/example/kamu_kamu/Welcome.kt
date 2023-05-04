package com.example.kamu_kamu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kamu_kamu.databinding.ActivityWelcomeBinding

class Welcome : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //handle click, login
        binding.loginbtn.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))

        }

        //handle click, skip and continue to main screen
        binding.skipBtn.setOnClickListener{
            startActivity(Intent(this,DashboardUserActivity::class.java))

        }
    }
}