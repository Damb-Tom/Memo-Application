package tom.memo

import com.google.firebase.database.DatabaseReference

data class MemoData(val title: String, val importance: Int)
data class MemoRefData(val title: String, val importance: Int, val title_ref: DatabaseReference, val importance_ref: DatabaseReference)

var sortedMemos = ArrayList<MemoRefData>()