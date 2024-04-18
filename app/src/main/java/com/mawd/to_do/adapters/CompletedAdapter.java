package com.mawd.to_do.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class CompletedAdapter extends RecyclerView.Adapter<CompletedAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<String> taskNameDoneList, taskDueDateDoneList;

    public CompletedAdapter(Context context, ArrayList<String> taskNameDoneList, ArrayList<String> taskDueDateDoneList) {
        this.context = context;
        this.taskNameDoneList = taskNameDoneList;
        this.taskDueDateDoneList = taskDueDateDoneList;
    }

    @NonNull
    @Override
    public CompletedAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.completed_card_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CompletedAdapter.MyViewHolder holder, int position) {
        Database db = Database.getInstance(context);
        String taskName = taskNameDoneList.get(position);
        String dueDate = taskDueDateDoneList.get(position);
        holder.txtTaskNameDone.setText(taskName);
        holder.txtTaskDueDateDone.setText(dueDate);

        // Check if the due date is today
        if (isDueToday(dueDate)) {
            String str = "Due Today";
            holder.txtTaskDueDateDone.setText(str);
        } else {
            holder.txtTaskDueDateDone.setText(dueDate);
        }
        int id = db.getIdByTaskName(taskName);
        holder.txtTaskNameDone.setOnClickListener(v -> {
            db.removeTask(id);
            taskNameDoneList.remove(position);
            taskDueDateDoneList.remove(position);
            ((MainActivity) context).updateCounter2(taskNameDoneList.size());
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return taskNameDoneList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtTaskNameDone, txtTaskDueDateDone;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTaskNameDone = itemView.findViewById(R.id.txtTaskNameDone);
            txtTaskDueDateDone = itemView.findViewById(R.id.txtTaskDueDateDone);
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
