package tom.memo

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.database.DataSnapshot




class MainActivity : FragmentActivity() {

    // Database Variables
    private lateinit var database: DatabaseReference
    private var unsortedMemos = ArrayList<MemoRefData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load Colour Setting from previous session
        var settings = applicationContext.getSharedPreferences("USER_DATA", 0)
        useColour = settings.getBoolean("useColour", false)
        colour_check.isChecked = useColour

        // Progress Bar Loading
        progress_bar.visibility = View.VISIBLE

        val user = FirebaseAuth.getInstance().currentUser
        val userID = user!!.uid

        database = FirebaseDatabase.getInstance().reference


        database.child("list").child(userID).orderByKey()
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    // Clear Arrays because they'll have kept data from the previous 'onDataChange'
                    unsortedMemos.clear()
                    sortedMemos.clear()

                    // Grab Data unsorted
                    for (ds in dataSnapshot.children) {
                        unsortedMemos.add(MemoRefData(ds.child("title").value.toString(), ds.child("importance").value.toString().toInt(), ds.child("title").ref, ds.child("importance").ref))
                    }

                    // Sort Data and move it to the array used
                    var newTempMemo = unsortedMemos.sortedWith(compareBy { it.importance})
                    for(obj in newTempMemo){
                        sortedMemos.add(obj)
                    }
                    sortedMemos.reverse()


                    // Populate RecyclerView
                    memo_list.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayout.VERTICAL, false)
                    memo_list.adapter = MainAdapter(sortedMemos)

                    progress_bar.visibility = View.INVISIBLE
            }
        })

        colour_check.setOnCheckedChangeListener { _, isChecked ->
            useColour = isChecked

            // Save Data Locally
            settings = applicationContext.getSharedPreferences("USER_DATA", 0)
            val editor = settings.edit()
            editor.putBoolean("useColour", useColour)
            editor.apply()

            // Refresh List
            memo_list.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayout.VERTICAL, false)
            memo_list.adapter = MainAdapter(sortedMemos)
        }


        add_memo_button.setOnClickListener {
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL

            val dialogAlert2 = AlertDialog.Builder(this)
            dialogAlert2.setTitle("Create Memo")

            var memoName = EditText(this)
            memoName.hint = "Title"
            memoName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            memoName.textAlignment = EditText.TEXT_ALIGNMENT_CENTER
            layout.addView(memoName)

            var importanceVal = EditText(this)
            importanceVal.hint = "Importance (1-3)"
            importanceVal.inputType = InputType.TYPE_CLASS_NUMBER
            importanceVal.textAlignment = EditText.TEXT_ALIGNMENT_CENTER
            layout.addView(importanceVal)

            dialogAlert2.setView(layout)
            dialogAlert2.setPositiveButton("Create") {
                    dialog, _ -> dialog.dismiss()

                if(importanceVal.text.toString() == "") {
                    Toast.makeText(this, "Importance field is invalid", Toast.LENGTH_SHORT).show()
                    importanceVal.setText("")
                    return@setPositiveButton
                }

                if (importanceVal.text.toString().toInt() < 0 || importanceVal.text.toString().toInt() > 3) {
                    Toast.makeText(this, "Importance must be 0-3", Toast.LENGTH_SHORT).show()
                    importanceVal.setText("")
                    return@setPositiveButton
                }

                if(memoName.text.toString() == "")
                {
                    Toast.makeText(this, "Invalid Title", Toast.LENGTH_SHORT).show()
                    memoName.setText("")
                    return@setPositiveButton
                }

                if(memoName.text.toString() != "" && importanceVal.text.toString().toInt() >= 0 && importanceVal.text.toString().toInt() <= 3) {
                    val data = MemoData(memoName.text.toString(), importanceVal.text.toString().toInt())
                    database.child("list").child(userID).push().setValue(data)
                    Toast.makeText(baseContext, "Created Memo!", Toast.LENGTH_SHORT).show()
                }
            }

            dialogAlert2.setNeutralButton("Cancel") {
                    dialog, _ -> dialog.cancel()
            }
            dialogAlert2.show()
        }

    }


    // Double Pressing back button will ask if the user would like to sign out
    private var backPressedTwice = false
    override fun onBackPressed() {
        if (backPressedTwice) {
            val signOutDialogBox = AlertDialog.Builder(this)
            signOutDialogBox.setTitle("Sign Out?")
            signOutDialogBox.setMessage("Would you like to sign out?")

            signOutDialogBox.setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()

                Toast.makeText(baseContext, "Signed out successfully!", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@MainActivity, PreLoginActivity::class.java))
            }

            signOutDialogBox.setNeutralButton("No") {
                    dialog, _ -> dialog.cancel()
            }
            signOutDialogBox.show()
            return
        }
        this.backPressedTwice = true
        Handler().postDelayed({ this.backPressedTwice = false }, 2000)
    }
}


