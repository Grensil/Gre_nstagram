package com.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gre_nstagram.R
import com.example.gre_nstagram.databinding.ActivityMainBinding
import com.example.gre_nstagram.databinding.FragmentDetailBinding
import com.example.gre_nstagram.databinding.ItemDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.navigation.model.AlarmDTO
import com.navigation.model.ContentDTO
import com.navigation.util.FcmPush

class DetailViewFragment : Fragment() {
    var firestore : FirebaseFirestore? = null

    private var mBinding: FragmentDetailBinding? = null // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!
    var uid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentDetailBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()

        return binding.root;

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 이 설정을 꼭해줘야 아템 들어감
        val detailviewfragment_recylcerview = binding.detailviewfragmentRecylcerview;
        

        val linearLayoutManager = LinearLayoutManager(requireContext());
        linearLayoutManager.isItemPrefetchEnabled = false;
        detailviewfragment_recylcerview.layoutManager = linearLayoutManager;
        
        detailviewfragment_recylcerview.adapter = DetailViewRecyclerViewAdapter();
    }



    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()//요기에
        var contentUidList: ArrayList<String> = arrayListOf() // UID 리스트

        init {
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    if(querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {


            var view = ItemDetailBinding.inflate(LayoutInflater.from(p0.context), p0, false)

            return CustomViewHoler(view)
        }
        inner class CustomViewHoler(view: ItemDetailBinding) : RecyclerView.ViewHolder(view.root){
            val view = view;

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    
        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHoler);


            viewholder.view.detailviewProfileTextview.text = contentDTOs!![p1].userId

            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl).into(viewholder.view.detailviewImageviewContent)

            viewholder.view.detailviewtiemExplainTextview.text = contentDTOs!![p1].explain

            viewholder.view.detailviewtiemExplainTextview.text = contentDTOs!![p1].explain

            viewholder.view.detailviewtiemFavoritecounterTextview.text = "Likes" + contentDTOs!![p1].favoriteCount

            viewholder.view.detailviewitemFavoriteImageview.setOnClickListener {
                favoriteEvent(p1)
            }

            if(contentDTOs!![p1].favorites.containsKey(uid)) {
                viewholder.view.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite)
            }
            else{
                viewholder.view.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite_border)
            }
            //this code is when the profile image is clicked
            viewholder.view.detailviewitemProfileImage.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[p1].uid)
                bundle.putString("userid",contentDTOs[p1].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()

            }
            viewholder.view.detailviewitemCommentImageview.setOnClickListener {  v->
                var intent = Intent(v.context,CommentActivity::class.java)
                intent.putExtra("contentUid",contentUidList[p1])
                intent.putExtra("destinationUid",contentDTOs[p1].uid)
                startActivity(intent)
            }

        }
        fun favoriteEvent(position : Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)
                if(contentDTO!!.favorites.containsKey(uid)) {
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount -1
                    contentDTO?.favorites.remove(uid)
                }
                else {
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount+1
                    contentDTO?.favorites[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc,contentDTO)


            }
        }

        fun favoriteAlarm(destinationUid: String) {
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userID = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

            var message = FirebaseAuth.getInstance()?.currentUser?.email + getString(R.string.alarm_favorite)
            FcmPush.instance.sendMessage(destinationUid,"Grenstagram",message)


        }



    }
}
