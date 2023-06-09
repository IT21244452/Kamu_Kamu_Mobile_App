package com.example.kamu_kamu.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.kamu_kamu.filters.FilterPdfadmin
import com.example.kamu_kamu.MyApplication
import com.example.kamu_kamu.R
import com.example.kamu_kamu.activities.PdfDetailActivity
import com.example.kamu_kamu.activities.PdfEditActivity
import com.example.kamu_kamu.databinding.RowPdfAdminBinding
import com.example.kamu_kamu.models.ModelPdf
import kotlin.random.Random

class AdapterPdfAdmin :Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable{

    //context
    private var context: Context
    //arraylist to hold pdfs
    public var pdfArrayList : ArrayList<ModelPdf>
    private val filterList:ArrayList<ModelPdf>


    //viewBinding
    private lateinit var binding:RowPdfAdminBinding

    //filter object
    private var filter: FilterPdfadmin? = null


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
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //handle click, show dialog with options 1) Edit recipe, 2) Delete Recipe
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

//        //handle item click
        holder.itemView.setOnClickListener {
            //intent with recipe id
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("recipeId", pdfId) // will be used to load recipe details
            context.startActivity(intent)
        }


    }

    private fun moreOptionsDialog(model: ModelPdf, holder: HolderPdfAdmin) {
            //get id, url , title of recipe
        val recipeId = model.id
        val recipeUrl = model.url
        val recipeTitle = model.title

        //options to show in dialog
        val options = arrayOf("Edit", "Delete")

        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options){dialog, position ->
                // handle item click
                if(position == 0){
                    //Edit is clicked
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("recipeId", recipeId) // passed recipeId, will be used to edit the recipe
                    context.startActivity(intent)

                }
                else if(position == 1){
                    //delete is clicked // delete function is in MyApplication class


                    MyApplication.deleteRecipe(context, recipeId, recipeUrl, recipeTitle)
                }

            }
            .show()

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


    //to get random colours
    fun randomColor(): Int{
        val list = ArrayList<Int>()
        list.add(R.color.pdfColor1)
        list.add(R.color.pdfColor2)
        list.add(R.color.pdfColor3)
        list.add(R.color.pdfColor4)
        list.add(R.color.pdfColor5)
        list.add(R.color.pdfColor6)

        val seed = System.currentTimeMillis().toInt()
        val randomIndex = Random(seed).nextInt(list.size)
        return list[randomIndex]
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