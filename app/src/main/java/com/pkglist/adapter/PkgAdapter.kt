package com.pkglist.adapter

import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pkglist.R
import com.pkglist.databinding.ListItemPkgBinding
import android.graphics.drawable.Drawable
import android.util.Log


class PkgAdapter internal constructor(private val context: Context, val packages: ArrayList<ApplicationInfo>) : RecyclerView.Adapter<PkgAdapter.ViewHolder>() {
    private var mClickListener: ItemClickListener? = null
    private var mItemRemoved: ItemRemoved? = null

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemPkgBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pkg = packages[position]
        try {
            holder.bind(context, pkg)
        } catch (ex: java.lang.Exception) {
            Log.e("PkgAdapter", ex.toString())
        }
    }

    // total number of rows
    override fun getItemCount(): Int {
        return packages.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(private val binding: ListItemPkgBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }

        @Throws(Exception::class)
        fun bind(context: Context, appInfo: ApplicationInfo) {
            try {
                val icon: Drawable = context.packageManager.getApplicationIcon(appInfo.packageName)
                binding.ivIcon.setImageDrawable(icon)
            } catch (ex: Exception) {
                if (mItemRemoved != null) mItemRemoved!!.onItemRemoved(adapterPosition)
                throw ex
            }

            binding.tvPackageName.text = appInfo.packageName
            val label = context.packageManager.getApplicationLabel(appInfo)
            binding.tvName.text = label
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): ApplicationInfo {
        return packages[id]
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    interface ItemRemoved {
        fun onItemRemoved(position: Int)
    }
}