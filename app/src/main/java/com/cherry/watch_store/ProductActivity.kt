package com.cherry.watch_store

import Adapter.ProductAdapter
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductActivity : AppCompatActivity() {
        private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter { product -> showDetailActivity(product) }
        recyclerView.adapter = productAdapter

        database = FirebaseDatabase.getInstance().getReference("products")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<Product>()
                snapshot.children.forEach {
                    val product = it.getValue(Product::class.java)
                    if (product != null) {
                        productList.add(product)
                    }
                }
                productAdapter.submitList(productList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun showDetailActivity(product: Product) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("product", product)
        startActivity(intent)
    }
}



