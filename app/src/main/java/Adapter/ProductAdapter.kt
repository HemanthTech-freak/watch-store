package Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cherry.watch_store.DetailActivity
import com.cherry.watch_store.Product
import com.cherry.watch_store.R

class ProductAdapter(private val onClick:(Product) -> Unit) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product, parent, false)
        return ProductViewHolder(view) { product ->
            val context = parent.context
            //intent with data for products
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra("name", product.name)
                putExtra("description", product.description)
                putExtra("price", product.price)
                putExtra("imageUrl", product.imageUrl)
            }
            context.startActivity(intent)
        }
    }

    //On bind view holder to bind the products
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    //Product view holder to display products on view
    class ProductViewHolder(itemView: View, val onClick: (Product) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productDescription : TextView = itemView.findViewById(R.id.productDescription)
        private val knowMoreBtn : Button = itemView.findViewById(R.id.knowMoreBtn)
        private var currentProduct: Product? = null

        //Click listeners for both image click and button click
        init {
            itemView.setOnClickListener {
                currentProduct?.let {
                    onClick(it)
                }
            }

            knowMoreBtn.setOnClickListener {
                currentProduct?.let {
                    onClick(it)
                }
            }

        }

        //Function to bind the data of products
        fun bind(product: Product) {
            currentProduct = product
            productName.text = product.name
            productPrice.text = "$${product.price}"
            productDescription.text = product.description
            Glide.with(productImage.context).load(product.imageUrl).into(productImage)
        }
    }

    //callback functions to compare the old item and the new items
    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

}