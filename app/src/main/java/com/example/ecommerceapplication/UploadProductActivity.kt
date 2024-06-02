package com.example.ecommerceapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.UUID

class UploadProductActivity : AppCompatActivity() {
    private lateinit var productPreviewImg: ImageView
    private lateinit var edtProductName: EditText
    private lateinit var edtProductPrice: EditText
    private lateinit var edtProductDes: EditText
    private lateinit var btnSelectProduct: Button
    private lateinit var btnUploadProduct: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_product)

        productPreviewImg = findViewById(R.id.img_product_preview)
        edtProductName = findViewById(R.id.edt_product_name)
        edtProductPrice = findViewById(R.id.edt_product_price)
        edtProductDes = findViewById(R.id.edt_product_des)
        btnSelectProduct = findViewById(R.id.btn_select_product)
        btnUploadProduct = findViewById(R.id.btn_upload_product)
        progressBar = findViewById(R.id.progress_bar)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                selectedImageUri = result.data!!.data
                productPreviewImg.setImageURI(selectedImageUri)
            }
        }

        btnSelectProduct.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(galleryIntent)
        }

        btnUploadProduct.setOnClickListener {
            if (selectedImageUri != null) {
                uploadProduct()
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadProduct() {
        val productName = edtProductName.text.toString()
        val productPrice = edtProductPrice.text.toString()
        val productDes = edtProductDes.text.toString()

        if (productName.isNotEmpty() && productPrice.isNotEmpty() && productDes.isNotEmpty()) {
            progressBar.visibility = View.VISIBLE

            val fileName = UUID.randomUUID().toString() + ".jpg"
            val storageRef = FirebaseStorage.getInstance().reference.child("productImages/$fileName")

            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val product = Product(productName, productDes, productPrice, uri.toString())
                        Firebase.database.getReference("products").child(productName).setValue(product)
                            .addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                Toast.makeText(this, "Product uploaded successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                progressBar.visibility = View.GONE
                                Toast.makeText(this, "Failed to upload product: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }
}
