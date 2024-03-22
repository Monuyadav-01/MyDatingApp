package com.example.mydatingapp.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mydatingapp.MainActivity
import com.example.mydatingapp.databinding.ActivityResisterBinding
import com.example.mydatingapp.model.UserModel
import com.example.mydatingapp.utils.Config
import com.example.mydatingapp.utils.Config.hideDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast

class ResisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResisterBinding
    private var imageUri: Uri? = null
    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it

        binding.userImage.setImageURI(imageUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.userImage.setOnClickListener {
            selectImage.launch("image/*")
        }

        binding.saveData.setOnClickListener {
            validateData()

        }
    }

    private fun validateData() {
        if (binding.userName.text.toString().isEmpty() || binding.userEmail.text.toString()
                .isEmpty() || binding.userCity.toString().isEmpty() || imageUri == null
        ) {
            FancyToast.makeText(
                this,
                "Please enter all fields",
                FancyToast.LENGTH_LONG,
                FancyToast.WARNING,
                true
            ).show()
        } else if (!binding.termsConditions.isChecked) {
            FancyToast.makeText(
                this,
                "Please accept terms and conditions",
                FancyToast.LENGTH_LONG,
                FancyToast.WARNING,
                true
            ).show()
        } else {
            uploadImage()
        }
    }

    private fun uploadImage() {
        Config.showDialog(this)

        val storageRef = FirebaseStorage.getInstance().getReference("profile")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("profile.jpg")

        storageRef.putFile(imageUri!!).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener {
                storeData(it)
            }.addOnFailureListener {
                hideDialog()
                FancyToast.makeText(
                    this,
                    "Some Thing went wrong",
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    true
                ).show()
            }

        }.addOnFailureListener {
            hideDialog()
            FancyToast.makeText(
                this,
                "Some Thing went wrong",
                FancyToast.LENGTH_LONG,
                FancyToast.ERROR,
                true
            ).show()
        }
    }

    private fun storeData(imageUrl: Uri?) {

        val data = UserModel(

            name = binding.userName.text.toString(),
            image = imageUrl.toString(),
            email = binding.userEmail.text.toString(),
            city = binding.userCity.text.toString(),

            )

        FirebaseDatabase.getInstance().getReference("users")
            .child(FirebaseAuth.getInstance().currentUser!!.phoneNumber!!).setValue(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()

                    FancyToast.makeText(
                        this,
                        "User register successfully",
                        FancyToast.LENGTH_LONG,
                        FancyToast.SUCCESS,
                        true
                    ).show()

                } else {

                    FancyToast.makeText(
                        this,
                        "Some Thing went wrong",
                        FancyToast.LENGTH_LONG,
                        FancyToast.ERROR,
                        true
                    ).show()

                }
            }

    }

}
