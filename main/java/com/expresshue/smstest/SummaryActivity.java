package com.expresshue.smstest;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class SummaryActivity extends AppCompatActivity
{
    private ListView mMessageList;
    private User mUser;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        mUser = User.getInstance();
        mMessageList = (ListView) findViewById(R.id.messageList);
        updateUI();
    }

    private void updateUI()
    {
        CustomAdapter adapter = new CustomAdapter
                (this, mUser.getEmotionSubArray(getIntent().getStringExtra("EMOTION_NAME")));
        mMessageList.setAdapter(adapter);
    }

    public class CustomAdapter extends ArrayAdapter<Message>
    {
        private final Context context;
        private ArrayList<Message> messages;

        private CustomAdapter(Context context, ArrayList<Message> messages)
        {
            super(context, R.layout.activity_overview, messages);
            this.messages = messages;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE); //get layout inflater

            View view = inflater.inflate(R.layout.row_left, parent, false);

            TextView chatBubble = (TextView) view.findViewById(R.id.text_view_left);
            TextView timeStamp = (TextView) view.findViewById(R.id.timestamp_left);

            chatBubble.setText(messages.get(position).getContent());
            chatBubble.setBackgroundColor(messages.get(position).getEmotion());

            timeStamp.setText(messages.get(position).getTimeStamp());

            if (!messages.get(position).isMine()) //determine whose message this is to inflate appropriate layout
            {
                view = inflater.inflate(R.layout.row_right, parent, false);
                chatBubble = (TextView) view.findViewById(R.id.text_view_right);
                timeStamp = (TextView) view.findViewById(R.id.timestamp_right);
                chatBubble.setText(messages.get(position).getContent());
                chatBubble.setBackgroundColor(messages.get(position).getEmotion());
                timeStamp.setText(messages.get(position).getTimeStamp());
            }

            return view;
        }
    }
}
