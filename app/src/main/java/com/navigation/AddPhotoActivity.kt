package com.navigation

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.gre_nstagram.R
import com.example.gre_nstagram.databinding.ActivityAddPhotoBinding
import com.example.gre_nstagram.databinding.ActivityLoginActivtyBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.navigation.model.ContentDTO
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUrl : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null


    val binding by lazy { ActivityAddPhotoBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        storage  = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        var photoPickIntent = Intent(Intent.ACTION_PICK)
        photoPickIntent.type ="image/*"
        startActivityForResult(photoPickIntent,PICK_IMAGE_FROM_ALBUM)
        binding.addphotoBtnUpload.setOnClickListener {
            contentUpload()
        }
        
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM) {
            if(resultCode == Activity.RESULT_OK) {
                photoUrl = data?.data
                binding.addphotoImage.setImageURI(photoUrl)
            }
            else {
                finish()
            }
        }

    }
    fun contentUpload() {
        var timestamp = SimpleDateFormat("yyyyMMdd__HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)
        //callback 방식
//        storageRef?.putFile(photoUrl!!)?.addOnSuccessListener {
//            storageRef.downloadUrl.addOnSuccessListener { uri ->
//                var contentDTO = ContentDTO()
//
//                contentDTO.imageUrl = uri.toString()
//                contentDTO.uid = auth?.currentUser?.uid
//                contentDTO.userId = auth?.currentUser?.email
//                contentDTO.explain = binding.addphotoEditExplain.text.toString()
//                contentDTO.timestamp = System.currentTimeMillis()
//
//                firestore?.collection("images")?.document()?.set(contentDTO)
//                setResult(Activity.RESULT_OK)
//                finish()
//            }
//
//        }
        //promise 방식
        storageRef?.putFile(photoUrl!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->

            var contentDTO = ContentDTO()

            contentDTO.imageUrl = uri.toString()
            contentDTO.uid = auth?.currentUser?.uid
            contentDTO.userId = auth?.currentUser?.email
            contentDTO.explain = binding.addphotoEditExplain.text.toString()
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)
                ?.addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                ?.addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            setResult(Activity.RESULT_OK)
            finish()

        }


    }
}