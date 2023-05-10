package com.example.kamu_kamu

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.kamu_kamu.databinding.ActivityPdfDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
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

    private lateinit var progressDialog:ProgressDialog



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


        //increment recipe view count,whenever page starts
        MyApplication.incrementRecipeViewCount(recipeId)

        loadRecipeDetails()

        //handle back button click, goback
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
                    MyApplication.loadPdfFromUrlSinglePage("$recipeUrl" , "$recipeTitle", binding.pdfView, binding.progressBar,binding.pagesTv)

                    //load pdf ize
                    MyApplication.loadPdfSize("$recipeUrl","$recipeTitle", binding.sizeTv)

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
}