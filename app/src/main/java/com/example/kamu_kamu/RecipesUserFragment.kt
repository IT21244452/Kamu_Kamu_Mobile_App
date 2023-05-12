package com.example.kamu_kamu

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.kamu_kamu.adapters.AdapterPdfUser
import com.example.kamu_kamu.databinding.FragmentRecipesUserBinding
import com.example.kamu_kamu.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RecipesUserFragment : Fragment {
    
    
    //view binding fragment_recipes_user.xml => FragmentRecipesUserBinding
    private lateinit var binding: FragmentRecipesUserBinding
    
    public companion object{
        private const val TAG = "RECIPES_USER_TAG"
        
        //receive data from activity to load recipes
        public fun newInstance(categoryId:String, category: String, uid: String): RecipesUserFragment{
            val fragment = RecipesUserFragment()
            //put data to bundle intent
            val args = Bundle()
            args.putString("categoryId",categoryId)
            args.putString("category",category)
            args.putString("uid",uid)
            fragment.arguments = args
            return fragment
        }
    }
    
    private var categoryId = ""
    private var category = ""
    private var uid = ""
    
    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser
    
    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get arguments that passed in newInstance method
        val args = arguments
        if(args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
        
        
    }
    
    

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentRecipesUserBinding.inflate(LayoutInflater.from(context), container, false)
        
        //load pdf according to the category
        Log.d(TAG, "onCreateView: Category: $category")
        if(category == "All"){
            //load all recipes
            loadAllRecipes()

        }
        else if (category == "Most Viewed"){
            //load Most Viewed recipes
            loadMostViewedDownloadedRecipes("viewsCount")
            
        }
        else if(category == "Most Downloaded"){
            //load most downloaded recipes 
            loadMostViewedDownloadedRecipes("downloadCount")
            
        }
        else{
            //load selected category recipes
            loadCategorizedRecipes()
        }
        
        //search
        binding.searchEt.addTextChangedListener { object: TextWatcher{

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                }
                catch (e: Exception){
                    Log.d(TAG, "onTextChanged: SEARCH EXCEPTION: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        } }
        
        return binding.root
    }

    private fun loadAllRecipes() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)

                    //add to list
                    pdfArrayList.add(model!!)
                }
                
                try {
                    //setup adapter
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    //set adapter to recycler view
                    binding.recipesRv.adapter = adapterPdfUser
                }
                catch (e:Exception){
                    Log.d(TAG, "onDataChange: setup adapter failed")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun loadMostViewedDownloadedRecipes(orderBy: String) {

        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.orderByChild(orderBy).limitToLast(10)  // load 10 most viewed or most downloaded recipes
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)

                    //add to list
                    pdfArrayList.add(model!!)
                }
                
                try {
                    //setup adapter
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    //set adapter to recycler view
                    binding.recipesRv.adapter = adapterPdfUser
                }
                catch (e: Exception){
                    Log.d(TAG, "onDataChange: Failed setup")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })



    }

    private fun loadCategorizedRecipes() {

        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Recipes")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before starting adding data into it
                    pdfArrayList.clear()
                    for(ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)

                        //add to list
                        pdfArrayList.add(model!!)
                    }
                    
                    try {
                        //setup adapter
                        adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                        //set adapter to recycler view
                        binding.recipesRv.adapter = adapterPdfUser
                    }
                    catch (e: Exception){
                        Log.d(TAG, "onDataChange: failed")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


        
    }


}