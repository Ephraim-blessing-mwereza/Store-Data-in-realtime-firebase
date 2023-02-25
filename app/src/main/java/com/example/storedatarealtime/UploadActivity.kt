package com.example.storedatarealtime

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UploadActivity : AppCompatActivity() {
    var uploadImage: ImageView? = null
    var saveButton: Button? = null
    var uploadTopic: EditText? = null
    var uploadDesc: EditText? = null
    var uploadLang: EditText? = null
    var imageURL: String? = null
    var uri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        uploadImage = findViewById(R.id.uploadImage)
        uploadDesc = findViewById(R.id.uploadDesc)
        uploadTopic = findViewById(R.id.uploadTopic)
        uploadLang = findViewById(R.id.uploadLang)
        saveButton = findViewById(R.id.saveButton)
        val activityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                uri = data!!.data
                uploadImage.setImageURI(uri)
            } else {
                Toast.makeText(this@UploadActivity, "No Image selected!", Toast.LENGTH_SHORT).show()
            }
        }
        uploadImage.setOnClickListener(View.OnClickListener {
            val photoPicker = Intent(Intent.ACTION_PICK)
            photoPicker.type = "image/*"
            activityResultLauncher.launch(photoPicker)
        })
        saveButton.setOnClickListener(View.OnClickListener { //pending
            saveData()
        })
    }

    fun saveData() {
        val storageReference = FirebaseStorage.getInstance().reference.child("Android Images")
            .child(uri!!.lastPathSegment!!)
        val builder = AlertDialog.Builder(this@UploadActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()
        storageReference.putFile(uri!!).addOnSuccessListener { taskSnapshot ->
            val uriTask = taskSnapshot.storage.downloadUrl
            while (!uriTask.isComplete);
            val urlImage = uriTask.result
            imageURL = urlImage.toString()
            uploadData()
            dialog.dismiss()
        }.addOnFailureListener { dialog.dismiss() }
    }

    fun uploadData() {
        val title = uploadTopic!!.text.toString()
        val desc = uploadDesc!!.text.toString()
        val lang = uploadLang!!.text.toString()
        val dataClass = DataClass(title, desc, lang, imageURL)
        FirebaseDatabase.getInstance().getReference("Android Tutorials").child(title)
            .setValue(dataClass).addOnCompleteListener {
                Toast.makeText(this@UploadActivity, "saved!", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this@UploadActivity,
                    e.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}