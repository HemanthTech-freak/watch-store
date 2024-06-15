package Adapter

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
import com.cherry.watch_store.Product
import com.cherry.watch_store.R

class ProductAdapter(private val onClick:(Product) -> Unit) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product, parent, false)
        return ProductViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(itemView: View, val onClick: (Product) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productDescription : TextView = itemView.findViewById(R.id.productDescription)
        private val knowMoreBtn : Button = itemView.findViewById(R.id.knowMoreBtn)
        private var currentProduct: Product? = null

        init {
            itemView.setOnClickListener {
                currentProduct?.let {
                    onClick(it)
                }
            }
        }

        fun bind(product: Product) {
            currentProduct = product
            productName.text = product.name
            productPrice.text = "$${product.price}"
            productDescription.text = product.description
            Glide.with(productImage.context).load(product.imageUrl).into(productImage)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

}