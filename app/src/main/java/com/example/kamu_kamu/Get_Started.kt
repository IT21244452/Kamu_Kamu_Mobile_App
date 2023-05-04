package com.example.kamu_kamu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kamu_kamu.databinding.ActivityGetStartedBinding

class Get_Started : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityGetStartedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //handle click,continue
        binding.continuebtn.setOnClickListener{
            startActivity(Intent(this, Welcome::class.java))
        }





    }
}