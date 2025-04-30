package com.example.goin2.teacher.CreateClass

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import com.example.goin2.API_and_location.ApiClient
import com.example.goin2.R
import com.example.goin2.main.MainActivity

class TeacherClassManagementActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private val classMap = mutableMapOf<Int, MutableList<Pair<Int, String>>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_class_management)

        container = findViewById(R.id.classListContainer)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            view.setPadding(0, insets.getInsets(WindowInsetsCompat.Type.systemBars()).top, 0, 0)
            insets
        }
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Manage Classes"

        buildClassesByTeacher(MainActivity.currentTeacherId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_teacher_class, menu)
        menu?.findItem(R.id.action_add_class)?.icon?.setTint(getColor(android.R.color.white))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_class -> {
                showCreateClassDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun buildClassesByTeacher(teacherId: Int) {
        container.removeAllViews()
        classMap.clear()

        ApiClient.getClassesByTeacher(teacherId) { classList ->
            classList.forEach { (classId, className) ->
                classMap[classId] = mutableListOf()
                ApiClient.getClassRoster(classId) { studentIds ->
                    val students = mutableListOf<Pair<Int, String>>()
                    val remaining = studentIds.toMutableSet()

                    if (studentIds.isEmpty()) {
                        runOnUiThread {
                            addClassView(classId, className, students)
                        }
                    } else {
                        studentIds.forEach { studentId ->
                            ApiClient.getUserById(studentId) { namePair ->
                                namePair?.let { students.add(studentId to "${it.first} ${it.second}") }
                                remaining.remove(studentId)

                                if (remaining.isEmpty()) {
                                    classMap[classId] = students.toMutableList()
                                    runOnUiThread {
                                        addClassView(classId, className, students)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showCreateClassDialog() {
        val fragment = CreateClassFragment {
            buildClassesByTeacher(MainActivity.currentTeacherId)
        }
        fragment.show(supportFragmentManager, "createClass")
    }

    private fun showAddStudentDialog(classId: Int, studentContainer: LinearLayout) {
        val view = layoutInflater.inflate(R.layout.fragment_add_student, null)
        val firstInput = view.findViewById<EditText>(R.id.firstNameInput)
        val lastInput = view.findViewById<EditText>(R.id.lastNameInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Student")
            .setView(view)
            .setPositiveButton("Submit", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val first = firstInput.text.toString().trim()
                val last = lastInput.text.toString().trim()

                if (first.isEmpty() || last.isEmpty()) {
                    Toast.makeText(this, "Enter full name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                button.isEnabled = false

                ApiClient.createStudent(this, first, last) { newId ->
                    if (newId != null) {
                        ApiClient.addStudentToClass(classId, newId) { success ->
                            if (success) {
                                val fullName = "$first $last"
                                runOnUiThread {
                                    classMap[classId]?.add(newId to fullName)
                                    addStudentView(classId, newId, fullName, studentContainer)
                                    dialog.dismiss()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this, "Failed to add to class", Toast.LENGTH_SHORT).show()
                                    button.isEnabled = true
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Failed to create student", Toast.LENGTH_SHORT).show()
                            button.isEnabled = true
                        }
                    }
                }
            }
        }

        dialog.show()
    }

    private fun addClassView(classId: Int, className: String, students: List<Pair<Int, String>>) {
        val classWrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val dropdownArrow = TextView(this).apply {
            text = "▶"
            textSize = 18f
        }

        val title = TextView(this).apply {
            text = className
            textSize = 18f
            setPadding(12, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val buttonSize = resources.getDimensionPixelSize(R.dimen.class_button_size)

        val addBtn = Button(this).apply {
            text = "+"
            setBackgroundColor(Color.parseColor("#FFBB86FC"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams((buttonSize * 1.5).toInt(), buttonSize).apply { marginEnd = 8 }
            setOnClickListener {
                val studentContainer = classWrapper.findViewWithTag<LinearLayout>("students_$classId")
                showAddStudentDialog(classId, studentContainer)
            }
        }

        val deleteBtn = Button(this).apply {
            text = "X"
            setBackgroundColor(Color.rgb(139, 0, 0))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams((buttonSize * 1.5).toInt(), buttonSize).apply { marginEnd = 8 }
            setOnClickListener {
                ApiClient.deleteClass(classId) { success ->
                    runOnUiThread {
                        if (success) {
                            classMap.remove(classId)
                            container.removeView(classWrapper)
                            Toast.makeText(this@TeacherClassManagementActivity, "Class deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@TeacherClassManagementActivity, "Failed to delete class", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        val studentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            tag = "students_$classId"
        }

        header.setOnClickListener {
            studentContainer.visibility = if (studentContainer.visibility == View.GONE) {
                dropdownArrow.text = "▼"
                View.VISIBLE
            } else {
                dropdownArrow.text = "▶"
                View.GONE
            }
        }

        header.addView(dropdownArrow)
        header.addView(title)
        header.addView(addBtn)
        header.addView(deleteBtn)

        classWrapper.addView(header)
        classWrapper.addView(studentContainer)
        container.addView(classWrapper)

        students.forEach { (id, name) -> addStudentView(classId, id, name, studentContainer) }
    }

    private fun addStudentView(classId: Int, studentId: Int, studentName: String, container: LinearLayout) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8)
        }

        val nameView = TextView(this).apply {
            text = studentName
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val buttonSize = resources.getDimensionPixelSize(R.dimen.class_button_size)

        val deleteBtn = Button(this).apply {
            text = "X"
            setBackgroundColor(Color.rgb(139, 0, 0))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(buttonSize, buttonSize)
            setOnClickListener {
                ApiClient.deleteStudent(studentId) { success ->
                    runOnUiThread {
                        if (success) {
                            classMap[classId]?.removeIf { it.first == studentId }
                            container.removeView(row)
                        } else {
                            Toast.makeText(this@TeacherClassManagementActivity, "Failed to delete student", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        row.addView(nameView)
        row.addView(deleteBtn)
        container.addView(row)
    }
}
