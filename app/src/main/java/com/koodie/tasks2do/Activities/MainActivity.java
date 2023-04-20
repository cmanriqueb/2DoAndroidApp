package com.koodie.tasks2do.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.koodie.tasks2do.Adapters.taskAdapter;
import com.koodie.tasks2do.DialogCloseListener;
import com.koodie.tasks2do.Model.taskModel;
import com.koodie.tasks2do.R;
import com.koodie.tasks2do.RecyclerItemTouchHelper;
import com.koodie.tasks2do.Utils.dbHandler;
import com.koodie.tasks2do.todoTask;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {

    private dbHandler db;
    private RecyclerView tasksRecyclerView;
    private taskAdapter tasksAdapter;
    private FloatingActionButton fab;
    private List<taskModel> taskList;
    private boolean isSortedDescending = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        db = new dbHandler(this);
        db.openDatabase();

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new taskAdapter(db,MainActivity.this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        fab = findViewById(R.id.fab);

        ImageButton sortingButton = findViewById(R.id.sortingButton);
        sortingButton.setOnClickListener(new View.OnClickListener() {
            boolean flipped = false;
            @Override
            public void onClick(View view) {


                flipIcon();
                isSortedDescending = !isSortedDescending;
                Log.d("isSortedDescending  -->", ""+isSortedDescending);
                taskList = db.getAllTasks(isSortedDescending);
                Collections.reverse(taskList);
                tasksAdapter.setTasks(taskList);
            }

            private void flipIcon() {
                float fromRotation = flipped ? 180f : 0f;
                float toRotation = flipped ? 0f : 180f;

                ValueAnimator animator = ValueAnimator.ofFloat(fromRotation, toRotation);
                animator.setDuration(300); // Duration of the animation in milliseconds
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float rotation = (float) animation.getAnimatedValue();
                        sortingButton.setRotation(rotation);
                    }
                });

                animator.start();
                flipped = !flipped;
            }

        });

        taskList = db.getAllTasks(isSortedDescending);
        Collections.reverse(taskList);

        tasksAdapter.setTasks(taskList);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                todoTask.newInstance().show(getSupportFragmentManager(), todoTask.TAG);
            }
        });


    }

    @Override
    public void handleDialogClose(DialogInterface dialog){
        taskList = db.getAllTasks(isSortedDescending);
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
        tasksRecyclerView.smoothScrollToPosition(0);
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (tasksAdapter != null) {
            tasksAdapter.setTasks(null);
            tasksAdapter.notifyDataSetChanged();
        }
    }

}