package com.example.kamu_kamu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.kamu_kamu.databinding.RowPdfAdminBinding

class AdapterPdfAdmin :RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable{

    //context
    private var context: Context
    //arraylist to hold pdfs
    public var pdfArrayList : ArrayList<ModelPdf>
    private val filterList:ArrayList<ModelPdf>


    //viewBinding
    private lateinit var binding:RowPdfAdminBinding

    //filter object
    var filter: FilterPdfadmin? = null


    //constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }






    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
       //bind/ inflate layout row_pdf_admin.xml
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent, false)

        return HolderPdfAdmin(binding.root)
    }


    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
     //GET data, set data, Handle click etc

        //get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        //convert timestamp to dd/MM/yy format
        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        //load category, pdf from url , pdf size


        //local category
        MyApplication.loadCategory(categoryId, holder.categoryTv)

        //pass null for page number // load pdf thumbnail
        MyApplication.loadPdfFromUrlSinglePage(pdfUrl,title, holder.pdfView , holder.progressBar, null)

        //load pdg size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

    }

    override fun getItemCount(): Int {
        return pdfArrayList.size // items count
    }




    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfadmin(filterList, this)
        }

        return filter as FilterPdfadmin
    }


    //    View Holder class for row_pdf_admin.xml
    inner class HolderPdfAdmin (itemView: View): RecyclerView.ViewHolder(itemView){

        //UI Views of row_pdf_admin.xml
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn


    }



}