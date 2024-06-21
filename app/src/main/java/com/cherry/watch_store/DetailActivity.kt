package com.cherry.watch_store

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)

        val productName: TextView = findViewById(R.id.productEachName)
        val productDescription: TextView = findViewById(R.id.productEachDescription)
        val productPrice: TextView = findViewById(R.id.productEachPrice)
        val productImage: ImageView = findViewById(R.id.imageView)
        val checkoutButton: Button = findViewById(R.id.productEachAdd)

        val name = intent.getStringExtra("name")
        val description = intent.getStringExtra("description")
        val price = intent.getDoubleExtra("price", 0.0)
        val imageUrl = intent.getStringExtra("imageUrl")

        productName.text = name
        productDescription.text = description
        productPrice.text = "$${price}"
        Glide.with(this).load(imageUrl).into(productImage)

        checkoutButton.setOnClickListener {
            val checkoutIntent = Intent(this, Checkout::class.java).apply {
                putExtra("name", name)
                putExtra("description", description)
                putExtra("price", price)
                putExtra("imageUrl", imageUrl)
            }
            startActivity(checkoutIntent)
        }
    }
}