package com.example.main_screen.viewmodel

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Helpers duplicated from ui layer so ViewModel can compile without changing existing UI logic

private fun parseDateFromId(id: String): Triple<Int, Int, Int>? {
    val re8 = Regex("""\d{8}""")
    val match8 = re8.find(id)
    if (match8 != null) {
        val s = match8.value
        val yyyy = s.substring(0, 4).toIntOrNull() ?: return null
        val mm = s.substring(4, 6).toIntOrNull() ?: return null
        val dd = s.substring(6, 8).toIntOrNull() ?: return null
        return Triple(yyyy, mm, dd)
    }
    val re6 = Regex("""\d{6}""")
    val match6 = re6.find(id)
    if (match6 != null) {
        val s = match6.value
        val yy = s.substring(0, 2).toIntOrNull() ?: return null
        val mm = s.substring(2, 4).toIntOrNull() ?: return null
        val dd = s.substring(4, 6).toIntOrNull() ?: return null
        val yyyy = 2000 + yy
        return Triple(yyyy, mm, dd)
    }
    return null
}

suspend fun getOrderCountForYear(db: FirebaseFirestore, year: Int): Int {
    val snap = db.collection("orders").get().await()
    return snap.documents.count { doc ->
        val idField = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
        val parsed = parseDateFromId(idField)
        parsed?.first == year
    }
}

suspend fun getOrderCountForMonth(db: FirebaseFirestore, month: Int): Int {
    val snap = db.collection("orders").get().await()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    return snap.documents.count { doc ->
        val idField = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
        val parsed = parseDateFromId(idField)
        parsed != null && parsed.first == currentYear && parsed.second == month
    }
}

suspend fun getSalesTotalForYear(db: FirebaseFirestore, year: Int): Double {
    val snap = db.collection("orders").get().await()
    return snap.documents.filter { doc ->
        val idField = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
        val parsed = parseDateFromId(idField)
        parsed?.first == year
    }.sumOf { doc -> (doc.getDouble("cost") ?: doc.getLong("cost")?.toDouble()) ?: 0.0 }
}

suspend fun getSalesTotalForMonth(db: FirebaseFirestore, month: Int): Double {
    val snap = db.collection("orders").get().await()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    return snap.documents.filter { doc ->
        val idField = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
        val parsed = parseDateFromId(idField)
        parsed != null && parsed.first == currentYear && parsed.second == month
    }.sumOf { doc -> (doc.getDouble("cost") ?: doc.getLong("cost")?.toDouble()) ?: 0.0 }
}

fun monthNumberToLabel(month: Int): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.MONTH, month - 1)
    val sdf = SimpleDateFormat("MMM", Locale.getDefault())
    return sdf.format(cal.time)
}
