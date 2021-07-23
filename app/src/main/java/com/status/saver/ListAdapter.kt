package com.status.saver


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.status.saver.SavedFragment.Companion.flag
import com.status.saver.StatusFragment.Companion.selectedItems
import com.status.saver.StatusFragment.Companion.wpShare
import java.net.URLConnection
import java.util.*


class ListAdapter(private val context: Context, private var oldDataList: List<Model>) :
    RecyclerView.Adapter<MyViewHolder>() {
   var mActionMode: ActionMode? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_media_row_item, parent, false)
        val holder=MyViewHolder(view)

        return holder
    }

    private fun isVideoFile(path: String?): Boolean {
        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("video")
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = oldDataList[position]
        val currentFile = model.file
        val imgUri: Uri = currentFile.toUri()
        if (isVideoFile(imgUri.path)) {
           holder.play.visibility=View.VISIBLE
        }
        else
        {
           holder.play.visibility=View.GONE
        }
        if (model.isSelected) {
            holder.cardViewImageMedia.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.teal_700
                )
            )
            holder.imageViewImageMedia.setColorFilter(R.color.teal_700)
        } else {
            holder.cardViewImageMedia.setCardBackgroundColor(Color.WHITE)
            holder.imageViewImageMedia.clearColorFilter()

        }



        holder.wp.setOnClickListener {

            val whatsappIntent = Intent(Intent.ACTION_SEND)
            whatsappIntent.type = "text/plain"
            whatsappIntent.setPackage("com.whatsapp")

            val uri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                currentFile
            )

            whatsappIntent.putExtra(Intent.EXTRA_STREAM, uri)
            whatsappIntent.type = "image/jpeg"
            whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                context.startActivity(whatsappIntent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(context, "Something Went Wrong..", Toast.LENGTH_LONG).show()
            }
            wpShare=true
           // selectedItems.clear()
        }


        holder.cardViewImageMedia.setOnLongClickListener {
            it as CardView
            model.isSelected = !model.isSelected
            if (model.isSelected) {
                holder.cardViewImageMedia.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.teal_700
                    )
                )
                holder.imageViewImageMedia.setColorFilter(R.color.teal_700)
                selectedItems.add(model)
                if (mActionMode != null) {
                    return@setOnLongClickListener true
                }
                mActionMode=(context as MainActivity).startSupportActionMode(mActionModeCallback)

                context.tabLayout.visibility=View.GONE
                flag = true
            } else {
                holder.cardViewImageMedia.setCardBackgroundColor(Color.WHITE)
                holder.imageViewImageMedia.clearColorFilter()
                selectedItems.remove(model)
             if(selectedItems.isEmpty()&& mActionMode!=null)
             {
                 mActionMode!!.finish()
                 (context as MainActivity).tabLayout.visibility=View.VISIBLE
             }

                flag = true
            }


            return@setOnLongClickListener true
        }

        holder.cardViewImageMedia.setOnClickListener {
            if (selectedItems.isEmpty()) {
                flag = false
            }
            if (!flag) {
                context.startActivity(Intent(context, ContentViewer::class.java).also {
                    it.putExtra("uri", imgUri)


                })

            } else {
                it as CardView
                model.isSelected = !model.isSelected
                if (model.isSelected) {
                    holder.imageViewImageMedia.setColorFilter(R.color.teal_700)
                    it.setCardBackgroundColor(ContextCompat.getColor(context, R.color.teal_700))
                    selectedItems.add(model)

                } else {

                    it.setCardBackgroundColor(Color.WHITE)
                    holder.imageViewImageMedia.clearColorFilter()
                    selectedItems.remove(model)
                    if(selectedItems.isEmpty() &&  mActionMode!=null)
                        mActionMode!!.finish()

                }
            }


        }



        Glide
            .with(context)
            .load(currentFile)
            .apply(RequestOptions().override(200, 200))
            .into(holder.imageViewImageMedia)
    }

    override fun getItemCount(): Int {
        return oldDataList.size
    }

    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            if (mode != null) {
                mode.getMenuInflater().inflate(R.menu.select_all, menu)
               // mode.setTitle("Choose your option");
            };

            return true;
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item!!.itemId) {
                R.id.option_1 -> {
                    mode?.let {

                        oldDataList.forEach {
                            it.isSelected = true
                        }
                        selectedItems.clear()
                        selectedItems.addAll(oldDataList)
                        //  it.finish()
                        notifyDataSetChanged()
                    }
                    true
                }
                R.id.option_2 -> {

                    mode?.let {

                        selectedItems.forEach {
                            val index = oldDataList.indexOf(it)
                            if(index==-1) return@forEach
                            oldDataList[index].isSelected = false
                        }
                        selectedItems.clear()
                        it.finish()
                    }
                    true
                }
                else ->
                {
                    Toast.makeText(context,"bak",Toast.LENGTH_SHORT).show()
                  false

                }
            }

        }

        override fun onDestroyActionMode(mode: ActionMode?) {
           mActionMode=null

            (context as MainActivity).tabLayout.visibility=View.VISIBLE
            if(selectedItems.isNotEmpty())
            {
                selectedItems.forEach {
                    val index = oldDataList.indexOf(it)
                    if(index==-1) return@forEach
                    oldDataList[index].isSelected = false
                }
                selectedItems.clear()

            }
            notifyDataSetChanged()
        }


    }


    }
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageViewImageMedia: ImageView = itemView.findViewById(R.id.imageViewImageMedia)
        var wp = itemView.findViewById<ImageButton>(R.id.wpshare)
        val play= itemView.findViewById<ImageView>(R.id.play)
        var cardViewImageMedia: CardView = itemView.findViewById(R.id.cardViewImageMedia)

    }


