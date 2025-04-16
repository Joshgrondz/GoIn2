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
import com.example.goin2.R

class TeacherClassManagementActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private val classMap = mutableMapOf<String, MutableList<Student>>()

    data class Student(val firstName: String, val lastName: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_class_management)

        container = findViewById(R.id.classListContainer)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            view.setPadding(0, topInset, 0, 0)
            insets
        }

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Manage Classes"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_teacher_class, menu)

        val item = menu?.findItem(R.id.action_add_class)
        item?.icon?.setTint(getColor(android.R.color.white))  // or a custom color

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

    private fun showCreateClassDialog() {
        val input = EditText(this)
        input.hint = "Class Name"
        AlertDialog.Builder(this)
            .setTitle("Create Class")
            .setView(input)
            .setPositiveButton("Submit") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty() && !classMap.containsKey(name)) {
                    classMap[name] = mutableListOf()
                    addClassView(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddStudentDialog(className: String, studentContainer: LinearLayout) {
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_add_student, null, false)
        val firstInput = view.findViewById<EditText>(R.id.firstNameInput)
        val lastInput = view.findViewById<EditText>(R.id.lastNameInput)

        AlertDialog.Builder(this)
            .setTitle("Add Student to $className")
            .setView(view)
            .setPositiveButton("Submit") { _, _ ->
                val first = firstInput.text.toString().trim()
                val last = lastInput.text.toString().trim()
                if (first.isNotEmpty() && last.isNotEmpty()) {
                    val student = Student(first, last)
                    classMap[className]?.add(student)
                    addStudentView(className, student, studentContainer)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addClassView(className: String) {
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
            layoutParams = LinearLayout.LayoutParams((buttonSize*1.5).toInt(), buttonSize).apply {marginEnd = 8 }
            setOnClickListener {
                val studentContainer = classWrapper.findViewWithTag<LinearLayout>("students_$className")
                showAddStudentDialog(className, studentContainer)
            }
        }

        val deleteBtn = Button(this).apply {
            text = "X"
            setBackgroundColor(Color.rgb(139, 0, 0))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams((buttonSize*1.5).toInt(), buttonSize).apply {marginEnd = 8 }
            setOnClickListener {
                classMap.remove(className)
                container.removeView(classWrapper)
            }
        }


        val studentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            tag = "students_$className"
        }

        header.setOnClickListener {
            if (studentContainer.visibility == View.GONE) {
                studentContainer.visibility = View.VISIBLE
                dropdownArrow.text = "▼"
            } else {
                studentContainer.visibility = View.GONE
                dropdownArrow.text = "▶"
            }
        }

        header.addView(dropdownArrow)
        header.addView(title)
        header.addView(addBtn)
        header.addView(deleteBtn)

        classWrapper.addView(header)
        classWrapper.addView(studentContainer)

        container.addView(classWrapper)
    }

    private fun addStudentView(className: String, student: Student, container: LinearLayout) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8)
        }

        val nameView = TextView(this).apply {
            text = "${student.firstName} ${student.lastName}"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val buttonSize = resources.getDimensionPixelSize(R.dimen.class_button_size)

        val deleteBtn = Button(this).apply {
            text = "X"
            setBackgroundColor(Color.rgb(139, 0, 0))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams((buttonSize), buttonSize)
            setOnClickListener {
                classMap[className]?.remove(student)
                container.removeView(row)
            }
        }

        row.addView(nameView)
        row.addView(deleteBtn)
        container.addView(row)
    }
}
