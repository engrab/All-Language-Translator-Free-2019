package com.megafreeapps.all.language.translator.free.conversation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class ChatDataBase {
    private static final String DATABASE_NAME = "chathistory.db";
    private static final String DATABASE_TABLE = "chat_result";
    private static final String FROM = "`from`";
    private static final String FROM_CODE = "`from_code`";
    private static final String FROM_TEXT = "`from_text`";
    private static final String ID = "`id`";
    private static final String TO = "`to`";
    private static final String TO_CODE = "`to_code`";
    private static final String TO_TEXT = "`to_text`";
    private static final int VERSION = 2;
    private static SQLiteDatabase db;
    private ChatDataBase.DBOpenHelper helper;

    ChatDataBase(Context context)
    {
        this.helper = new ChatDataBase.DBOpenHelper(context, DATABASE_NAME, null, VERSION);
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
        db.execSQL("delete from chat_result;");
    }

    void insertResult(int id, String from, String to, String from_text, String to_text, String from_code, String to_code)
    {
        ContentValues newRow = new ContentValues();
        newRow.put(ID, id);
        newRow.put(FROM, from);
        newRow.put(TO, to);
        newRow.put(FROM_TEXT, from_text);
        newRow.put(TO_TEXT, to_text);
        newRow.put(FROM_CODE, from_code);
        newRow.put(TO_CODE, to_code);
        db.insert(DATABASE_TABLE, null, newRow);
    }

    void deleteResult(int id)
    {
        db.execSQL("delete from chat_result where id=" + id);
    }

    ArrayList<ChatHistoryRow> getResults()
    {
        ArrayList<ChatHistoryRow> chat_history_results = null;
        Cursor cursor = null;
        try
        {
            cursor = db.query(true, DATABASE_TABLE, new String[]{ID, FROM, TO, FROM_TEXT, TO_TEXT, FROM_CODE, TO_CODE},
                    null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                chat_history_results = new ArrayList<>();
                while (!cursor.isAfterLast())
                {
                    chat_history_results.add(new ChatHistoryRow(cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("from")),
                            cursor.getString(cursor.getColumnIndex("to")),
                            cursor.getString(cursor.getColumnIndex("from_text")),
                            cursor.getString(cursor.getColumnIndex("to_text")),
                            cursor.getString(cursor.getColumnIndex("to_code")),
                            cursor.getString(cursor.getColumnIndex("from_code"))));
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
        return chat_history_results;
    }

    private static class DBOpenHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_CREATE = "create table chat_result (`id` INT, `from` TEXT, `to` TEXT,`from_text` TEXT, `to_text` TEXT, `from_code` TEXT, `to_code` TEXT);";

        DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase _db)
        {
            _db.execSQL(DATABASE_CREATE);
        }

        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion)
        {
            _db.execSQL("DROP TABLE IF EXISTS chat_result");
            onCreate(_db);
        }
    }
}

