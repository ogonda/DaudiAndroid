package com.zeroq.daudi4native.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.StorageReference
import com.zeroq.daudi4native.R
import io.reactivex.subjects.PublishSubject

class UploadNotesAdapter(var storageReference: StorageReference) :
    RecyclerView.Adapter<UploadNotesAdapter.ViewHolder>() {

    private val paths = ArrayList<Pair<Boolean, String>>()
    private var context: Context? = null;
    var onLongPress = PublishSubject.create<Pair<Int, String>>()
    var onClick = PublishSubject.create<Pair<Int, String>>()
    var startCamera = PublishSubject.create<String>()


    /*
    * add new notes and replace the old ones
    * */
    fun replaceDeliveryNotes(notes: ArrayList<Pair<Boolean, String>>) {
        if (paths.size > 0) paths.clear()
        paths.addAll(notes)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context;

        val view =
            LayoutInflater.from(context)
                .inflate(
                    R.layout.upload_note_adapter,
                    parent, false
                )

        return ViewHolder(view)
    }

    override fun getItemCount() = paths.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = paths[position]

        context?.let {
            holder.bindDeliveryNote(it, note, storageReference)
        }

        /*
        * If the value is true, it means it a placeholder
        * */
        if (!note.first) {
            holder.noteImageView?.setOnLongClickListener {
                onLongPress.onNext(
                    Pair(
                        position,
                        note.second
                    )
                ); true
            }

            holder.noteImageView?.setOnClickListener { onClick.onNext(Pair(position, note.second)) }
        } else {
            // on press start camera instance
            holder.noteImageView?.setOnClickListener { startCamera.onNext("click") }
        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var noteImageView: ImageView? = null

        init {
            noteImageView = view.findViewById(R.id.postImageView)
        }

        fun bindDeliveryNote(
            context: Context,
            note: Pair<Boolean, String>,
            storageReference: StorageReference
        ) {
            val ONE_MEGABYTE = 1024 * 1024

            if (!note.first) {
                note.second.let {
                    val pathReference = storageReference.child(it)

                    pathReference.getBytes(ONE_MEGABYTE.toLong()).addOnSuccessListener { data ->
                        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                        noteImageView?.setImageBitmap(bitmap)
                    }
                }
            } else {
                noteImageView?.let {
                    Glide.with(context).load(R.drawable.icon_camera).centerCrop().into(it)
                }
            }
        }
    }
}