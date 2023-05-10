package com.example.kamu_kamu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.kamu_kamu.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {

    // View binding
    private lateinit var binding: ActivityPdfViewBinding

    //TAG
    private companion object{
        const val TAG = "PDF_VIEW_TAG"
    }


    //recipe id
    var recipeId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get recipe id from intent, to load recipe from firebase
        recipeId = intent.getStringExtra("recipeId")!!
        loadRecipeDetails()

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


    }

    private fun loadRecipeDetails() {
        Log.d(TAG, "loadRecipeDetails: Get pdf URL from db")

        //Database Reference to get recipe details e.g. get recipe url using recipe id
        //Step (1) Get Recipe URL using Recipe Id

        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get recipe url
                    val pdfUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL: $pdfUrl")

                    //load pdf using url from firebase storage
                    loadRecipeFromUrl("$pdfUrl")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            } )


    }

    private fun loadRecipeFromUrl(pdfUrl: String) {
        Log.d(TAG, "loadRecipeFromUrl: Get Pdf from firebase storage using URL")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG, "loadRecipeFromUrl: pdf got from url")

                //load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)//set false to scroll vertical, set true to scroll horizontal
                    .onPageChange{page, pageCount->
                        //set current and total pages
                        val currentPage = page+1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                        Log.d(TAG, "loadRecipeFromUrl:  $currentPage/$pageCount")
                    }
                    .onError{t->
                        Log.d(TAG, "loadRecipeFromUrl: ${t.message}")
                }
                    .onPageError { page, t ->
                        Log.d(TAG, "loadRecipeFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE

            }
            .addOnFailureListener{e->
                Log.d(TAG, "loadRecipeFromUrl: Failed to get url due to ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}