package com.mawd.to_do.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mawd.to_do.Database;
import com.mawd.to_do.MainActivity;
import com.mawd.to_do.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {
    public interface OnTaskCompletedListener {
        void onTaskCompleted(String taskName, String dueDate);
    }

    private Context context;
    private ArrayList<String> taskNameList, dueDateList;
    private OnTaskCompletedListener taskCompletedListener;

    public ToDoAdapter(Context context, ArrayList<String> taskNameList, ArrayList<String> dueDateList, OnTaskCompletedListener listener) {
        this.context = context;
        this.taskNameList = taskNameList;
        this.dueDateList = dueDateList;
        this.taskCompletedListener = listener;
    }

    @NonNull
    @Override
    public ToDoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.todo_card_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoAdapter.MyViewHolder holder, int position) {
        Database db = Database.getInstance(context);
        holder.txtTaskName.setText(taskNameList.get(position));
        String taskName = holder.txtTaskName.getText().toString();
        String dueDate = dueDateList.get(position); // Fetch due date from the list
        int id = db.getIdByTaskName(taskName);
        // Check if the due date is today
        if (isDueToday(dueDate)) {
            String str = "Due Today";
            holder.txtTaskDueDate.setText(str);
        } else {
            holder.txtTaskDueDate.setText(dueDate);
        }
        holder.btnMarkAsDone.setOnClickListener(v -> {
            String completedTaskName = db.markTaskAsCompleted(id);
            if (completedTaskName != null) {
                taskNameList.remove(completedTaskName);
                dueDateList.remove(dueDate); // Remove due date from the list
                notifyDataSetChanged();
                ((MainActivity) context).updateCounter(taskNameList.size());
                // Notify MainActivity that a task is completed
                taskCompletedListener.onTaskCompleted(completedTaskName, dueDate);
            }
        });
        holder.txtTaskName.setOnClickListener(v -> {
            db.removeTask(id);
            taskNameList.remove(position);
            dueDateList.remove(position);
            ((MainActivity) context).updateCounter(taskNameList.size());
            notifyDataSetChanged();
        });
    }


    @Override
    public int getItemCount() {
        return taskNameList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtTaskName, txtTaskDueDate;
        ImageView btnMarkAsDone;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTaskName = itemView.findViewById(R.id.txtTaskName);
            txtTaskDueDate = itemView.findViewById(R.id.txtTaskDueDate);
            btnMarkAsDone = itemView.findViewById(R.id.btnMarkAsDone);

        }
    }

    private boolean isDueToday(String dueDate) {
        // Get today's date
        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        // Parse the due date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date parsedDueDate;
        try {
            parsedDueDate = dateFormat.parse(dueDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        // Compare with today's date
        Calendar dueDateCalendar = Calendar.getInstance();
        dueDateCalendar.setTime(parsedDueDate);
        int dueYear = dueDateCalendar.get(Calendar.YEAR);
        int dueMonth = dueDateCalendar.get(Calendar.MONTH);
        int dueDay = dueDateCalendar.get(Calendar.DAY_OF_MONTH);

        return (todayYear == dueYear && todayMonth == dueMonth && todayDay == dueDay);
    }
}
