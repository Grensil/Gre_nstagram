package com.example.gre_nstagram

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gre_nstagram.databinding.ActivityAddPhotoBinding
import com.example.gre_nstagram.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.navigation.*
import com.navigation.util.FcmPush
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    //private var mBinding: ActivityMainBinding? = null // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    //private val binding get() = mBinding!!

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //mBinding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            // 얘도 바구셈 ReleaseListener로 그게머임
            binding.bottomNavigation.setOnItemSelectedListener(onNavigationSelectedListener(applicationContext));

            //binding.bottomNavigation.OnNavigationItemReselectedListener()
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            binding.bottomNavigation.selectedItemId = R.id.action_home
            registerPushToken()

        }

    fun registerPushToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                //Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            val map = mutableMapOf<String,Any>()
            map["pushToken"] = token!!

            FirebaseFirestore.getInstance().collection("pushtokens").document(uid!!).set(map)

        })


    }

//    override fun onStop() {
//        super.onStop()
//        FcmPush.instance.sendMessage("MKmQZg9LThhF3GdLoeauC0gRavy1","hi","bye")
//
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri ->
                var map = HashMap<String,Any>()
                map["image"] = uri.toString()
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
            }

        }
    }

    inner class onNavigationSelectedListener(private val context:Context) : NavigationBarView.OnItemSelectedListener{
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            setToobalDeafult()
            when (item.itemId) {
                R.id.action_home -> { //이게 내 목록 보기 일걸 여기다 걸고 해보게
                    val detailViewFragment = DetailViewFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, detailViewFragment).commitAllowingStateLoss();
                    true
                }
                R.id.action_search -> {
                    val gridFragment = GridFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, gridFragment).commit()
                    true
                }
                R.id.action_add_photo -> {
                    if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
                        startActivity(Intent(context, AddPhotoActivity::class.java))
                    }
                    true
                }
                R.id.action_favorite_alarm -> {
                    val alarmFragment = AlarmFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, alarmFragment).commit()
                    true
                }
                R.id.action_account -> {
                    val userFragment = UserFragment()
                    var bundle = Bundle()
                    var uid = FirebaseAuth.getInstance().currentUser?.uid
                    bundle.putString("destinationUid",uid)
                    userFragment.arguments = bundle
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, userFragment).commit()
                    true
                }
                else -> false
            }
            //}
            return false

        }
        fun setToobalDeafult() {
            binding.toolbalUsername.visibility = View.GONE
            binding.toolbalBtnBack.visibility = View.GONE
            binding.toolbalTitleImage.visibility = View.VISIBLE

        }


    }

//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        //binding.bottomNavigation.setOnNavigationItemSelectedListener { it ->
//            when (item.itemId) {
//                R.id.action_home -> { //이게 내 목록 보기 일걸 여기다 걸고 해보게
//                    val detailViewFragment = DetailViewFragment()
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.main_content, detailViewFragment).commitAllowingStateLoss();
//                    true
//                }
//                R.id.action_search -> {
//                    val gridFragment = GridFragment()
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.main_content, gridFragment).commit()
//                    true
//                }
//                R.id.action_add_photo -> {
//                    if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
//                        startActivity(Intent(this,AddPhotoActivity::class.java))
//                    }
//                    true
//                }
//                R.id.action_favorite_alarm -> {
//                    val alarmFragment = AlarmFragment()
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.main_content, alarmFragment).commit()
//                    true
//                }
//                R.id.action_account -> {
//                    val userFragment = UserFragment()
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.main_content, userFragment).commit()
//                    true
//                }
//                else -> false
//            }
//        //}
//        return true
//
//    }


}