package com.example.kamu_kamu

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.kamu_kamu.activities.PdfDetailActivity
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
    
    companion object{
        //created a static method to convert timestamp to proper date format
        fun formatTimeStamp(timestamp: Long) : String{
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            
            //format dd/MM/yyy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }
        
        //function to get pdf size
        fun loadPdfSize(pdfUrl:String , pdfTitle: String, sizeTv:TextView){
            val TAG = "PDF_SIZE_TAG"
            
            //get file and metadata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener {storageMetaData ->
                    Log.d(TAG, "loadPdfSize: got metadata")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    //convert bytes to kb/MB
                    val kb = bytes/1024
                    val mb = kb/1024
                    if(mb>=1){
                        sizeTv.text = "${String.format("%.2f", mb)}MB"
                    }
                    else if(kb >= 1){
                        sizeTv.text = "${String.format("%.2f", kb)}kb"
                    }
                    else{
                        sizeTv.text = "${String.format("%.2f", bytes)}bytes"
                    }
                }
                .addOnFailureListener{e->
                    //failed to get metadata
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                    
                }
            
        }
        
        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
        pdfTitle: String,
        pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ){
            val TAG = "PDF_THUMBNAIL_TAG"

            //using url we can get file and its metadata from firebase
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->

                    if (bytes != null) {

                        Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                        //SET to pdf view
                        pdfView.fromBytes(bytes)
                            .pages(0) // show first page only
                            .spacing(0)
                            .swipeHorizontal(false)
                            .enableSwipe(false)
                            .onError { t ->
                                progressBar.visibility = View.INVISIBLE
                                Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                            }
                            .onPageError { page, t ->
                                progressBar.visibility = View.INVISIBLE
                                Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")

                            }
                            .onLoad { nbPages ->
                                Log.d(TAG, "loadPdfFromUrlSinglePage: Pages:$nbPages")

                                //pdf loaded, we can set page count , pdf thumbnail
                                progressBar.visibility = View.INVISIBLE

                                //if pagesTv param is not null then set page numbers
                                if (pagesTv != null) {
                                    pagesTv.text = "$nbPages"
                                }
                            }
                            .load()

//                    }

                    }

                }


                        .addOnFailureListener { e ->
                            //failed to get metadata
                            Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")

                        }


        }



        fun loadCategory(categoryId: String, categoryTv: TextView){
            //load category using category id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val category:String = "${snapshot.child("category").value}"

                        //set category
                        categoryTv.text = category

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        fun deleteRecipe(context: Context, recipeId:String, recipeUrl: String, recipeTitle: String){
            //param details
            //1) context , use when require toast, prgress ialog
             // 2)  recipe id, to delete recipe from db
            //3) recipeUrl, delete recipe from firebase storage
            //4) recipe Title , show in dialog
            
            val TAG = "DELETE_RECIPE_TAG"
            Log.d(TAG, "deleteRecipe: deleting....")
            
            //progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $recipeTitle...")
            progressDialog.show()

            Log.d(TAG, "deleteRecipe: Deleting from storage....")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(recipeUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteRecipe: Deleted from storage")
                    Log.d(TAG, "deleteRecipe: Deleting from db now...")
                    
                    val ref = FirebaseDatabase.getInstance().getReference("Recipes")
                    ref.child(recipeId)
                        .removeValue()
                        .addOnSuccessListener { 
                            progressDialog.dismiss()
                            Toast.makeText(context,"Successfully deleted..." , Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteRecipe: Deleted from db too...")
                        }
                        .addOnFailureListener {e->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteRecipe: Failed to delete from db due to ${e.message}")
                            Toast.makeText(context,"Failed to delete due to ${e.message}" , Toast.LENGTH_SHORT).show()
                            
                        }
                    
                }
                .addOnFailureListener{e->
                    progressDialog.dismiss()
                    Log.d(TAG, "deleteRecipe: Failed to delete from storage due to ${e.message}")
                    Toast.makeText(context,"Failed to delete due to ${e.message}" , Toast.LENGTH_SHORT).show()
                    
                }
        }

        fun incrementRecipeViewCount(recipeId: String){
            //1)Get current book views count
            val ref = FirebaseDatabase.getInstance().getReference("Recipes")
            ref.child(recipeId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if(viewsCount == "" || viewsCount== "null"){
                            viewsCount = "0";
                        }

                        //2) Increment views count
                        val newViewsCount = viewsCount.toLong() + 1

                        //setup data to update in db
                        val hashMap = HashMap<String, Any> ()
                        hashMap["viewsCount"] = newViewsCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Recipes")
                        dbRef.child(recipeId)
                            .updateChildren(hashMap)

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }


        public fun removeFromFavorite(context: Context, recipeId: String){
            val TAG = "REMOVE_FAV_TAG"

            Log.d(TAG, "removeFromFavorite: Remove from favorite")

            val firebaseAuth = FirebaseAuth.getInstance()


            //database ref
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(recipeId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "removeFromFavorite: removed from favorite")
                    Toast.makeText(context, "Removed from favorites",Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener{e->
                    Log.d(TAG, "removeFromFavorite: Failed to remove from favorite due to ${e.message}")
                    Toast.makeText(context, "Failed to remove from favorite due to ${e.message}",Toast.LENGTH_SHORT).show()

                }
        }

    }

}