package com.example.kamu_kamu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.kamu_kamu.databinding.ActivityDashBoardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashBoardAdminActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityDashBoardAdminBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    //adapter
    private lateinit var adapterCategory: AdapterCategory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBoardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        //search
        binding.searchEt.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {


            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //called as and when user type anything
                try {
                    adapterCategory.filter.filter(s)
                }
                catch (e: Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        //handle click, logout
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }

        //handle click, start add category page
        binding.addCategoryBtn.setOnClickListener{
            startActivity(Intent(this,CategoryAddActivity::class.java))
        }


    }

    private fun loadCategories() {
        //init arraylist
        categoryArrayList = ArrayList()

        //get all categories from firebase database
        val ref =FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data
                categoryArrayList.clear()
                for(ds in snapshot.children){
                    //get data as model
                    val model = ds.getValue(ModelCategory::class.java)

                    //add to arraylist
                    categoryArrayList.add(model!!)
                }
                //setup adapter
                adapterCategory = AdapterCategory(this@DashBoardAdminActivity,categoryArrayList)
                //set adapter to recyclerview
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //not logged in, goto main screen
            startActivity(Intent(this, Welcome::class.java))
            finish()
        }
        else{
            //logged in, get and show user info
            val email = firebaseUser.email
            //set to text in the toolbar
            binding.subtitleTv.text = email

        }
    }
}