package com.expresshue.smstest;

/*
    CHANGES AS OF 3/27/17

    -Converted most of the button instance variables to local variables
    -Removed all color buttons and added a spinner to select emotions
    -Added getColor() method to retrieve the color code from message field (mMessageField)
    -Added populateSelectEmotion() method that fills the spinner with the emotion names
        found in the User class mEmotionMap HashMap
    -Added populateTestData() method to just, well... test stuff
    -Changed a bunch of instance variable names to match weird Android convention (mVariableName)

    CHANGES AS OF 4/5/17

    -SQLite database now stores messages locally with the help of DatabaseHelper class
    -Design pattern changed a bit. The User class still has an ArrayList that holds all the messages
        but it's loaded from the database simply for the purpose of not having to query the database
        every time the UI is updated or a new message is sent/received

    CHANGES AS OF 4/20/17

    -SQLite database now stores messages locally with the help of DatabaseHelper class
    -Design pattern changed a bit. The User class still has an ArrayList that holds all the messages
        but it's loaded from the database simply for the purpose of not having to query the database
        every time the UI is updated or a new message is sent/received

    CHANGES AS OF 4/25/17

    Okay, so here's a basic rundown of how everything runs:

    All of the messages are in the database, so upon selecting a contact, the DatabaseHelper class
    gets the SQLite database and the method getMessagesForContact supplies an ArrayList with all
    the messages for the contact whose number it was given. That ArrayList is then stored
    in the User class so that the DB doesn't have to be dinged every time something happens.

    Next, user preferences are retrieved from the SharedPreferences long-term storage; things like
    color associations with hues.

    From there, the ArrayList with all the messages is set an adapter where it's displayed
    in the ListView.

 */

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.database.Cursor;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    //CONSTANTS

    private static final int PICK_CONTACT = 10;
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;


    private EditText mTargetNum;
    private EditText mMessageField;
    private Spinner mEmotionSelect;
    private ListView mMessageList;
    private CustomMessageAdapter mArrayAdapter;
    private User mUser;
    private DatabaseHelper mDB;

    //this line added by ssheehy
    private IntentFilter filter1;

    private Uri contactURI;
    private String contactID;
    private String incoming;
    private String mContactNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mDB = DatabaseHelper.getInstance(getApplicationContext());
        mContactNumber = "555-555-5555";

        initializePreferences();
        initializeMessages();
        askForContactPermission();

        //test statements

        Cursor c = mDB.getReadableDatabase().rawQuery("select * from Contacts", null);
        System.out.println(DatabaseUtils.dumpCursorToString(c));
        //mDB.getWritableDatabase().execSQL("drop table Messages");
        //mDB.addNewCustomHue();

        final Button positiveSendButton = (Button) findViewById(R.id.pos_send_button);
        final Button negativeSendButton = (Button) findViewById(R.id.neg_send_button);
        Button overviewButton = (Button) findViewById(R.id.contact_overview_button);
        Button settingsButton = (Button) findViewById(R.id.settings_button);
        final Button contactsButton = (Button) findViewById(R.id.contactsBtn);
        mEmotionSelect = (Spinner) findViewById(R.id.emotion_select);
        mMessageList = (ListView) findViewById(R.id.messageList);
        mTargetNum = (EditText) findViewById(R.id.targetNum);
        mMessageField = (EditText) findViewById(R.id.message);
        mMessageList.setAdapter(mArrayAdapter);

        setAdapters();

        updateUI();

        contactsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent getContacts = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(getContacts, PICK_CONTACT);
            }
        });

        mEmotionSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() //Dropdown menu for selecting hue
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                mMessageField.setBackgroundColor //get String from spinner and convert it to a color
                        (mUser.getAssociatedEmotion(mEmotionSelect.getSelectedItem().toString()));

                if(isPureEmotion(mEmotionSelect.getSelectedItem().toString()))
                {
                    if(isPositiveEmotion(mEmotionSelect.getSelectedItem().toString()))
                    {
                        positiveSendButton.setEnabled(true);
                        negativeSendButton.setEnabled(false);
                    }

                    else
                    {
                        positiveSendButton.setEnabled(false);
                        negativeSendButton.setEnabled(true);
                    }
                }
                else
                {
                    positiveSendButton.setEnabled(true);
                    negativeSendButton.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });

        positiveSendButton.setOnClickListener(new View.OnClickListener() //send message w/ positive
        {
            public void onClick(View v)
            {
                String phone = mTargetNum.getText().toString();
                String msgContent = mMessageField.getText().toString().trim();

                //if (phone.length() > 0 && msg.length() > 0 && color != 0)
                if(msgContent.length() > 0 && mUser.getSelectedColor(mMessageField.getBackground()) != 0)
                {
                    Message sentMsg = new Message(msgContent, (String) mEmotionSelect.getSelectedItem(),
                            true, true);
                    mUser.addMessage(sentMsg); //add new message to temporary storage
                    mDB.addMessageForNumber(mContactNumber, sentMsg); //add new message to long-term storage
                    msgContent += "#" + sentMsg.getEmotion();
                    updateUI(); //add new message to UI
                    //sendSMS(phone, msg);
                    mMessageField.setText(null); //clear message field
                }
                else
                {
                    Toast.makeText(getBaseContext(),
                            "Fields cannot be empty.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        negativeSendButton.setOnClickListener(new View.OnClickListener() //send message w/ negative
        {
            public void onClick(View v)
            {
                String phone = mTargetNum.getText().toString();
                String msgContent = mMessageField.getText().toString().trim();

                //if (phone.length() > 0 && msg.length() > 0 && color != 0)
                if(msgContent.length() > 0 && mUser.getSelectedColor(mMessageField.getBackground()) != 0)
                {
                    Message sentMsg = new Message(msgContent, (String) mEmotionSelect.getSelectedItem(),
                            true, false);
                    mUser.addMessage(sentMsg);
                    mDB.addMessageForNumber(mContactNumber, sentMsg);
                    msgContent += "#" + sentMsg.getEmotion();
                    updateUI(); //add new message to UI
                    //sendSMS(phone, msg);
                    mMessageField.setText(null); //clear message field
                }
                else
                {
                    Toast.makeText(getBaseContext(),
                            "Fields cannot be empty.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mTargetNum.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                //mContactNumber = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });

        overviewButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(MainActivity.this, OverviewActivity.class);
                startActivity(i);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });

         //these two lines added by ssheehy
        filter1 = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(GetSMS, filter1);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateUI();
    }

    @Override
    public void onActivityResult(int req, int result, Intent data)
    {
        super.onActivityResult(req, result, data);
        String hasPhone = null;
        String phoneNum = null;
        if (req == PICK_CONTACT && result == RESULT_OK)
        {
            contactURI = data.getData();
            //mContactNumber = mTargetNum.getText().toString();
            Cursor cursorID = getContentResolver().query(
                    contactURI, null, null, null, null);

            if (cursorID.moveToFirst())
            {
                contactID = cursorID.getString(cursorID.getColumnIndexOrThrow(
                        ContactsContract.Contacts._ID));
                hasPhone = cursorID.getString(cursorID.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER));
            }

            cursorID.close();

            if (hasPhone.equalsIgnoreCase("1"))
            {
                Cursor cursorNum = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID,
                        null, null);

                if (cursorNum.moveToFirst())
                {
                    phoneNum = cursorNum.getString(cursorNum.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    cursorNum.close();
                }
            }

            mTargetNum.setText(phoneNum, TextView.BufferType.EDITABLE);
        }
    }

    /************************askForContactPermission*********
     Requests runtime permission to view contacts
     *******************************************************/

    private void askForContactPermission()
    {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    /************************initializeMessages***********
     Get messages from database for given contact number
     *******************************************************/

    private void initializeMessages()
    {
        mUser.setMessageList(mDB.getMessagesForNumber(mContactNumber));
    }

    /************************initializePreferences***********
     Gets basic hue settings from SharedPreferences(color,
     polarity)
     *******************************************************/

    private void initializePreferences()
    {
        mUser = User.getInstance();
        SharedPreferences preferences = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        mUser.associateEmotion(getString(R.string.angry),
                preferences.getInt(getString(R.string.angry), 0));

        mUser.associateEmotion(getString(R.string.sad),
                preferences.getInt(getString(R.string.sad), 0));

        mUser.associateEmotion(getString(R.string.fear),
                preferences.getInt(getString(R.string.fear), 0));

        mUser.associateEmotion(getString(R.string.happy),
                preferences.getInt(getString(R.string.happy), 0));

        mUser.associateEmotion(getString(R.string.maybe),
                preferences.getInt(getString(R.string.maybe), 0));

        mUser.associateEmotion(getString(R.string.hug),
                preferences.getInt(getString(R.string.hug), 0));

    }

    /************************isNotLockedEmotion************************
     * The four "pure" emotions have fixed polarity. This method checks
     * to see if selected emotion is one of the "pure" emotions to
     * ensure they're not changed.
     ****************************************************************/

    private boolean isPureEmotion(String emotion)
    {
        if(emotion.equals("angry") || emotion.equals("sad") || emotion.equals("fear") ||
            emotion.equals("happy"))
            return true;
        else
            return false;
    }

    private boolean isPositiveEmotion(String emotion)
    {
        if(emotion.equals("happy"))
            return true;
        else
            return false;
    }

    /************************sendSMS************************
     Resets the adapter
     *******************************************************/

    private void sendSMS(String phoneNum, String msgSMS)
    {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNum, null, msgSMS, null, null);
    }

    private void setAdapters()
    {
        CustomMessageAdapter messageListAdapter = new CustomMessageAdapter(this, mUser.getMessageList());
        mMessageList.setAdapter(messageListAdapter);

        ArrayAdapter emotionSelectAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_dropdown_item, mUser.getEmotionsAsString());

        mEmotionSelect.setAdapter(emotionSelectAdapter);
    }

    /************************updateUI************************
        Resets the adapter
     *******************************************************/

    public void updateUI()
    {
        mArrayAdapter = new CustomMessageAdapter(this, mUser.getMessageList());
        mMessageList.setAdapter(mArrayAdapter);
    }

    //The BroadcastReceiver that listens for SMS
    /*
    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase("android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED")) {
                Log.d(TAG,"Bluetooth connect");
            }
        }
    };
    */

    //public class GetSMS extends BroadcastReceiver
    private final BroadcastReceiver GetSMS = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(SMS_RECEIVED))
            {
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs = null;
                incoming = "";

                if (bundle != null)
                {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];

                    for (int i = 0; i < msgs.length; i++)
                    {
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        incoming += msgs[i].getMessageBody();
                    }

                    //Message receivedMsg = new Message(incoming ,"angry");
                   // mUser.addMessage(receivedMsg);
                    //updateUI(); //add new message to UI
                }
            }
        }
    };

    //TODO rewrite as RecyclerView


    public class CustomMessageAdapter extends ArrayAdapter<Message>
    {
        private final Context context;
        private ArrayList<Message> messages;

        private CustomMessageAdapter(Context context, ArrayList<Message> messages)
        {
            super(context, R.layout.activity_main, messages);
            this.context = context;
            this.messages = messages;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE); //get layout inflater

            View view;
            TextView chatBubble;
            TextView timeStamp;
            TextView polaritySymbol;

            if(messages.get(position).isMine())  //display left or right layouts depending on who wrote the message
            {
                view = inflater.inflate(R.layout.row_left, parent, false); //assign view to each message
                chatBubble = (TextView) view.findViewById(R.id.text_view_left);
                timeStamp = (TextView) view.findViewById(R.id.timestamp_left);
                polaritySymbol = (TextView) view.findViewById(R.id.polarity_symbol_left);

                chatBubble.setText(messages.get(position).getContent());
                chatBubble.setBackgroundColor(messages.get(position).getEmotion());

                timeStamp.setText(messages.get(position).getTimeStamp());

                if(messages.get(position).isPositive())
                    polaritySymbol.setText(getString(R.string.positive_symbol));
                else
                    polaritySymbol.setText(getString(R.string.negative_symbol));
            }

            else
            {
                view = inflater.inflate(R.layout.row_right, parent, false);
                chatBubble = (TextView) view.findViewById(R.id.text_view_right);
                timeStamp = (TextView) view.findViewById(R.id.timestamp_right);
                polaritySymbol = (TextView) view.findViewById(R.id.polarity_symbol);

                chatBubble.setText(messages.get(position).getContent());
                chatBubble.setBackgroundColor(messages.get(position).getEmotion());

                timeStamp.setText(messages.get(position).getTimeStamp());


                if(messages.get(position).isPositive())
                    polaritySymbol.setText(getString(R.string.positive_symbol));
                else
                    polaritySymbol.setText(getString(R.string.negative_symbol));
            }

            return view;
        }
    }
}

/*
SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("angry", Color.parseColor("#ff0000"));
        editor.putInt("sad", Color.parseColor("#ffff00"));
        editor.putInt("fear", Color.parseColor("#ff00ff"));
        editor.putInt("happy", Color.parseColor("#2b90f5"));
        editor.putBoolean("angry_boolean", false);
        editor.putBoolean("sad_boolean", false);
        editor.putBoolean("fear_boolean", false);
        editor.putBoolean("happy_boolean",  true);
        editor.commit();
 */