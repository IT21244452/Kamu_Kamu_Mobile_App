package com.example.kamu_kamu.activities


import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Toast
import com.example.kamu_kamu.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityForgotPasswordBinding


    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDilog : ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //setup progress dialog
        progressDilog = ProgressDialog(this)
        progressDilog.setTitle("Please wait..")
        progressDilog.setCanceledOnTouchOutside(false)


        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


        //handle click, begin password recovery process
        binding.submitBtn.setOnClickListener {
            validateData()

        }


    }

    private var email = ""

    private fun validateData() {
        //get data
        email = binding.emailEt.text.toString().trim()

        //validate data
        if(email.isEmpty()){
            Toast.makeText(this,"Enter email...", Toast.LENGTH_SHORT).show()

        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid email pattern...", Toast.LENGTH_SHORT).show()
        }
        else{
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        //show progress
        progressDilog.setMessage("Sending password reset instructions to $email")
        progressDilog.show()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                //sent
                progressDilog.dismiss()
                Toast.makeText(this,"Instruction sent to \n$email", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e->
               // failed
                progressDilog.dismiss()
                Toast.makeText(this,"Failed to send due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }
}