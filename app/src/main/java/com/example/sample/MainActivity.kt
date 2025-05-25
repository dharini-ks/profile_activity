package com.example.sample

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProfileAdapter

    private lateinit var profileImage: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var editIcon: ImageView
    private lateinit var joinDateTextView: TextView

    // Image picker
    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = getBitmapFromUri(it)
            bitmap?.let { bmp ->
                val circularBitmap = getCircularBitmap(bmp)
                profileImage.setImageBitmap(circularBitmap)
                saveBitmapToInternalStorage(circularBitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        profileImage = findViewById(R.id.profileImage)
        nameTextView = findViewById(R.id.nameTextView)
        editIcon = findViewById(R.id.editIcon)
        joinDateTextView = findViewById(R.id.joinDateTextView)

        val savedName = loadNameFromPrefs()
        if (!savedName.isNullOrEmpty()) {
            nameTextView.text = savedName
        }

        val savedJoinDate = loadJoinDateFromPrefs()
        if (savedJoinDate == null) {
            val currentDate = getCurrentDateString()
            saveJoinDateToPrefs(currentDate)
            joinDateTextView.text = "Member since $currentDate"
        } else {
            joinDateTextView.text = "Member since $savedJoinDate"
        }

        val bitmap = loadBitmapFromInternalStorage()
        bitmap?.let {
            val circularBitmap = getCircularBitmap(it)
            profileImage.setImageBitmap(circularBitmap)
        }

        profileImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        editIcon.setOnClickListener {
            showEditNameDialog()
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProfileAdapter(emptyList())
        recyclerView.adapter = adapter

        profileViewModel.profileItems.observe(this) { items ->
            adapter.updateData(items)
        }
    }

    private fun showEditNameDialog() {
        val editText = EditText(this).apply {
            hint = "Enter your name"
            setText(nameTextView.text.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Name")
            .setView(editText)
            .setPositiveButton("Save") { dialog, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    nameTextView.text = newName
                    saveNameToPrefs(newName)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveNameToPrefs(name: String) {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("user_name", name).apply()
    }

    private fun loadNameFromPrefs(): String? {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_name", null)
    }

    private fun saveJoinDateToPrefs(dateString: String) {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("join_date", dateString).apply()
    }

    private fun loadJoinDateFromPrefs(): String? {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("join_date", null)
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap) {
        try {
            openFileOutput("profile_image.png", Context.MODE_PRIVATE).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadBitmapFromInternalStorage(): Bitmap? {
        return try {
            val file = File(filesDir, "profile_image.png")
            if (file.exists()) {
                android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            bitmap
        }

        val size = Math.min(bmp.width, bmp.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, size, size)

        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val left = (bmp.width - size) / 2
        val top = (bmp.height - size) / 2
        canvas.drawBitmap(bmp, -left.toFloat(), -top.toFloat(), paint)

        return output
    }
}
