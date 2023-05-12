package com.example.kamu_kamu.adapters

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.kamu_kamu.filters.FilterPdfUser
import com.example.kamu_kamu.MyApplication
import com.example.kamu_kamu.activities.PdfDetailActivity
import com.example.kamu_kamu.databinding.RowPdfUserBinding
import com.example.kamu_kamu.models.ModelPdf


class AdapterPdfUser : Adapter<AdapterPdfUser.HolderPdfUser>, Filterable{

    //    context, get using constructor
    private var context:Context


    //    arraylist to hold pdfs, get using constructor
    public var pdfArrayList: ArrayList<ModelPdf>//to access in filter class

    //arraylist to hold filtered list
    public var filterList: ArrayList<ModelPdf>


    //    viewBinding row_pdf_user.xml => RowPdfUserBinding
    private lateinit var binding:RowPdfUserBinding




private var filter: FilterPdfUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        //inflate/bind layout row_pdf_user.xml
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfUser(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size // return list size/ number of records
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        //Get data , set data, handle click

        //get data
        val model = pdfArrayList[position]
        val recipeId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp


        //convert time
        val date = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

try {
//    MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)
}
catch (e: Exception){
    Log.d(TAG, "onBindViewHolder: failed")
}

        MyApplication.loadCategory(categoryId, holder.categoryTv)


        try {

//            MyApplication.loadPdfSize(url, title, holder.sizeTv)
        }
        catch (e: Exception){
            Log.d(TAG, "onBindViewHolder: failed load size")
        }

        //handle click, open pdf details page
        holder.itemView.setOnClickListener {
            //pass recipe id in intent to get pdf info
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            context.startActivity(intent)
        }


    }


    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfUser
    }




    //    ViewHolder class row_pdf_user.xml
    inner class HolderPdfUser(itemView: View): ViewHolder(itemView){
        //init UI components of row_pdf_user.xml

        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv


    }



}