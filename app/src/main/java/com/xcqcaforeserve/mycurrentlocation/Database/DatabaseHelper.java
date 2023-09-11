package com.xcqcaforeserve.mycurrentlocation.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xcqcaforeserve.mycurrentlocation.Models.NoteBook;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "notes_db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NoteBook.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NoteBook.TABLE_NAME);
        onCreate(db);
    }


    public long insertNote(String note) {
        SQLiteDatabase db = this.getWritableDatabase();    // get writable database as we want to write data
        ContentValues values = new ContentValues();  // `id` and `timestamp` will be inserted automatically.
        values.put(NoteBook.COLUMN_NOTE, note);        // no need to add them
        long id = db.insert(NoteBook.TABLE_NAME, null, values);   // insert row
        db.close();  // close db connection
        return id;    // return newly inserted row id
    }

    @SuppressLint("Range")
    public NoteBook getNoteBook(long id) {
        SQLiteDatabase db = this.getReadableDatabase();   // get readable database as we are not inserting anything
        Cursor cursor = db.query(NoteBook.TABLE_NAME,
                new String[]{NoteBook.COLUMN_ID, NoteBook.COLUMN_NOTE, NoteBook.COLUMN_TIMESTAMP},
                NoteBook.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        NoteBook noteBook = new NoteBook(
                cursor.getInt(cursor.getColumnIndex(NoteBook.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(NoteBook.COLUMN_NOTE)),
                cursor.getString(cursor.getColumnIndex(NoteBook.COLUMN_TIMESTAMP)));
        cursor.close();    // close the db connection

        return noteBook;
    }

    @SuppressLint("Range")
    public List<NoteBook> getAllNotes() {
        List<NoteBook> notes = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + NoteBook.TABLE_NAME + " ORDER BY " +
                NoteBook.COLUMN_TIMESTAMP + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                NoteBook note = new NoteBook();
                note.setId(cursor.getInt(cursor.getColumnIndex(NoteBook.COLUMN_ID)));
                note.setNote(cursor.getString(cursor.getColumnIndex(NoteBook.COLUMN_NOTE)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(NoteBook.COLUMN_TIMESTAMP)));
                notes.add(note);
            } while (cursor.moveToNext());
        }
        db.close();   // close db connection
        return notes;    // return notes list
    }

    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + NoteBook.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        return count;    // return count
    }

    public int updateNote(NoteBook note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NoteBook.COLUMN_NOTE, note.getNote());

        // updating row
        return db.update(NoteBook.TABLE_NAME, values, NoteBook.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }

    public void deleteNote(NoteBook note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NoteBook.TABLE_NAME, NoteBook.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
    }
}
