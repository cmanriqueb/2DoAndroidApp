package com.koodie.tasks2do.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.koodie.tasks2do.todoTask;
import com.koodie.tasks2do.Activities.MainActivity;
import com.koodie.tasks2do.Model.taskModel;
import com.koodie.tasks2do.R;
import com.koodie.tasks2do.Utils.dbHandler;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class taskAdapter extends RecyclerView.Adapter<taskAdapter.ViewHolder> {

    private List<taskModel> todoList;
    private dbHandler db;
    private MainActivity activity;

    private String priorityToString(int priority) {
        switch (priority) {
            case 1:
                return "Low";
            case 2:
                return "Medium";
            case 3:
                return "High";
            default:
                return "Unknown";
        }
    }


    public taskAdapter(dbHandler db, MainActivity activity) {
        this.db = db;
        this.activity = activity;
        this.todoList = db.getAllTasks(true);
        int expiredCount = countExpiredTasks(todoList);
        if (expiredCount > 0) {
            sendNotification(expiredCount);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final taskModel item = todoList.get(position);
        holder.taskText.setText(item.getTask());
        holder.priority.setText(priorityToString(item.getPriority()));
        holder.location.setText(item.getLocation());
        holder.task.setChecked(toBoolean(item.getStatus()));


        Date dueDate = item.getDueDate();
        Log.d("taskAdapter dueDate -->", "Due Date: " + dueDate);
        Boolean expired = (dueDate.compareTo(Calendar.getInstance().getTime()) <= 0 ) ? true : false;
        Log.d("taskAdapter expired -->", "is expired? " + expired);

        if (expired) {
            holder.taskText.setTextColor(Color.RED);
        } else {
            holder.taskText.setTextColor(Color.BLACK);
        }

        if (dueDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            holder.dueDate.setText(sdf.format(dueDate));
        } else {
            holder.dueDate.setText("");
        }

        holder.task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.task.isChecked()) {
                    item.setStatus(1);
                } else {
                    item.setStatus(0);
                }
                db.updateStatus(item.getId(), item.getStatus());
                Collections.sort(todoList, new TaskComparator());
                notifyDataSetChanged();
            }
        });
    }


    private int countExpiredTasks(List<taskModel> tasks) {
        int expiredCount = 0;

        for (taskModel task : tasks) {
            Date dueDate = task.getDueDate();
            if (dueDate != null && dueDate.compareTo(Calendar.getInstance().getTime()) <= 0) {
                expiredCount++;
            }
        }

        return expiredCount;
    }

    private void sendNotification(int numOfTasks) {
        try {
            JSONObject notificationContent = new JSONObject()
                    .put("contents", new JSONObject().put("en", numOfTasks + " tasks have expire!"))
                    .put("include_player_ids", new JSONArray().put(OneSignal.getDeviceState().getUserId()));

            OneSignal.postNotification(notificationContent, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private boolean toBoolean(int n) {
        return n != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public Context getContext() {
        return activity;
    }

    public void setTasks(List<taskModel> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        taskModel item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public void editItem(int position) {
        taskModel item = todoList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        bundle.putInt("priority", item.getPriority());
        bundle.putString("location", item.getLocation());
        bundle.putString("due_date", sdf.format(item.getDueDate()));
        todoTask fragment = new todoTask();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), todoTask.TAG);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox task;
        TextView dueDate, taskText;
        TextView priority, location;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
            priority = view.findViewById(R.id.priorityValue);
            location = view.findViewById(R.id.locationValue);
            dueDate = view.findViewById(R.id.dueDateTextView);
            taskText = itemView.findViewById(R.id.todoText);

        }
    }

    public class TaskComparator implements Comparator<taskModel> {

        @Override
        public int compare(taskModel o1, taskModel o2) {
            if (o1.getStatus() == o2.getStatus()) {
                return Integer.compare(o2.getPriority(), o1.getPriority());
            }
            return Integer.compare(o1.getStatus(), o2.getStatus());
        }
    }

}
