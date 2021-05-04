package whatsapps.status.saver

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import whatsapps.status.saver.ListAdapter.MyViewHolder
import whatsapps.status.saver.SavedFragment.Companion.flag
import whatsapps.status.saver.StatusFragment.Companion.selectedItems
import java.util.*


class ListAdapter(private val context: Context, private val modelFeedArrayList: ArrayList<Model>) :
    RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_media_row_item, parent, false)
           return MyViewHolder(view)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model=modelFeedArrayList[position]
        val currentFile = model.file
        val imgUri: Uri = currentFile.toUri()

        if(model.isSelected)
        {
            holder.cardViewImageMedia.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.teal_700
                )
            )
            holder.imageViewImageMedia.setColorFilter(R.color.teal_700)
        }
        else
        {
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
        }


        holder.cardViewImageMedia.setOnLongClickListener { it as CardView
            model.isSelected=!model.isSelected
            if(model.isSelected)
            {
                holder.cardViewImageMedia.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.teal_700
                    )
                )
                holder.imageViewImageMedia.setColorFilter(R.color.teal_700)
                selectedItems.add(currentFile)
                flag=true
            }
            else
            {
                holder.cardViewImageMedia.setCardBackgroundColor(Color.WHITE)
                holder.imageViewImageMedia.clearColorFilter()
                selectedItems.remove(currentFile)
                flag=false
            }


            return@setOnLongClickListener true
        }

        holder.cardViewImageMedia.setOnClickListener {
            if(selectedItems.isEmpty())
            {
                flag=false
            }
            if(!flag)
            {
                context.startActivity(Intent(context, ContentViewer::class.java).also {
                    it.putExtra("uri", imgUri)
                })
            }
           else
            {
                it as CardView
                model.isSelected=!model.isSelected
                if(model.isSelected)
                {
                    holder.imageViewImageMedia.setColorFilter(R.color.teal_700)
                    it.setCardBackgroundColor(ContextCompat.getColor(context, R.color.teal_700))
                    selectedItems.add(currentFile)

                }
                else
                {

                    it.setCardBackgroundColor(Color.WHITE)
                    holder.imageViewImageMedia.clearColorFilter()
                    selectedItems.remove(currentFile)
                }
            }


          }



            Glide
                .with(context)
                .load(currentFile)
                .apply(RequestOptions().override(150, 150))
                .into(holder.imageViewImageMedia)
        }

    override fun getItemCount(): Int {
        return modelFeedArrayList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageViewImageMedia: ImageView = itemView.findViewById(R.id.imageViewImageMedia)
        var wp=itemView.findViewById<ImageButton>(R.id.wpshare)
        var cardViewImageMedia: CardView = itemView.findViewById(R.id.cardViewImageMedia)

    }




}