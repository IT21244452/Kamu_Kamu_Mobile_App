package com.example.kamu_kamu

import android.widget.Filter
import com.example.kamu_kamu.models.ModelPdf


//use to filter data from recycler view / search pdf from pdf list in recyclerview
class FilterPdfadmin : Filter {
    //arraylist in which we want to search
    var filterList: ArrayList<ModelPdf>

    //adapter in which filter need to be implemented
    var adapterPdfAdmin: AdapterPdfAdmin

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint // value to search
        val results = FilterResults()
        //value to be searched should not be null and not empty
        if(constraint != null && constraint.isNotEmpty()){
            //change to upper case or lower case
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices){
                //validate if match
                if(filterList[i].title.lowercase().contains(constraint)){
                    //searched value is similar to value in list, add to filtered list
                    filteredModels.add(filterList[i])

                }
            }
            results.count = filteredModels.size
            results.values = filteredModels

        }
        else{
            //value is either null or empty, return all data
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
       //apply filter changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>
        //notify changes
        adapterPdfAdmin.notifyDataSetChanged()
    }


}