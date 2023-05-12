package com.example.kamu_kamu.activities

import android.app.ProgressDialog
import android.content.AbstractThreadedSyncAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.kamu_kamu.MyApplication
import com.example.kamu_kamu.R
import com.example.kamu_kamu.adapters.AdapterPdfFavorite
import com.example.kamu_kamu.databinding.ActivityProfileBinding
import com.example.kamu_kamu.models.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityProfileBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //Firebase current user
    private lateinit var firebaseUser: FirebaseUser


    //arraylist to hold recipes
    private lateinit var recipesArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfFavorite: AdapterPdfFavorite

    //progress dialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //reset to default values
        binding.accountTypeTv.text = "N/A"
        binding.memberDateTv.text = "N/A"
        binding.favoriteRecipeCountTv.text = "N/A"
        binding.accountStatusTv.text = "N/A"


        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        //set progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)


        loadUserinfo()
        loadFavouriteRecipes()

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


        //handle click, open edit profile
        binding.profileEditBtn.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }

        //handle click, verify user if not
        binding.accountStatusTv.setOnClickListener {
            if(firebaseUser.isEmailVerified){
                //User is verified
                Toast.makeText(this, "Already verified...!", Toast.LENGTH_SHORT).show()
            }
            else{
                //User isn't verified, show confirmation dialog before verification
                emailVerificationDialog()
            }
        }

    }

    private fun emailVerificationDialog() {
        //show confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verify Email")
            .setMessage("Are you sure you want to send email verification instructions to your email ${firebaseUser.email}")
            .setPositiveButton("SEND"){d,e->
                sendEmailVerification()

            }
            .setNegativeButton("CANCEL"){d,e->
                d.dismiss()
            }
            .show()
    }

    private fun sendEmailVerification() {
        //show progress dialog
        progressDialog.setMessage("Sending email verification instructions to email ${firebaseUser.email}")
        progressDialog.show()

        //send instructions
        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                //successfully sent
                progressDialog.dismiss()
                Toast.makeText(this, "Instructions sent! ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{e->
                //failed to send
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to send due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadUserinfo() {

        //check if user is verfied or not
        if(firebaseUser.isEmailVerified){
            binding.accountStatusTv.text = "Verified"
        }else{
            binding.accountStatusTv.text = "Not Verified"
        }

        //db reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //convert timestamp
                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    //set data
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType

                    //set image
                    try{
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray).into(binding.profileIv)
                    }
                    catch (e:Exception){

                    }

                }
                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


    private fun loadFavouriteRecipes(){

        //init arraylist
        recipesArrayList = ArrayList()


        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    //clear arraylist, before starting adding data
                    recipesArrayList.clear()
                    for(ds in snapshot.children){
                        //get only id of the recipe, rest of the info have loaded in adaptor class
                        val recipeId = "${ds.child("recipeId").value}"

                        //set to model
                        val modelPdf = ModelPdf()
                        modelPdf.id = recipeId


                        //add model to list
                        recipesArrayList.add(modelPdf)
                    }
                    //set number of favorite recipes
                    binding.favoriteRecipeCountTv.text = "${recipesArrayList.size}"

                    //setup adapter
                    adapterPdfFavorite = AdapterPdfFavorite(this@ProfileActivity, recipesArrayList)

                    //set adapter to recyclerview
                    binding.favoriteRv.adapter = adapterPdfFavorite

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

    }



}