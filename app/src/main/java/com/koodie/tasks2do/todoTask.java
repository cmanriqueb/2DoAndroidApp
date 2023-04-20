package com.koodie.tasks2do;


import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;


import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.koodie.tasks2do.Model.taskModel;
import com.koodie.tasks2do.Utils.dbHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class todoTask extends BottomSheetDialogFragment {
    public static final String TAG = "ActionBottomDialog";
    private EditText newTaskText;
    private Button newTaskSaveButton;
    private Spinner prioritySpinner;
    private EditText locationEditText;
    private EditText dueDateEditText;


    private dbHandler db;

    public static todoTask newInstance() {
        return new todoTask();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.new_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        prioritySpinner = view.findViewById(R.id.prioritySpinner);
        locationEditText = view.findViewById(R.id.locationEditText);
        newTaskText = view.findViewById(R.id.newTaskText);
        newTaskSaveButton = view.findViewById(R.id.newTaskButton);
        dueDateEditText = view.findViewById(R.id.dueDateTxt);

        // Code to format the date in case is entered without "slashes"
        EditText dueDateEditText = view.findViewById(R.id.dueDateTxt);

        dueDateEditText.addTextChangedListener(new TextWatcher() {
            private boolean isManualChange = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isManualChange) {
                    String input = editable.toString();

                    if (input.length() == 2 || input.length() == 5) {
                        isManualChange = true;
                        dueDateEditText.setText(input + "/");
                        dueDateEditText.setSelection(input.length() + 1);
                        isManualChange = false;
                    }
                }
            }
        });

        // END of code format

        db = new dbHandler(getActivity());
        db.openDatabase();

        Date dueDate = Calendar.getInstance().getTime();

        boolean isUpdate = false;
        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            //Log.d("UPDATE -->", "YES!");
            String task = bundle.getString("task");
            int priority = bundle.getInt("priority");
            String location = bundle.getString("location");
            String dueDateString = bundle.getString("due_date");
            Log.d("todoTask dueDateString -->", "Due Date Input: " + dueDateString);
            if (!TextUtils.isEmpty(dueDateString)) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                try {
                    dueDate = sdf.parse(dueDateString);
                    Log.d("todoTask dueDate -->", "Due Date Input: " + dueDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            newTaskText.setText(task);
            prioritySpinner.setSelection(priority - 1); // Subtract 1 to match the spinner position
            locationEditText.setText(location);
            dueDateEditText.setText(dueDateString);
        }else{
            Log.d("UPDATE -->", "NO!");
        }

        newTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    newTaskSaveButton.setEnabled(false);
                    newTaskSaveButton.setTextColor(Color.GRAY);
                } else {
                    newTaskSaveButton.setEnabled(true);
                    newTaskSaveButton.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorPrimaryDark));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final boolean finalIsUpdate = isUpdate;
        //Log.d("isUpdate -->", ""+finalIsUpdate);

        newTaskSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = newTaskText.getText().toString();
                int priority = prioritySpinner.getSelectedItemPosition() + 1;
                String location = locationEditText.getText().toString();

                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                Date finalDueDate = null;
                try {
                    finalDueDate = sdf.parse(dueDateEditText.getText().toString());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }


                if (finalIsUpdate) {
                    int id = bundle.getInt("id");
                    taskModel task = new taskModel();
                    task.setId(id);
                    task.setTask(text);
                    task.setPriority(priority);
                    task.setLocation(location);
                    task.setDueDate(finalDueDate);
                    db.updateTask(task);
                } else {
                    taskModel task = new taskModel();
                    task.setTask(text);
                    task.setStatus(0);
                    task.setPriority(priority);
                    task.setLocation(location);
                    task.setDueDate(finalDueDate);
                    db.insertTask(task);
                }
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity instanceof DialogCloseListener)
            ((DialogCloseListener) activity).handleDialogClose(dialog);
    }
}
