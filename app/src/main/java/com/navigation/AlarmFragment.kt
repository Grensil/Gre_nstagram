package com.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gre_nstagram.R
import com.example.gre_nstagram.databinding.FragmentAlarmBinding
import com.example.gre_nstagram.databinding.FragmentGridBinding
import com.example.gre_nstagram.databinding.ItemCommentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.navigation.model.AlarmDTO

class AlarmFragment : Fragment() {
    private var mBinding: FragmentAlarmBinding? = null // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm,container,false)
        mBinding = FragmentAlarmBinding.inflate(inflater, container, false)


//
//        val uid = FirebaseAuth.getInstance().currentUser?.uid
//        FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid",uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//            alarmDTOList.clear()
//            if (querySnapshot == null) return@addSnapshotListener
//            for (snapshot in querySnapshot.documents) {
//                alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
//            }
//        }

        binding.alarmfragmentRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.alarmfragmentRecyclerview.adapter = AlarmRecyclerViewAdapter()

        return binding.root
    }

    //private val alarmDTOList : ArrayList<AlarmDTO>
    inner class AlarmRecyclerViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()
        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid",uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                alarmDTOList.clear()
                if(querySnapshot ==null) return@addSnapshotListener
                for (snapshot in querySnapshot.documents) {
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
//            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_comment,p0,false)
//            return CustomViewHolder(view)
            var view = ItemCommentBinding.inflate(LayoutInflater.from(p0.context), p0, false)
            return CustomViewHolder(view)

        }
        inner class CustomViewHolder(view: ItemCommentBinding) :RecyclerView.ViewHolder(view.root) {
            var view = view
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder)
//            var view = p0.itemView
//            //알림 프로필이미지 보이기
//            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[p1].uid!!).get().addOnCompleteListener { task ->
//                if(task.isSuccessful) {
//                    val url = task.result!!["image"]
//                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(viewholder.view.commentviewitemImageviewProfile)
//                }
//            }

            when(alarmDTOList[p1].kind) {
                0 -> {
                    var str_0 = alarmDTOList[p1].userID +" " + getString(R.string.alarm_favorite)
                    viewholder.view.commentviewitemTextviewProfile.text = str_0
                }
                1 -> {
                    var str_0 = alarmDTOList[p1].userID + " " + getString(R.string.alarm_comment) +"of" + alarmDTOList[p1].message
                    viewholder.view.commentviewitemTextviewProfile.text = str_0

                }
                2 -> {
                    var str_0 = alarmDTOList[p1].userID + " "+ getString(R.string.alarm_follow)
                    viewholder.view.commentviewitemTextviewProfile.text = str_0

                }
            }
            viewholder.view.commentviewitemTextviewComment.visibility = View.INVISIBLE
        }

        override fun getItemCount(): Int {
            return alarmDTOList.size

        }

    }
}
