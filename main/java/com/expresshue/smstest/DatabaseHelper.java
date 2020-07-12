package com.expresshue.smstest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

import com.expresshue.smstest.MessagesSchema.MessagesTable;
import com.expresshue.smstest.ContactsSchema.ContactsTable;

import java.util.ArrayList;
import java.util.Collections;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Contacts.db";
    private static DatabaseHelper dbHelper;

    private DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance(Context context)
    {
        if(dbHelper == null)
        {
            dbHelper = new DatabaseHelper(context);
        }
        return dbHelper;
    }

    /*******************addAllContactsToDatabase**********************
     Initializes the database with all contacts
     ****************************************************************/

    public void addAllContactsToDatabase(Context context)
    {
        ArrayList<String> allNumbers = getAllContactNumbers(context);
        String updateStatement;

        for(int i = 0; i < allNumbers.size(); i++)
        {
            //System.out.println(allNumbers.get(i) + "&*^(&*^(*&");
            updateStatement = "INSERT INTO " + ContactsSchema.ContactsTable.NAME + " (" +
                    ContactsTable.Cols.MOBILE_NUMBER + ") VALUES(" + "'" + allNumbers.get(i) + "')";
            dbHelper.getWritableDatabase().execSQL(updateStatement);
        }
        dbHelper.close();
    }

    /************************addMessageForNumber**********************
     Adds new message to the database
     ****************************************************************/

    public void addMessageForNumber(String number, Message msg)
    {
        String query = "INSERT into " + MessagesTable.NAME +
                " (mobile_number, message, owner, emotion, polarity, date) VALUES" +
                "('" + number + "', '" + msg.getContent() + "', " + convertBoolToInt(msg.isMine()) + ", '" +
                msg.getEmotionName() + "', '" + convertBoolToInt(msg.isPositive()) + "', '" + msg.getTimeStamp() + "')";

        dbHelper.getWritableDatabase().execSQL(query);
        dbHelper.close();
    }

    public void addNewCustomHue()
    {
        dbHelper.getWritableDatabase().execSQL("CREATE TABLE if not exists " + MessagesSchema.MessagesTable.NAME + "(" +
                MessagesTable.Cols.MOBILE_NUMBER + " VARCHAR(255), " +
                MessagesTable.Cols.MESSAGE + " VARCHAR(255)," + //message
                MessagesTable.Cols.OWNER + " BOOLEAN," + //whose message it is
                MessagesTable.Cols.EMOTION + " VARCHAR(255)," + //name of hue
                MessagesTable.Cols.POLARITY + " BOOLEAN," + //polarity of hue; false = negative; true = positive
                MessagesTable.Cols.DATE + " DATETIME)" //date sent
        );
    }

    /*******************canSendOrReceive******************************
     Takes a contact's number and column name to check send/receive
     permissions for the given contact. If send/receive is disabled,
     returns false; true if enabled.
     ****************************************************************/

    public boolean canSendOrReceive(String contactNumber, String colToCheck)
    {
        int result = 0;
        String[] projection = {colToCheck};
        String selection = ContactsTable.Cols.MOBILE_NUMBER + " = ?";
        String[] selectionArgs = {contactNumber};

        Cursor c = dbHelper.getReadableDatabase().query(
                ContactsSchema.ContactsTable.NAME, //table to query
                projection, //columns to return (SELECT __ from)
                selection, //query parameters (WHERE selection = selectionArgs)
                selectionArgs,
                null,
                null,
                null
        );

        if(c.moveToFirst())
        {
            result = c.getInt(0);
            //System.out.println(result + "********");
        }
        c.close();

        if(result == 1)
            return true;
        else
            return false;

    }

    public int convertBoolToInt(boolean b)
    {
        if(b)
            return 1;
        else
            return 0;
    }

    public String convertBoolToPolaritySymbol(boolean b)
    {
        if(b)
            return "+";
        else
            return "-";
    }

    /************************getAllContactNumbers************************
     Returns all contact numbers
     ****************************************************************/

    public ArrayList<String> getAllContactNumbers(Context context)
    {
        ArrayList<String> allNumbers = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null);

        if(c.moveToFirst())
        {
            do
            {
                allNumbers.add(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            }
            while(c.moveToNext());
        }
        c.close();

        return allNumbers;
    }

    /************************getContactsAsString************************
     Populates an array list with contacts
     ****************************************************************/

    public ArrayList<String> getContactsAsString(Context context)
    {
        ArrayList<String> contacts = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);

        if(c.moveToFirst())
        {
            do
            {
                contacts.add(c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
            }
            while(c.moveToNext());
        }
        c.close();
        Collections.sort(contacts);
        return contacts;
    }

    /************************getContactNumber************************
     Gets specified contact number given a contact name
     ****************************************************************/

    public String getContactNumber(Context context, String contactName)
    {
        String contactNumber = null;
        String contactId = null;
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                "DISPLAY_NAME = '" + contactName + "'", null, null);


        if (c.moveToFirst())
        {
            contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
        }
        c.close();


        Cursor p = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                null,
                null);

        if(p.moveToFirst())
        {
            contactNumber = p.getString(p.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        p.close();

        return contactNumber;
    }

     /*******************getMessagesForNumber*************************
     Takes a contact number and returns all messages associated
      with that number
     ****************************************************************/

    public ArrayList<Message> getMessagesForNumber(String number)
    {
        ArrayList<Message> messages = new ArrayList<>();

        String[] projection = {MessagesTable.Cols.MESSAGE, MessagesTable.Cols.EMOTION,
                MessagesTable.Cols.OWNER, MessagesTable.Cols.POLARITY};
        String selection = MessagesTable.Cols.MOBILE_NUMBER + " = ?";
        String[] selectionArgs = {number};
        boolean owner;
        boolean polarity;

        Cursor cursor = dbHelper.getReadableDatabase().query(
                MessagesSchema.MessagesTable.NAME, //table to query
                projection, //columns to return (SELECT __ from)
                selection, //query parameters (WHERE selection = selectionArgs)
                selectionArgs,
                null,
                null,
                null
        );

        if(cursor.moveToFirst())
        {
            do
            {
                polarity = true;
                owner = true;
                if(cursor.getInt(2) == 0)
                    owner = false;
                if(cursor.getInt(3) == 0)
                    polarity = false;

                messages.add(new Message(cursor.getString(0), cursor.getString(1),
                        owner, polarity));
            }
            while(cursor.moveToNext());
        }
        cursor.close();

        return messages;
    }

    /*******************updateSendReceivePermissions******************
     Takes a column name, true or false value and associated contact
     number and updates the Contacts table with the specified send
     and receive permissions
     ****************************************************************/

    public void updateSendReceivePermissions(String contactNumber, String colToUpdate, boolean value)
    {
        int valueConversion = 0;
        if(value)
            valueConversion = 1;

        String updateStatement = "UPDATE Contacts SET " + colToUpdate + " = " + valueConversion +
                " WHERE mobile_number = '" + contactNumber + "'";

        dbHelper.getWritableDatabase().execSQL(updateStatement);
        dbHelper.close();
    }

    public void deleteAll()
    {
        dbHelper.getWritableDatabase().execSQL("delete from Messages");
        dbHelper.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //Table for storing messages
        db.execSQL("CREATE TABLE if not exists " + MessagesSchema.MessagesTable.NAME + "(" +
                MessagesTable.Cols.MOBILE_NUMBER + " VARCHAR(255), " +
                MessagesTable.Cols.MESSAGE + " VARCHAR(255)," + //message
                MessagesTable.Cols.OWNER + " BOOLEAN," + //whose message it is
                MessagesTable.Cols.EMOTION + " VARCHAR(255)," + //name of hue
                MessagesTable.Cols.POLARITY + " BOOLEAN," + //polarity of hue; false = negative; true = positive
                MessagesTable.Cols.DATE + " DATETIME)" //date sent
        );

        //Table for storing hue settings and hue information
        db.execSQL("CREATE TABLE if not exists " + ContactsSchema.ContactsTable.NAME + "(" +
                ContactsTable.Cols.MOBILE_NUMBER + " VARCHAR(255) PRIMARY KEY, " +
                ContactsTable.Cols.ANGRY_SEND + " BOOLEAN NOT NULL DEFAULT 1, " + //if can send angry hues to
                ContactsTable.Cols.SAD_SEND + " BOOLEAN NOT NULL DEFAULT 1, " +
                ContactsTable.Cols.FEAR_SEND + " BOOLEAN NOT NULL DEFAULT 1, " +
                ContactsTable.Cols.HAPPY_SEND + " BOOLEAN NOT NULL DEFAULT 1, " +
                ContactsTable.Cols.MAYBE_SEND + " BOOLEAN NOT NULL DEFAULT 1, " +
                ContactsTable.Cols.HUG_SEND + " BOOLEAN NOT NULL DEFAULT 1, " +
                ContactsTable.Cols.ANGRY_RECEIVE + " BOOLEAN NOT NULL DEFAULT 1, " + //if can receive angry hues from
                ContactsTable.Cols.SAD_RECEIVE + " BOOLEAN NOT NULL DEFAULT 1, " +
                ContactsTable.Cols.FEAR_RECEIVE + " BOOLEAN NOT NULL DEFAULT 1, " +
                ContactsTable.Cols.HAPPY_RECEIVE + " BOOLEAN NOT NULL DEFAULT 1, " +
                ContactsTable.Cols.MAYBE_RECEIVE + " BOOLEAN NOT NULL DEFAULT 1, " +
                ContactsTable.Cols.HUG_RECEIVE + " BOOLEAN NOT NULL DEFAULT 1)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int newVersion, int oldVersion)
    {

    }
}