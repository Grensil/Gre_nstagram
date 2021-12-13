package com.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gre_nstagram.LoginActivty
import com.example.gre_nstagram.MainActivity
import com.example.gre_nstagram.R
import com.example.gre_nstagram.databinding.FragmentDetailBinding
import com.example.gre_nstagram.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.navigation.model.AlarmDTO
import com.navigation.model.ContentDTO
import com.navigation.model.FollowDTO
import com.navigation.util.FcmPush

class UserFragment : Fragment() {
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUseriD : String? = null
    companion object {
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    private var mBinding: FragmentUserBinding? = null // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //var fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        mBinding = FragmentUserBinding.inflate(inflater, container, false)
        uid = arguments?.getString("destinationUid") // 내 계정 uid
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUseriD = auth?.currentUser?.uid // 내가 보고 있는
        if (uid == currentUseriD) {
            //my page
            binding.accountBntFollowSignout?.text = getString(R.string.signout)
            binding?.accountBntFollowSignout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivty::class.java))
                auth?.signOut()
            }
        } else {
            //outheruser page
            binding.accountBntFollowSignout?.text = getString(R.string.follow)
            var mainActivity = (activity as MainActivity)
            mainActivity?.binding.toolbalUsername?.text = arguments?.getString("userId")
            mainActivity?.binding.toolbalBtnBack?.setOnClickListener {
                mainActivity.binding.bottomNavigation.selectedItemId = R.id.action_home
            }
            mainActivity?.binding.toolbalTitleImage?.visibility = View.GONE
            mainActivity?.binding.toolbalUsername?.visibility = View.VISIBLE
            mainActivity?.binding.toolbalBtnBack?.visibility = View.VISIBLE
            binding.accountBntFollowSignout?.setOnClickListener {
                requestFollow()
            }
        }

        binding.accountRecyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        binding.accountRecyclerview?.layoutManager = GridLayoutManager(requireActivity(), 3)

        binding.accountIvProfile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage()
        getFollowerAndFollowing()
        return binding.root

    }
    fun getFollowerAndFollowing() {
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot ==null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount != null) {
                binding.accountTvFollowingCount?.text = followDTO?.followingCount?.toString()
            }
            if(followDTO?.followerCount != null) {
                binding?.accountTvFollowerCount?.text = followDTO?.followerCount?.toString()
                if(followDTO?.followers?.containsKey(currentUseriD)!!) {
                    binding.accountBntFollowSignout?.text = getString(R.string.follow_cancel)
                    binding.accountBntFollowSignout?.background?.setColorFilter(ContextCompat.getColor(requireActivity(),R.color.colorLightGray),PorterDuff.Mode.MULTIPLY)

                }
                else
                {
                    if(uid != currentUseriD) {
                        binding.accountBntFollowSignout?.text = getString(R.string.follow)
                        binding.accountBntFollowSignout?.background?.colorFilter = null
                    }
                }
            }

        }

    }

    fun followerAlarm(destinationUid : String) {
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userID = auth?.currentUser?.email
        alarmDTO.userID = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        var message = auth?.currentUser?.email + getString(R.string.alarm_follow)
        FcmPush.instance.sendMessage(destinationUid,"Grenstagram",message)

    }



    fun getProfileImage() {
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null) {
                var uri = documentSnapshot?.data!!["image"]
                Glide.with(requireActivity()).load(uri).apply(RequestOptions().circleCrop()).into(binding.accountIvProfile!!)

            }
        }

    }

    fun requestFollow() {
        //내 게정의 팔로워들
        var tsDocFollowing = firestore?.collection("users")?.document(currentUseriD!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followers[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction
            }
            if (followDTO.followings.containsKey(uid)) {
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followers?.remove(uid)
            } else {
                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.followers[uid!!] = true
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction

        }
        //내가 following 할
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUseriD!!] = true
                followerAlarm(uid!!)
                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }
            if (followDTO!!.followers.containsKey(currentUseriD)) {
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUseriD)
            } else {
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUseriD!!] = true
                followerAlarm(uid!!)
            }
            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }
    }





    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                binding.accountTvPostCount?.text = contentDTOs.size.toString()
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
