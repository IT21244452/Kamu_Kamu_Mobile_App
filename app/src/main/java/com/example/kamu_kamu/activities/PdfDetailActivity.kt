package com.example.kamu_kamu.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.kamu_kamu.Constants
import com.example.kamu_kamu.MyApplication
import com.example.kamu_kamu.R
import com.example.kamu_kamu.adapters.AdapterComment
import com.example.kamu_kamu.databinding.ActivityPdfDetailBinding
import com.example.kamu_kamu.databinding.DialogCommentAddBinding
import com.example.kamu_kamu.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfDetailActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityPdfDetailBinding

    private companion object{
        //TAG
        const val TAG = "RECIPE_DETAILS_TAG"
    }

    //recipe id, get from intent

    private var recipeId = ""
    //get from firebase
    private var recipeTitle = ""
    private var recipeUrl = ""

    //will hold a boolean value false/true to indicate either is in current user's favorite list or not
    private var isInMyFavorite = false

    private lateinit var firebaseAuth : FirebaseAuth

    private lateinit var progressDialog:ProgressDialog


    //arraylist to hold comments
    private lateinit var commentArrayList: ArrayList<ModelComment>

    //adapter to be set to recycler view
    private lateinit var adapterComment: AdapterComment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get recipe id from intent
        recipeId = intent.getStringExtra("recipeId")!!


        // init progress bar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

//        init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser != null){
            //user is logged in , check whether recipe is in favorite or not
            checkIsFavorite()
        }



        //increment recipe view count,whenever page starts
        MyApplication.incrementRecipeViewCount(recipeId)

        loadRecipeDetails()
        showComments()

        //handle back button click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, open pdf view activity
        binding.readRecipeBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            startActivity(intent)
        }

        //handle click, download recipe
        binding.downloadRecipeBtn.setOnClickListener {
            //first check storage permission
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadRecipe()
            }
            else{
                Log.d(TAG, "onCreate: STORAGE PERMISSION was not granted, LET'S request it")
                requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }


        //handle click, add/remove favourite
        binding.favoriteBtn.setOnClickListener {
            //add only if user logged in
            //1)check whether user logged in or not
            if(firebaseAuth.currentUser == null){
                //user not logged in, Can't do favorite action
                Toast.makeText(this,"You're not logged in", Toast.LENGTH_SHORT).show()
            }
            else{
                //user is logged in,can do favorite action
                if(isInMyFavorite){
                    //already in fav. remove
                    MyApplication.removeFromFavorite(this, recipeId)
                }
                else{
                    //not in favorite. add
                    addToFavorite()
                }

            }
        }


        //handle click, show add comment dialog
        binding.addCommentBtn.setOnClickListener {
//            To add a comment user must be logged in

            if(firebaseAuth.currentUser == null){
                //user not logged in, not allowed to add comment
                Toast.makeText(this, "You're not logged in", Toast.LENGTH_SHORT).show()

            }
            else{
                //user logged in , allow to add comment
                addCommentDialog()

            }

        }

    }

    private fun showComments() {
        //init arraylist
        commentArrayList = ArrayList()
        //db path to load comments
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId).child("Comments")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
//                    clear list
                    commentArrayList.clear()
                    for(ds in snapshot.children){
                        //get data model
                        val model = ds.getValue(ModelComment::class.java)
                        //add to list
                        commentArrayList.add(model!!)
                    }

                    //                setup adapter
                    adapterComment = AdapterComment(this@PdfDetailActivity, commentArrayList)
                    //set adapter to recyclerview
                    binding.commentsRv.adapter = adapterComment

                }






                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


    private var comment = ""

    private fun addCommentDialog() {
        //inflate view for adding dialog dialog_comment_add.xml
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        //setup alert dialog
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        //create and show alert dialog
        val alertDialog = builder.create()
        alertDialog.show()

        //handle click, dismiss dialog
        commentAddBinding.backBtn.setOnClickListener { alertDialog.dismiss() }

        //handle click, add comment
        commentAddBinding.submitBtn.setOnClickListener {
            //get data
            comment = commentAddBinding.commentEt.text.toString().trim()

            //validate data
            if(comment.isEmpty()){
                Toast.makeText(this, "Enter comment....", Toast.LENGTH_SHORT).show()

            }
            else{
                alertDialog.dismiss()
                addComment()
            }
        }

    }

    private fun addComment() {
        //show progress
        progressDialog.setMessage("Adding comment")
        progressDialog.show()

        //timestamp for comment id, comment timestamp
        val timestamp = "${System.currentTimeMillis()}"

        //setup daa to add in db for comment
        val hashMap = HashMap<String , Any>()
        hashMap["id"] = "$timestamp"
        hashMap["recipeId"] = "$recipeId"
//        hashMap["recipeId"] = "$recipeId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"


        //Db path to add data into it
        //Recipes > recipeId > Comment > commentId > commentData
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(this, "Comment added...", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add comment due to ${e.message}", Toast.LENGTH_SHORT).show()

            }







    }


    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted:Boolean ->
        //check granted or not
        if(isGranted){
            Log.d(TAG, "onCreate: STORAGE PERMISSION is granted")
            downloadRecipe()
        }
        else{
            Log.d(TAG, "onCreate: STORAGE PERMISSION is not denied")
            Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    private fun downloadRecipe(){
        Log.d(TAG, "downloadRecipe: Downloading recipe...")

        progressDialog.setMessage("Downloading recipe...")
        progressDialog.show()

        // download recipe from firebase storage using url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(recipeUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes ->
                Log.d(TAG, "downloadRecipe: Recipe Downloaded...")
                saveToDownloadFolder(bytes)
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Log.d(TAG, "downloadRecipe: Failed to download recipe due to ${e.message}")
                Toast.makeText(this,"Failed to download recipe due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDownloadFolder(bytes: ByteArray) {
        Log.d(TAG, "saveToDownloadFolder: saving downloaded recipe")

        val nameWithExtension = "$recipeTitle + ${System.currentTimeMillis()}.pdf"

        try{
            val  downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs() // create folder if not exists

            val filePath = downloadsFolder.path + "/" + nameWithExtension

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this,"Saved to Downloads folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "saveToDownloadFolder: Saved to Downloads folder")

            progressDialog.dismiss()
            incrementDownloadCount()
        }
        catch(e: Exception){
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadFolder: failed to save due to ${e.message}")
            Toast.makeText(this,"failed to save due to ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun incrementDownloadCount() {
        //increment downloads count to firebase db
        Log.d(TAG, "incrementDownloadCount: ")

        //1) get previous download count
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get downloads count
                    var downloadsCount = "${snapshot.child("downloadCount").value}"
                    Log.d(TAG, "onDataChange: Current Downloads Count: $downloadsCount")

                    if(downloadsCount == "" || downloadsCount == "null"){
                        downloadsCount = "0"
                    }

                    //convert to long and increment 1
                    val newDownloadCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: New Downloads Count: $newDownloadCount")

                    //setup data to update to db
                    val hashMap:HashMap<String, Any> = HashMap()
                    hashMap["downloadCount"] = newDownloadCount

                    //2) Update new incremented downloads count to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Recipes")
                    dbRef.child(recipeId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Downloads count incrremented")
                        }
                        .addOnFailureListener{e->
                            Log.d(TAG, "onDataChange: FAILED to increment due to ${e.message}")
                            
                        }
                    

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadRecipeDetails() {
        //Recipes > recipeId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadCount = "${snapshot.child("downloadCount").value}"
                    val timestamp ="${snapshot.child("timestamp").value}"
                    recipeTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    recipeUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"


                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    //load pdf category
                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    //load pdf thumbnail, pages count
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$recipeUrl",
                        "$recipeTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )

                    //load pdf ize
                    MyApplication.loadPdfSize("$recipeUrl", "$recipeTitle", binding.sizeTv)

                    //set data
                    binding.titleTv.text = recipeTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadCount
                    binding.dateTv.text = date


                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: Checking if recipe is in favorite or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(recipeId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if(isInMyFavorite){
                        //available in favourites
                        Log.d(TAG, "onDataChange: available in favourites")
                        //set drawable top icon
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_filled_white,0,0)
                        binding.favoriteBtn.text = "Remove Favorite"
                    }
                    else{
                        //not available in favorites
                        Log.d(TAG, "onDataChange: not available in favorites")
                        //set drawable top icon
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_border_white,0,0)
                        binding.favoriteBtn.text = "Add Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }

    private fun addToFavorite(){
        val timestamp = System.currentTimeMillis()
        Log.d(TAG, "addToFavorite: Adding to favorite")

        //setup data to add in db
        val hashMap = HashMap<String , Any>()
        hashMap["recipeId"] = recipeId
        hashMap["timestamp"] = timestamp


        //save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(recipeId)
            .setValue(hashMap)
            .addOnSuccessListener {
                //added to fav
                Log.d(TAG, "addToFavorite: Added to favorite")
                Toast.makeText(this, "Added to favorite",Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e->
                //failed to add favorite
                Log.d(TAG, "addToFavorite: Failed to add to favorite due to ${e.message}")
                Toast.makeText(this, "Failed to add to favorite due to ${e.message}",Toast.LENGTH_SHORT).show()

            }

    }



}