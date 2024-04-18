/* Made By Kaym Mikael
Simple To-Do App
4/18/2024-done
no edit feature
 */
package com.mawd.to_do;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mawd.to_do.adapters.CompletedAdapter;
import com.mawd.to_do.adapters.ToDoAdapter;
import com.mawd.to_do.models.Task;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements ToDoAdapter.OnTaskCompletedListener {
    private Button btnAddNewTask, btnAddTask;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private EditText inputNewTask, inputNewDate;
    private Dialog addNewTaskDialog;
    private RecyclerView ToDoList, completedList;
    private TextView txtToDo, txtCompleted;
    private ToDoAdapter toDoAdapter;
    private CompletedAdapter completedAdapter;
    private ArrayList<String> taskNameList, dueDateList, taskNameDoneList, taskDueDateDoneList;

    private Database db;

    private void setReference() {
        btnAddNewTask = findViewById(R.id.btnAddNewTask);
        addNewTaskDialog = new Dialog(MainActivity.this);
        addNewTaskDialog.setContentView(R.layout.alert_dialog_add_task);
        addNewTaskDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addNewTaskDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.alert_dialog_background));
        addNewTaskDialog.setCancelable(true);

        inputNewTask = addNewTaskDialog.findViewById(R.id.txtNewTask);
        inputNewDate = addNewTaskDialog.findViewById(R.id.txtNewDate);
        btnAddTask = addNewTaskDialog.findViewById(R.id.btnAddTask);
        txtToDo = findViewById(R.id.txtToDo);
        txtCompleted = findViewById(R.id.txtCompleted);

        db = Database.getInstance(this);
        taskNameList = new ArrayList<>();
        dueDateList = new ArrayList<>();
        taskNameDoneList = new ArrayList<>();
        taskDueDateDoneList = new ArrayList<>();

        toDoAdapter = new ToDoAdapter(this, taskNameList, dueDateList, this);
        ToDoList = findViewById(R.id.ToDoList);
        ToDoList.setAdapter(toDoAdapter);
        ToDoList.setLayoutManager(new LinearLayoutManager(this));

        completedAdapter = new CompletedAdapter(this, taskNameDoneList, taskDueDateDoneList);
        completedList = findViewById(R.id.completedList);
        completedList.setAdapter(completedAdapter);
        completedList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void resetList() {
        taskNameList.clear();
        dueDateList.clear();
    }

    private void setToDoCount() {
        String str = "TO DO: " + taskNameList.size();
        txtToDo.setText(str);
    }

    private void setCompletedCount() {
        String str = "COMPLETED: " + taskNameDoneList.size();
        txtCompleted.setText(str);
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setReference();
        displayData();
        displayCompletedData();
        setToDoCount();
        setCompletedCount();

        btnAddNewTask.setOnClickListener(v -> {
            addNewTaskDialog.show();
        });

        dateSetListener = (view, year, month, dayOfMonth) -> {
            String selectedDate = (month + 1) + "/" + dayOfMonth + "/" + year;
            inputNewDate.setText(selectedDate);
        };

        inputNewDate.setOnClickListener(v -> {
            showDatePickerDialog();
        });

        btnAddTask.setOnClickListener(v -> {
            String newTask = inputNewTask.getText().toString();
            String dueDate = inputNewDate.getText().toString();
            if (newTask.isEmpty() || dueDate.isEmpty()) {
                showMessage("Please Input All Fields.");
            } else if (newTask.length() <= 3) {
                showMessage("Task Name Must Be At least 4 characters");
            } else {
                boolean isCompleted = false;
                Task task = new Task(newTask, dueDate, isCompleted);
                db.addTask(task);
                //Clear inputs
                inputNewTask.setText("");
                inputNewDate.setText("");
                //Hide alert dialog
                addNewTaskDialog.dismiss();
                resetList();
                displayData();
                toDoAdapter.notifyDataSetChanged();
                setToDoCount();
            }
        });
    }

    @Override
    public void onTaskCompleted(String taskName, String dueDate) {
        // Add the completed task to CompletedAdapter
        taskNameDoneList.add(taskName);
        taskDueDateDoneList.add(dueDate);
        completedAdapter.notifyDataSetChanged();
        setCompletedCount();
    }

    private void showDatePickerDialog() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, dateSetListener, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    private void displayData() {
        Cursor cursor = db.readAllToDoData();

        if (cursor.getCount() == 0) {
            showMessage("No Task to Display");
        } else {
            while (cursor.moveToNext()) {
                taskNameList.add(cursor.getString(0));
                dueDateList.add(cursor.getString(1));
            }
            // Notify adapter after fetching data
            toDoAdapter.notifyDataSetChanged();
        }
    }

    private void displayCompletedData() {
        Cursor cursor = db.readAllCompletedData();
        if (cursor.getCount() == 0) {
            showMessage("No Completed Task");
        } else {
            while (cursor.moveToNext()) {
                taskNameDoneList.add(cursor.getString(0));
                taskDueDateDoneList.add(cursor.getString(1));
            }
            completedAdapter.notifyDataSetChanged();
        }
    }


    public void updateCounter(int count) {
        String str = "TO DO: " + count;
        txtToDo.setText(str);
    }

    public void updateCounter2(int count) {
        String str = "COMPLETED: " + count;
        txtCompleted.setText(str);
    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}