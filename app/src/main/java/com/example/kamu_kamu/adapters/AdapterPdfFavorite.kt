package com.example.kamu_kamu.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.kamu_kamu.MyApplication
import com.example.kamu_kamu.activities.PdfDetailActivity
import com.example.kamu_kamu.databinding.ActivityProfileEditBinding
import com.example.kamu_kamu.databinding.RowPdfFavoriteBinding
import com.example.kamu_kamu.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorite : Adapter<AdapterPdfFavorite.HolderPdfFavorite> {

    //Context
    private val context: Context

    //Array list to hold books
    private var recipesArrayList: ArrayList<ModelPdf>



    // view binding
    private lateinit var binding: RowPdfFavoriteBinding


    //constructor

    constructor(context: Context, recipesArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.recipesArrayList = recipesArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        //bind  row_pdf_favorite.xml
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfFavorite(binding.root)

    }

    override fun getItemCount(): Int {
        return recipesArrayList.size // return size of list / number of items in list

    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {

        /* ------Get data , set data , handle clicks -----*/

        //get data
        val model = recipesArrayList[position]

        loadRecipeDetails(model, holder)


        //handle click, open pdf details page, pass recipe id to load details
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("recipeId", model.id) // pass recipe id
            context.startActivity(intent)
        }

        //handle  click, remove from favorite
        holder.removeFavBtn.setOnClickListener {
            MyApplication.removeFromFavorite(context, model.id)
        }


    }

    private fun loadRecipeDetails(model: ModelPdf, holder: AdapterPdfFavorite.HolderPdfFavorite) {
        val recipeId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.child(recipeId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get recipe info
                   val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadCount = "${snapshot.child("downloadCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"


                    //set data to model
                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()
                    model.downloadCount = downloadCount.toLong()


                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    MyApplication.loadCategory("$categoryId" , holder.categoryTv)
                    MyApplication.loadPdfFromUrlSinglePage("$url", "$title", holder.pdfView, holder.progressBar, null)
                    MyApplication.loadPdfSize("$url", "$title" , holder.sizeTv)

                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.dateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

    }


    //View holder class to manage UI views of row_pdf_favorite.xml

    inner class HolderPdfFavorite(itemView: View) : ViewHolder(itemView){
        //init UI Views
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var removeFavBtn = binding.removeFavBtn
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv

    }




}