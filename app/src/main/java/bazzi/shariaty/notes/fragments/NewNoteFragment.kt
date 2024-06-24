package bazzi.shariaty.notes.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import bazzi.shariaty.notes.R
import bazzi.shariaty.notes.databinding.FragmentNewNoteBinding
import bazzi.shariaty.notes.utils.NoteData
import com.google.android.material.textfield.TextInputEditText

class NewNoteFragment : DialogFragment() {

    private lateinit var binding: FragmentNewNoteBinding
    private lateinit var listener: DialogNextBtnClickListener
    private var noteData : NoteData? = null

    fun setListener(listener: HomeNoteFragment){
        this.listener = listener
    }

    companion object{
        const val TAG = "NewNoteFragment"

        @JvmStatic
        fun newInstance(noteId: String, title: String, note: String) = NewNoteFragment().apply {
            arguments = Bundle().apply {
                putString("noteId", noteId)
                putString("note", note)
                putString("title", title)

            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.FullScreenDialogStyle)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null) {
            noteData = NoteData(
                noteId = arguments?.getString("noteId").toString(),
                title = arguments?.getString("title").toString(),
                note = arguments?.getString("note").toString(),
                timestamp = arguments?.getString("timestamp").toString()
            )

            binding.etTitleBody.setText(noteData?.title)
            binding.etNoteBody.setText(noteData?.note)
        }


        registerEvents()
    }

    private fun registerEvents() {
        binding.saveNote.setOnClickListener {
            val noteTitle = binding.etTitleBody.text.toString()
            val noteBody = binding.etNoteBody.text.toString()

            if (noteTitle.isNotEmpty() || noteBody.isNotEmpty()) {
                if (noteData == null) {
                    listener.onSaveNote(noteTitle, noteBody, binding.etTitleBody, binding.etNoteBody)
                } else {
                    noteData?.title = noteTitle
                    noteData?.note = noteBody
                    listener.onUpdateNote(noteData!!, binding.etTitleBody, binding.etNoteBody)
                }
            } else {
                Toast.makeText(context, "عنوان یا یادداشت را بنویسید!", Toast.LENGTH_SHORT).show()
            }
        }
    }





    interface DialogNextBtnClickListener{
        fun onSaveNote(noteTitle: String, noteBody: String, etTitleBody: TextInputEditText, etNoteBody: TextInputEditText)
        fun onUpdateNote(noteData: NoteData, etTitleBody: TextInputEditText, etNoteBody: TextInputEditText)
    }

}