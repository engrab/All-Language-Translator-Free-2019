package com.megafreeapps.all.language.translator.free;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

class DB
{
    private static final String DATABASE_NAME = "translator.db";
    private static final String DATABASE_TABLE = "result";
    private static final String FROM = "`from`";
    private static final String FROM_CODE = "`from_code`";
    private static final String FROM_TEXT = "`from_text`";
    private static final String ID = "`id`";
    private static final String NATIVE_TEXT = "`native_text`";
    private static final String TO = "`to`";
    private static final String TO_CODE = "`to_code`";
    private static final String TO_TEXT = "`to_text`";
    private static final int VERSION = 4;
    private static SQLiteDatabase db;
    private DBOpenHelper helper;

    DB(Context context)
    {
        this.helper = new DBOpenHelper(context, DATABASE_NAME, null, VERSION);
    }

    void open() throws SQLiteException
    {
        try
        {
            db = this.helper.getWritableDatabase();
        }
        catch (SQLiteException e)
        {
            db = this.helper.getReadableDatabase();
        }
    }

    void close()
    {
        if (db != null && db.isOpen())
        {
            db.close();
        }
    }

    void clear()
    {
        db.execSQL("delete from result;");
    }

    void insertResult(int id, String from, String to, String from_text, String to_text, String from_code, String to_code, String native_text)
    {
        ContentValues newRow = new ContentValues();
        newRow.put(ID, id);
        newRow.put(FROM, from);
        newRow.put(TO, to);
        newRow.put(FROM_TEXT, from_text);
        newRow.put(TO_TEXT, to_text);
        newRow.put(FROM_CODE, from_code);
        newRow.put(TO_CODE, to_code);
        newRow.put(NATIVE_TEXT, native_text);
        db.insert(DATABASE_TABLE, null, newRow);
    }

    void deleteResult(int id)
    {
        db.execSQL("delete from result where id=" + id);
    }

    ArrayList<ResultRow> getResults()
    {
        ArrayList<ResultRow> results = null;
        Cursor cursor = null;
        try
        {
            cursor = db.query(true, DATABASE_TABLE, new String[]{ID, FROM, TO, FROM_TEXT, TO_TEXT, FROM_CODE, TO_CODE, NATIVE_TEXT},
                    null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                results = new ArrayList<>();
                while (!cursor.isAfterLast())
                {
                    results.add(new ResultRow(cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("from")),
                            cursor.getString(cursor.getColumnIndex("to")),
                            cursor.getString(cursor.getColumnIndex("from_text")),
                            cursor.getString(cursor.getColumnIndex("to_text")),
                            cursor.getString(cursor.getColumnIndex("to_code")),
                            cursor.getString(cursor.getColumnIndex("from_code")),
                            cursor.getString(cursor.getColumnIndex("native_text"))));
                    cursor.moveToNext();
                }
            }
        }
        catch (Exception ignored)
        {
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return results;
    }

    private static class DBOpenHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_CREATE = "create table result (`id` INT, `from` TEXT, `to` TEXT,`from_text` TEXT, `to_text` TEXT, `from_code` TEXT, `to_code` TEXT, `native_text` TEXT);";

        DBOpenHelper(Context context, String name, CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase _db)
        {
            _db.execSQL(DATABASE_CREATE);
        }

        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion)
        {
            _db.execSQL("DROP TABLE IF EXISTS result");
            onCreate(_db);
        }
    }
}
