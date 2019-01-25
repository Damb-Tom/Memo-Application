package tom.memo

import android.app.AlertDialog
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.text.InputType
import android.widget.*


class MainAdapter(private val userList: ArrayList<MemoRefData>): RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mainTitle.text = userList[position].title
        holder.importanceTitle.text = userList[position].importance.toString()
        holder.itemView.tag = position

        // Colour the card backgrounds
        if (useColour) {
            when (holder.importanceTitle.text.toString().toInt()) {
                1 -> holder.importanceColour.setCardBackgroundColor(ContextCompat.getColor(holder.importanceColour.context, R.color.importance_low))
                2 -> holder.importanceColour.setCardBackgroundColor(ContextCompat.getColor(holder.importanceColour.context, R.color.importance_med))
                3 -> holder.importanceColour.setCardBackgroundColor(ContextCompat.getColor(holder.importanceColour.context, R.color.importance_high))
            }
        } else {
            holder.importanceColour.setCardBackgroundColor(ContextCompat.getColor(holder.importanceColour.context, R.color.cardview_dark_background))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return userList.size
    }


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnLongClickListener {

        private lateinit var database: DatabaseReference
        private lateinit var userId: String

        // Constructor
        init {
            val user = FirebaseAuth.getInstance().currentUser
            if(user != null) {
                userId = user.uid
                database = FirebaseDatabase.getInstance().reference.child("list").child(userId)
            }

            itemView.setOnLongClickListener(this)
        }


        override fun onLongClick(v: View?): Boolean {
            val modifyMemoDialogBox = AlertDialog.Builder(itemView.context)
            modifyMemoDialogBox.setTitle("What would you like to do?")

            // Delete Memo from database
            modifyMemoDialogBox.setPositiveButton("Delete") {
                    dialog, _ -> dialog.dismiss()
                sortedMemos[v!!.tag.toString().toInt()].title_ref.setValue(null)
                sortedMemos[v.tag.toString().toInt()].importance_ref.setValue(null)
                Toast.makeText(itemView.context, "Deleted Memo!", Toast.LENGTH_SHORT).show()
            }

            // Edit Memo from database
            modifyMemoDialogBox.setNegativeButton("Edit") {
                    dialog, _ -> dialog.cancel()

                val layout = LinearLayout(itemView.context)
                layout.orientation = LinearLayout.VERTICAL


                // Setup Dialog
                val editMemoDialogBox = AlertDialog.Builder(itemView.context)
                editMemoDialogBox.setTitle("Edit Memo")

                var memoName = EditText(itemView.context)
                memoName.hint = "Title"
                memoName.inputType = InputType.TYPE_CLASS_TEXT
                memoName.textAlignment = EditText.TEXT_ALIGNMENT_CENTER
                memoName.setText("${mainTitle.text}")
                memoName.setSelection(memoName.text.length)
                layout.addView(memoName)

                var importanceVal = EditText(itemView.context)
                importanceVal.hint = "Importance (1-3)"
                importanceVal.inputType = InputType.TYPE_CLASS_NUMBER
                importanceVal.textAlignment = EditText.TEXT_ALIGNMENT_CENTER
                importanceVal.setText("${importanceTitle.text}")
                layout.addView(importanceVal)

                editMemoDialogBox.setView(layout)
                editMemoDialogBox.setPositiveButton("Edit") {
                        dialog2, _ -> dialog2.dismiss()

                    // Validation
                    if(importanceVal.text.toString() == "") {
                        Toast.makeText(itemView.context, "Importance field is invalid", Toast.LENGTH_SHORT).show()
                        importanceVal.setText("")
                        return@setPositiveButton
                    }
                    if(importanceVal.text.toString().toInt() < 0 || importanceVal.text.toString().toInt() > 3)
                    {
                        Toast.makeText(itemView.context, "Importance must be 0-3", Toast.LENGTH_SHORT).show()
                        importanceVal.setText("")
                        return@setPositiveButton
                    }
                    if(memoName.text.toString() == "")
                    {
                        Toast.makeText(itemView.context, "Invalid Title", Toast.LENGTH_SHORT).show()
                        memoName.setText("")
                        return@setPositiveButton
                    }

                    // Edit Database Record
                    sortedMemos[v?.tag.toString().toInt()].title_ref.setValue("${memoName.text}")
                    sortedMemos[v?.tag.toString().toInt()].importance_ref.setValue("${importanceVal.text}")
                    Toast.makeText(itemView.context, "Edited Memo!", Toast.LENGTH_SHORT).show()
                }
                editMemoDialogBox.setNeutralButton("Cancel") {
                        dialog2, _ -> dialog2.cancel()
                }
                editMemoDialogBox.show()
            }

            modifyMemoDialogBox.setNeutralButton("Nothing") {
                    dialog, _ -> dialog.cancel()
            }
            modifyMemoDialogBox.show()
            return true
        }


        val mainTitle = itemView.findViewById<TextView>(R.id.text)!!
        val importanceTitle = itemView.findViewById<TextView>(R.id.importance_text)!!
        val importanceColour = itemView.findViewById<CardView>(R.id.card_view)!!
    }


}