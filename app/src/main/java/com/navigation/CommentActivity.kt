package com.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gre_nstagram.R
import com.example.gre_nstagram.databinding.ActivityCommentBinding
import com.example.gre_nstagram.databinding.ActivityLoginActivtyBinding
import com.example.gre_nstagram.databinding.ItemCommentBinding
import com.example.gre_nstagram.databinding.ItemDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.navigation.model.AlarmDTO
import com.navigation.model.ContentDTO
import com.navigation.util.FcmPush

class CommentActivity : AppCompatActivity() {

    val binding by lazy { ActivityCommentBinding.inflate(layoutInflater) }
    var contentUid : String? = null
    var destionationUid : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        contentUid = intent.getStringExtra("contentUid")
        destionationUid = intent.getStringExtra("destinationUid")
        binding.commentBtnSend?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userID = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = binding.commentEditMessage.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)
            commentAlarm(destionationUid!!,binding.commentEditMessage.text.toString())

            binding.commentEditMessage.setText("")
        }

        val recyclerView = binding.commentRecyclerview;
        val linearLayoutManager = LinearLayoutManager(this);
        recyclerView.layoutManager = linearLayoutManager;
        recyclerView.adapter = CommentRecyclerViewAdapter();

    }
    fun commentAlarm(destinationUid: String, message : String) {
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userID = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.kind = 1
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        var msg = FirebaseAuth.getInstance().currentUser?.email + " " + getString(R.string.alarm_comment) + " of " + message
        FcmPush.instance.sendMessage(destinationUid,"Grenstagram",msg)


    }





    inner class  CommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments : ArrayList<ContentDTO.Comment> = arrayListOf()
        init {
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if(querySnapshot==null) return@addSnapshotListener

                    for(snapshot in querySnapshot.documents!!) {
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            //var view = LayoutInflater.from(p0.context).inflate(R.layout.item_comment,p0,false)

            //return CustomViewHolder(view)
            var view = ItemCommentBinding.inflate(LayoutInflater.from(p0.context), p0, false)

            return CustomViewHolder(view)
        }
        private inner class CustomViewHolder(view: ItemCommentBinding) : RecyclerView.ViewHolder(view.root) {
            val view = view
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

            var viewholder = (p0 as CustomViewHolder);
            viewholder.view.commentviewitemTextviewComment.text = comments[p1].comment
            viewholder.view.commentviewitemTextviewProfile.text = comments[p1].userID

            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(comments[p1].uid!!)
                .get()
                .addOnCompleteListener { task->
                    if(task.isSuccessful) {
                        var url = task.result!!["image"]
                        Glide.with(p0.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(viewholder.view.commentviewitemImageviewProfile)
                    }
                }

        }

        override fun getItemCount(): Int {
            return comments.size
        }

    }
}