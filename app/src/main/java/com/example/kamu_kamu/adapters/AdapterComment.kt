package com.example.kamu_kamu.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.kamu_kamu.MyApplication
import com.example.kamu_kamu.R
import com.example.kamu_kamu.databinding.RowCommentBinding
import com.example.kamu_kamu.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment : Adapter<AdapterComment.HolderComment> {

    //context
    val context: Context

    //arraylist to hold comments
    val commentArrayList: ArrayList<ModelComment>

    //view binding row_comment.xml => RowCommentBinding
    private lateinit var binding: RowCommentBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //constructor

    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.commentArrayList = commentArrayList

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        //binding row_comment.xml
        binding = RowCommentBinding.inflate(LayoutInflater.from(context),parent, false)

        return HolderComment(binding.root)
    }

    override fun getItemCount(): Int {

        return commentArrayList.size // return list size

    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        /*-----Get data, set data, handle click------*/

        //get data
        val model = commentArrayList[position]

        val id = model.id
        val recipeId = model.recipeId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        //format timestamp
        val date = MyApplication.formatTimeStamp(timestamp.toLong())

        //set data
        holder.dateTv.text = date
        holder.commentTv.text = comment

        //load user name, profile picture using uid
        loadUserDetails(model, holder)

        //handle click, show dialog to delete comment
        holder.itemView.setOnClickListener {
//            Requirements to delete a comment
            //1) User must be logged in
            //2)uid in comment(to be deleted) must be same as uid of current user
            // User can delete only his comments

            if(firebaseAuth.currentUser != null && firebaseAuth.uid == uid){
                deleteCommentDialog(model, holder)
            }


        }


    }

    private fun deleteCommentDialog(model: ModelComment, holder: AdapterComment.HolderComment) {
//            alert dialog

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Comment")
            .setMessage(" Are you sure you want to delete this comment?")
            .setPositiveButton("DELETE"){d,e->

                val recipeId = model.recipeId
                val commentId = model.id

                //delete comment
                val ref = FirebaseDatabase.getInstance().getReference("Recipes")
                ref.child(recipeId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Comment deleted....", Toast.LENGTH_SHORT).show()

                    }
                    .addOnFailureListener{e->
                        //failed to delete
                        Toast.makeText(context, "Failed to delete comment due to ${e.message}", Toast.LENGTH_SHORT).show()

                    }

            }
            .setNegativeButton("CANCEL"){d,e->
                d.dismiss()
            }
            .show()

    }

    private fun loadUserDetails(model: ModelComment, holder: AdapterComment.HolderComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get name, profile
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    //set data
                    holder.nameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.profileIv)
                    }
                    catch (e: Exception){
                        //in case of exception due to image is empty or null or other reason, set default image
                        holder.profileIv.setImageResource(R.drawable.ic_person_gray)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }


//    ViewHolder class for row_comment.xml

    inner class HolderComment(itemView:View):ViewHolder(itemView){

        //init ui views of row_comment.xml
        val profileIv = binding.profileIv
        val nameTv= binding.nameTv
        val dateTv= binding.dateTv
        val commentTv = binding.commentTv

    }

}