package com.example.gre_nstagram

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gre_nstagram.databinding.ActivityLoginActivtyBinding
import com.facebook.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import java.util.*


class LoginActivty : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    val binding by lazy { ActivityLoginActivtyBinding.inflate(layoutInflater) }
    var callbackManager : CallbackManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_activty)
        auth = FirebaseAuth.getInstance()
        setContentView(binding.root)
        binding.emailLoginButton.setOnClickListener {
            signInAndSignUp()
        }
        binding.googleLoginButton.setOnClickListener {
            googleLogin()
        }
        binding.facebookLoginButton.setOnClickListener {
            facebookLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("250206723835-qbse25sqhqsa5l7nkamqavm45fvlu94h.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)
        //printHashKey()
        callbackManager = CallbackManager.Factory.create()

    }
    fun facebookLogin() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    handleFacebookAccessToken(result?.accessToken)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }
            })
    }
    fun handleFacebookAccessToken(token: AccessToken?) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    moveMainPage(task.result?.user)

                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()

                }
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode,resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess) {
                var account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }
    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
        ?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                moveMainPage(task.result?.user)

            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()

            }
        }
    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }
    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName,PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }
    //HqZh0fiXIlzAvPjMOHTQCZk7Rcc=
    fun googleLogin() {
        var signIntent = googleSignInClient?.signInIntent
        var bundle = Bundle()
        bundle.putString("GOOGLE_LOGIN_CODE",GOOGLE_LOGIN_CODE.toString())
        //startActivity(signIntent,bundle)
        startActivityForResult(signIntent,GOOGLE_LOGIN_CODE)
    }

    fun signInAndSignUp() {
        auth?.createUserWithEmailAndPassword(
            binding.emailEdittext.text.toString(),
            binding.passwordEdittext.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                moveMainPage(task.result?.user)
            } else if (task.exception?.message.isNullOrEmpty()) {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            } else {
                signinEmail()
            }
        }


    }

    fun signinEmail() {
        auth?.createUserWithEmailAndPassword(
            binding.emailEdittext.text.toString(),
            binding.passwordEdittext.text.toString()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                moveMainPage(task.result?.user)

            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()

            }
        }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }


}