package com.example.mydatingapp.auth

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.mydatingapp.MainActivity
import com.example.mydatingapp.R
import com.example.mydatingapp.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val auth = FirebaseAuth.getInstance()
    private var verificationId: String? = null

    private lateinit var alertDialog: AlertDialog

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alertDialog =
            AlertDialog.Builder(this).setView(R.layout.loading_layout).setCancelable(false).create()


        // for send otp


        binding.sendOtp.setOnClickListener {
//            binding.sendOtp.showLoadingButton()
            if (binding.userNumber.text!!.isEmpty()) {
                binding.userNumber.error = "enter your number"
            } else {
                sendOtp(binding.userNumber.text.toString())

            }
        }
        // for verify otp


        binding.verifyOtp.setOnClickListener {
            if (binding.userOtp.text!!.isEmpty()) {
                binding.userOtp.error = "enter otp"
            } else {
                verifyOtp(binding.userOtp.text.toString())
            }
        }
    }


    // function for send otp to user

    private fun sendOtp(number: String) {
        alertDialog.show()

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {

                } else if (e is FirebaseTooManyRequestsException) {

                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {

                }

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {

                alertDialog.dismiss()

                Log.d(TAG, "onCodeSent:$verificationId")
                this@LoginActivity.verificationId = verificationId
                binding.numberLayout.isVisible = false
                binding.otpLayout.isVisible = true


            }
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$number") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    // function for verify otp for user

    private fun verifyOtp(otp: String) {
//        binding.sendOtp.showLoadingButton()
        alertDialog.show()
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        signInWithPhoneAuthCredential(credential)

    }

    // signing with phone credential


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
//                binding.sendOtp.showNormalButton()
                if (task.isSuccessful) {

                    checkUserExist(binding.userNumber.text.toString())
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
                    Log.d(TAG, "signInWithCredential:success")

                } else {
                    alertDialog.dismiss()
                    FancyToast.makeText(this,"please enter correct number",FancyToast.LENGTH_LONG,FancyToast.ERROR,true);
                }
            }
    }

    private fun checkUserExist(number: String) {
        FirebaseDatabase.getInstance().getReference("users").child(number).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                alertDialog.dismiss()
                if (snapshot.exists()) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@LoginActivity, ResisterActivity::class.java))
                    finish()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                alertDialog.dismiss()
                Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_LONG).show()
            }

        })
    }
}