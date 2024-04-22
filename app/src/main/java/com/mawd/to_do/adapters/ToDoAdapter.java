package com.mawd.to_do.adapters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mawd.to_do.Database;
import com.mawd.to_do.MainActivity;
import com.mawd.to_do.R;
import com.mawd.to_do.notifications.NotificationReceiver;

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
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

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
            cancelNotification(taskName);
            ((MainActivity) context).updateCounter(taskNameList.size());
            notifyDataSetChanged();
        });

        holder.cardBtn.setOnLongClickListener(v -> {
            scheduleNotification(taskNameList.get(position), dueDate);
            return false;
        });
    }


    @Override
    public int getItemCount() {
        return taskNameList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtTaskName, txtTaskDueDate;
        ImageView btnMarkAsDone;
        CardView cardBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTaskName = itemView.findViewById(R.id.txtTaskName);
            txtTaskDueDate = itemView.findViewById(R.id.txtTaskDueDate);
            btnMarkAsDone = itemView.findViewById(R.id.btnMarkAsDone);
            cardBtn = itemView.findViewById(R.id.cardBtn);
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

    private void scheduleNotification(String taskName, String dueDate) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create an Intent for the notification
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_name", taskName);
        intent.putExtra("due_date", dueDate);

        // Create a unique requestCode for each notification
        int requestCode = taskName.hashCode(); // Using hashCode as requestCode
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        // Parse the due date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Calendar dueDateCalendar = Calendar.getInstance();
        try {
            dueDateCalendar.setTime(dateFormat.parse(dueDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        // Get today's date
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // Set the time for the notification
        long triggerTime = dueDateCalendar.getTimeInMillis();
        long currentTime = System.currentTimeMillis();

        // Schedule the notification if the due date is in the future
        if (triggerTime > currentTime) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            showMessage("A reminder is set for " + taskName);
        } else if (triggerTime < currentTime) {
            showMessage("The task is past due/due today");
        }
    }


    private void showMessage(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private void cancelNotification(String taskName) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        int requestCode = taskName.hashCode(); // Use the same requestCode as when creating the pending intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

}
