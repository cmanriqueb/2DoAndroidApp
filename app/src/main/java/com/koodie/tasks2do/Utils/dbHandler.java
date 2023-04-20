package com.koodie.tasks2do.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.koodie.tasks2do.Model.taskModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class dbHandler extends SQLiteOpenHelper {

    private static final int VERSION = 4;
    private static final String NAME = "toDoListDatabase";
    private static final String TODO_TABLE = "todo";
    private static final String ID = "id";
    private static final String TASK = "task";
    private static final String STATUS = "status";
    private static final String PRIORITY = "priority";
    private static final String LOCATION = "location";
    private static final String DUE_DATE = "due_date";
    private static final String USER_TABLE = "user";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final String CREATE_USER_TABLE = "CREATE TABLE " + USER_TABLE + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + USERNAME + " TEXT NOT NULL UNIQUE, "
            + PASSWORD + " TEXT NOT NULL);";


    private static final String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TASK + " TEXT, "
            + STATUS + " INTEGER, " + PRIORITY + " INTEGER, " + LOCATION + " TEXT, " + DUE_DATE + " INTEGER)";



    private SQLiteDatabase db;

    public dbHandler(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_TODO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // Drop the existing table if it exists
            db.execSQL("DROP TABLE IF EXISTS " + TODO_TABLE);
            // Recreate the table with the new schema
            onCreate(db);
        }
    }


    public void openDatabase() {
        db = this.getWritableDatabase();
    }

    public void insertTask(taskModel task) {
        ContentValues cv = new ContentValues();
        cv.put(TASK, task.getTask());
        cv.put(STATUS, 0);
        cv.put(PRIORITY, task.getPriority());
        cv.put(LOCATION, task.getLocation());
        cv.put(DUE_DATE, task.getDueDate().getTime()); // Convert Date object to timestamp (long)
        db.insert(TODO_TABLE, null, cv);
    }


    @SuppressLint("Range")
    public List<taskModel> getAllTasks(boolean isSortedDescending) {
        List<taskModel> todoList = new ArrayList<>();

        String selectQuery;
        if (isSortedDescending) {
            selectQuery = "SELECT * FROM " + TODO_TABLE + " ORDER BY " + STATUS + " DESC, " + PRIORITY + " DESC";
        } else {
            selectQuery = "SELECT * FROM " + TODO_TABLE + " ORDER BY " + STATUS + " ASC, " + PRIORITY + " ASC";
        }

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        taskModel item = new taskModel();
                        item.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                        item.setTask(cursor.getString(cursor.getColumnIndex(TASK)));
                        item.setStatus(cursor.getInt(cursor.getColumnIndex(STATUS)));
                        item.setPriority(cursor.getInt(cursor.getColumnIndex(PRIORITY)));
                        item.setLocation(cursor.getString(cursor.getColumnIndex(LOCATION)));
                        long dueDateTimestamp = cursor.getLong(cursor.getColumnIndex(DUE_DATE));
                        Date dueDate = new Date(dueDateTimestamp);
                        item.setDueDate(dueDate);

                        todoList.add(item);
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return todoList;
    }




    // Add methods to update priority and location
    public void updatePriority(int id, int priority) {
        ContentValues cv = new ContentValues();
        cv.put(PRIORITY, priority);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[]{String.valueOf(id)});
    }

    public void updateLocation(int id, String location) {
        ContentValues cv = new ContentValues();
        cv.put(LOCATION, location);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[]{String.valueOf(id)});
    }

    public void updateStatus(int id, int status){
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void updateTask(taskModel task) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(dbHandler.TASK, task.getTask());
        contentValues.put(dbHandler.STATUS, task.getStatus());
        contentValues.put(dbHandler.PRIORITY, task.getPriority());
        contentValues.put(dbHandler.LOCATION, task.getLocation());

        Date dueDate = task.getDueDate();
        if (dueDate != null) {
            contentValues.put(dbHandler.DUE_DATE, dueDate.getTime());
        } else {
            contentValues.putNull(dbHandler.DUE_DATE);
        }

        db.update(dbHandler.TODO_TABLE, contentValues, dbHandler.ID + " = ?", new String[]{String.valueOf(task.getId())});
    }


    public void updateDueDate(int id, Date dueDate) {
        ContentValues cv = new ContentValues();
        cv.put(DUE_DATE, dueDate.getTime());
        db.update(TODO_TABLE, cv, ID + "= ?", new String[]{String.valueOf(id)});
    }



    public void deleteTask(int id){
        db.delete(TODO_TABLE, ID + "= ?", new String[] {String.valueOf(id)});
    }


    public boolean authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE, new String[]{ID, USERNAME, PASSWORD},
                USERNAME + "=?", new String[]{username}, null, null, null);

        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
            @SuppressLint("Range") String storedPassword = cursor.getString(cursor.getColumnIndex(PASSWORD));
            cursor.close();
            return password.equals(storedPassword);
        }
        return false;
    }

    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERNAME, username);
        contentValues.put(PASSWORD, password);

        long result = db.insert(USER_TABLE, null, contentValues);
        return result != -1;
    }

}
