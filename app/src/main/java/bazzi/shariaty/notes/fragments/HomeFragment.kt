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
import bazzi.shariaty.notes.databinding.FragmentHomeBinding
import bazzi.shariaty.notes.utils.ToDoAdapter
import bazzi.shariaty.notes.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText
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


class HomeFragment : Fragment(), AddTodoPopupFragment.DialogNextBtnClickListener,
    ToDoAdapter.ToDoAdapterClickInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef : DatabaseReference
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeBinding
    private var popUpFragment : AddTodoPopupFragment? = null
    private lateinit var adapter: ToDoAdapter
    private lateinit var mList : MutableList<ToDoData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        getDataFromFirebase()
        registerEvents()
    }

    private fun registerEvents() {
        binding.noteTitleHeader.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_homeNoteFragment)
        }
        binding.addNoteBtn.setOnClickListener{
            if (popUpFragment != null)
                childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
            popUpFragment = AddTodoPopupFragment()
            popUpFragment!!.setListener(this)
            popUpFragment!!.show(
                childFragmentManager, AddTodoPopupFragment.TAG
            )
        }
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("Tasks").child(auth.currentUser?.uid.toString())

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = ToDoAdapter(mList)
        adapter.setListener(this)
        binding.recyclerView.adapter = adapter
    }

    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (taskSnapshot in snapshot.children) {
                    val todoTask = taskSnapshot.key?.let {
                        val task = taskSnapshot.child("task").value.toString()
                        val timestamp = taskSnapshot.child("timestamp").value.toString()
                        ToDoData(it, task, timestamp)
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

    override fun onSaveTask(todo: String, todoEt: TextInputEditText) {
        val taskId = databaseRef.push().key ?: ""
        val timestamp = getCurrentTimestamp()
        val todoData = ToDoData(taskId, todo, timestamp)

        databaseRef.child(taskId).setValue(todoData).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "یادداشت شما با موفقیت افزوده شد:)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "دوباره امتحان کنید", Toast.LENGTH_SHORT).show()
            }
            todoEt.text = null
            popUpFragment?.dismiss()
        }
    }

    // Function to get the current timestamp
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onUpdateTask(toDoData: ToDoData, todoEt: TextInputEditText) {
        val map = HashMap<String, Any>()
        map["task"] = toDoData.task
        map["timestamp"] = getCurrentTimestamp()

        databaseRef.child(toDoData.taskId).updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "یادداشت شما با موفقیت بروز شد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "دوباره امتحان کنید", Toast.LENGTH_SHORT).show()
            }
            todoEt.text = null
            popUpFragment!!.dismiss()
        }
    }

    override fun onDeleteTaskBtnClicked(toDoData: ToDoData) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("آیا از حذف این چک لیست مطمئن هستید؟")
            .setPositiveButton("بله") { dialog, id ->
                databaseRef.child(toDoData.taskId).removeValue().addOnCompleteListener {
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


    override fun onEditTaskBtnClicked(toDoData: ToDoData) {
        if (popUpFragment != null)
            childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
        popUpFragment = AddTodoPopupFragment.newInstance(toDoData.taskId, toDoData.task)
        popUpFragment!!.setListener(this)
        popUpFragment!!.show(childFragmentManager, AddTodoPopupFragment.TAG)
    }
}
