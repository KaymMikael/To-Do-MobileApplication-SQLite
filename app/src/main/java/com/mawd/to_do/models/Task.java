package com.mawd.to_do.models;

public class Task {
    private String taskName;
    private String taskDueDate;
    private boolean isCompleted;

    public Task(String taskName, String taskDueDate, boolean isCompleted) {
        this.taskName = taskName;
        this.taskDueDate = taskDueDate;
        this.isCompleted = isCompleted;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDueDate() {
        return taskDueDate;
    }

    public void setTaskDueDate(String taskDueDate) {
        this.taskDueDate = taskDueDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
