package bazzi.shariaty.notes.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import bazzi.shariaty.notes.R
import bazzi.shariaty.notes.databinding.FragmentHomeNoteBinding
import bazzi.shariaty.notes.utils.NoteAdapter
import bazzi.shariaty.notes.utils.NoteData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.AlertDialog
import android.content.DialogInterface


class HomeNoteFragment : Fragment(), NewNoteFragment.DialogNextBtnClickListener, NoteAdapter.NoteAdapterClickInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeNoteBinding
    private var newNoteFragment: NewNoteFragment? = null
    private lateinit var adapter: NoteAdapter
    private lateinit var mList: MutableList<NoteData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getDataFromFirebase()
        registerEvents()
    }

    private fun registerEvents() {
        binding.todoTitleHeader.setOnClickListener {
            navController.navigate(R.id.action_homeNoteFragment_to_homeFragment)
        }
        binding.fabAddNote.setOnClickListener {
            if (newNoteFragment != null)
                childFragmentManager.beginTransaction().remove(newNoteFragment!!).commit()
            newNoteFragment = NewNoteFragment()
            newNoteFragment!!.setListener(this)
            newNoteFragment!!.show(childFragmentManager, NewNoteFragment.TAG)
        }
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("Notes").child(auth.currentUser?.uid.toString())

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = NoteAdapter(mList)
        adapter.setListener(this)
        binding.recyclerView.adapter = adapter
    }

    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (taskSnapshot in snapshot.children) {
                    val todoTask = taskSnapshot.key?.let {
                        val title = taskSnapshot.child("title").value.toString()
                        val note = taskSnapshot.child("note").value.toString()
                        val timestamp = taskSnapshot.child("timestamp").value.toString()
                        NoteData(it, title, note, timestamp)
                    }

                    if (todoTask != null) {
                        mList.add(todoTask)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSaveNote(
        title: String,
        note: String,
        etTitleBody: TextInputEditText,
        etNoteBody: TextInputEditText
    ) {
        val noteId = databaseRef.push().key ?: ""
        val timestamp = getCurrentTimestamp() // دریافت زمان فعلی
        val noteData = NoteData(noteId, title, note, timestamp)
        databaseRef.child(noteId).setValue(noteData).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "یادداشت شما با موفقیت افزوده شد:)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "دوباره امتحان کنید", Toast.LENGTH_SHORT).show()
            }
            etTitleBody.text = null
            etNoteBody.text = null
            newNoteFragment?.dismiss()
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onUpdateNote(noteData: NoteData, etNoteBody: TextInputEditText, etTitleBody: TextInputEditText) {
        val map = HashMap<String, Any>()
        map["title"] = noteData.title
        map["note"] = noteData.note
        map["timestamp"] = getCurrentTimestamp() // به روزرسانی timestamp

        databaseRef.child(noteData.noteId).updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "یادداشت شما با موفقیت بروز شد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "دوباره امتحان کنید", Toast.LENGTH_SHORT).show()
            }
            etTitleBody.text = null
            etNoteBody.text = null
            newNoteFragment!!.dismiss()
        }
    }

    override fun onNoteClick(noteData: NoteData) {
        Toast.makeText(context, "یادداشت انتخاب شده: ${noteData.title}", Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteTaskBtnClicked(noteData: NoteData) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("آیا از حذف این یادداشت مطمئن هستید؟")
            .setPositiveButton("بله") { dialog, id ->
                databaseRef.child(noteData.noteId).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "یادداشت شما با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "دوباره امتحان کنید", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("خیر") { dialog, id ->
                dialog.dismiss()
            }
            .setNeutralButton("لغو") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }


    override fun onEditTaskBtnClicked(noteData: NoteData) {
        if (newNoteFragment != null)
            childFragmentManager.beginTransaction().remove(newNoteFragment!!).commit()
        newNoteFragment = NewNoteFragment.newInstance(noteData.noteId, noteData.title, noteData.note)
        newNoteFragment!!.setListener(this)
        newNoteFragment!!.show(childFragmentManager, NewNoteFragment.TAG)
    }

}
