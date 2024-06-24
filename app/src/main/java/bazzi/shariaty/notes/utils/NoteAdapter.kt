package bazzi.shariaty.notes.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bazzi.shariaty.notes.databinding.NoteLayoutAdapterBinding
import bazzi.shariaty.notes.fragments.HomeNoteFragment


class NoteAdapter(private val list: MutableList<NoteData>) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var listener: NoteAdapterClickInterface? = null

    fun setListener(listener: HomeNoteFragment) {
        this.listener = listener
    }

    inner class NoteViewHolder(val binding: NoteLayoutAdapterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteLayoutAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = list[position]
        holder.binding.tvNoteTitle.text = note.title
        holder.binding.tvNoteBody.text = note.note
        holder.binding.tvTimestamp.text = note.timestamp // Set the timestamp text

        holder.binding.root.setOnClickListener {
            listener?.onNoteClick(note)
        }

        holder.binding.deleteNote.setOnClickListener {
            listener?.onDeleteTaskBtnClicked(note)
        }

        holder.binding.editNote.setOnClickListener {
            listener?.onEditTaskBtnClicked(note)
        }
    }

    interface NoteAdapterClickInterface {
        fun onNoteClick(noteData: NoteData)
        fun onDeleteTaskBtnClicked(noteData: NoteData)
        fun onEditTaskBtnClicked(noteData: NoteData)
    }
}
