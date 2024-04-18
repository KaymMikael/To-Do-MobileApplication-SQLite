package com.mawd.to_do;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.mawd.to_do.models.Task;

public class Database extends SQLiteOpenHelper {

    private SQLiteDatabase db;

    public static final String DATABASE_NAME = "ToDoApp";
    public static final String tableToDo = "tblToDo";
    public static final String columnId = "task_id";
    public static final String columnTaskName = "task_name";
    public static final String columnTaskDueDate = "task_due_date";
    public static final String columnIsCompleted = "task_is_completed";
    private static int ToDoAppVersion = 1;

    private static Database dbInstance;
    private Context context;

    private Database(Context context) {
        super(context, DATABASE_NAME, null, ToDoAppVersion);
        this.context = context;
    }

    public static Database getInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = new Database(context);
        }
        return dbInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + tableToDo +
                " (" + columnId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                columnTaskName + " TEXT NOT NULL, " +
                columnTaskDueDate + " TEXT NOT NULL, " +
                columnIsCompleted + " TEXT NOT NULL)";
        try {
            db.execSQL(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE iF EXISTS " + tableToDo);
        onCreate(db);
    }

    public void addTask(Task t) {
        db = this.getWritableDatabase();
        if (t != null) {
            ContentValues cv = new ContentValues();
            cv.put(columnTaskName, t.getTaskName());
            cv.put(columnTaskDueDate, t.getTaskDueDate());
            if (!t.isCompleted()) {
                cv.put(columnIsCompleted, "No");
            }
            db.insert(tableToDo, null, cv);

            showMessage("Added Successfully");
        }
        db.close();
    }

    public void removeTask(int id) {
        db = this.getWritableDatabase();
        int res = db.delete(tableToDo, columnId + " = ?", new String[]{String.valueOf(id)});
        if(res == 0) {
            showMessage("Failed To Delete Task");
        }else {
            showMessage("Task Deleted");
        }
    }

    @SuppressLint("Range")
    public int getIdByTaskName(String taskName) {
        db = this.getReadableDatabase();
        String query = "SELECT " + columnId + " FROM " + tableToDo + " WHERE " + columnTaskName + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{taskName});
        int id = -1;  // Default value in case no match is found
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex(columnId));
        }
        cursor.close();
        db.close();
        return id;
    }


    @SuppressLint("Range")
    public String markTaskAsCompleted(int id) {
        db = this.getWritableDatabase();
        String taskName = null;
        ContentValues cv = new ContentValues();
        cv.put(columnIsCompleted, "Yes");

        Cursor cursor = db.rawQuery("SELECT " + columnTaskName + " FROM " + tableToDo + " WHERE " + columnId + "=?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            taskName = cursor.getString(cursor.getColumnIndex(columnTaskName));
        }
        cursor.close();

        long result = db.update(tableToDo, cv, columnId + "=?", new String[]{String.valueOf(id)});
        if (result == -1) {
            Toast.makeText(context, "Failed to Update", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return taskName;
    }

    public Cursor readAllToDoData() {
        String query = "SELECT task_name, task_due_date FROM " + tableToDo + " WHERE task_is_completed = 'No'";
        db = this.getWritableDatabase();
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public Cursor readAllCompletedData() {
        String query = "SELECT task_name, task_due_date FROM " + tableToDo + " WHERE task_is_completed = 'Yes'";
        db = this.getWritableDatabase();
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    private void showMessage(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
