package com.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gre_nstagram.R
import com.example.gre_nstagram.databinding.FragmentGridBinding
import com.example.gre_nstagram.databinding.FragmentUserBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.navigation.model.ContentDTO

class GridFragment : Fragment() {

    var firestore : FirebaseFirestore? = null

    private var mBinding: FragmentGridBinding? = null // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //var view = LayoutInflater.from(activity).inflate(R.layout.fragment_grid,container,false)
        mBinding = FragmentGridBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        binding.gridfragmentRecyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        binding.gridfragmentRecyclerview?.layoutManager = GridLayoutManager(requireActivity(),3)

        return binding.root
    }



    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore?.collection("images")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }

                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageview = ImageView(p0.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHoler(imageview)
        }
        inner class CustomViewHoler(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var imageview = (p0 as CustomViewHoler).imageview
            Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size

        }

    }
}
