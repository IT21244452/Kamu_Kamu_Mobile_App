package com.example.kamu_kamu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashScreen : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        firebaseAuth = FirebaseAuth.getInstance()


        Handler().postDelayed(Runnable {
            checkUser()
        },2000)
    }

    private fun checkUser() {
        //get current user, if logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //user not logged in go to get started screen
            startActivity(Intent(this,Get_Started::class.java))
            finish()
        }
        else{
            //user logged in check user type, same as done in loggin screen
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {


                        //get userType
                        val userType = snapshot.child("userType").value
                        if(userType == "user"){
                            //open user dashboard
                            startActivity(Intent(this@SplashScreen,DashboardUserActivity::class.java))
                            finish()
                        }
                        else if(userType == "admin"){
                            //open admin dashboard
                            startActivity(Intent(this@SplashScreen,DashBoardAdminActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }
}

