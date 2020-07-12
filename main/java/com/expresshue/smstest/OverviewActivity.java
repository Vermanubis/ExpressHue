package com.expresshue.smstest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Map;

public class OverviewActivity extends AppCompatActivity
{
    private ListView mEmotions;
    private CustomAdapter mAdapter;
    private User mUser;
    private Map<String, Integer> mEmotionTally;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        mEmotions = (ListView) findViewById(R.id.emotion_ratios);

        populateTestData();
        mEmotionTally = mUser.analyzeMessages();

        updateUI();
    }

    private void updateUI()
    {
        mAdapter = new CustomAdapter(mEmotionTally);
        mEmotions.setAdapter(mAdapter);
    }

    public class CustomAdapter extends BaseAdapter
    {
        private final ArrayList data;

        private CustomAdapter(Map<String, Integer> emotions)
        {
            data = new ArrayList();
            data.addAll(emotions.entrySet()); //store HashMap contents in ArrayList
        }

        @Override
        public int getCount()
        {
            return data.size();
        }

        @Override
        public Map.Entry<String, Integer> getItem(int position)
        {
            return (Map.Entry) data.get(position); //get key-value pair from HashMap
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        /*-----------------getView-----------------*/
        //Called every time the screen needs to
        //be redrawn. Takes every element in the
        //adapter and assigns a view thereto
        /*-----------------getView-----------------*/

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final View view;
            float emotionRatio = 0;

            if (convertView == null)
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_overview, parent, false);
            else
                view = convertView;

            final TextView listItem = (TextView) view.findViewById(R.id.overview_list_item);

            final Map.Entry<String, Integer> item = getItem(position);

            emotionRatio = (item.getValue()*100)/mUser.getMessageList().size(); //calculate percentage

            listItem.setBackgroundColor(mUser.getAssociatedEmotion(item.getKey()));
            listItem.setText(item.getKey() + " " + emotionRatio + "%");

            listItem.setOnClickListener(new View.OnClickListener() //go to SummaryActivity to display all given messages
            {
                @Override
                public void onClick(View view)
                {
                    Intent i = new Intent(OverviewActivity.this, SummaryActivity.class);
                    i.putExtra("EMOTION_NAME", item.getKey());
                    startActivity(i);
                }
            });

            return view;
        }
    }

    private void populateTestData()
    {
        mUser = User.getInstance();
        mUser.associateEmotion("angry", Color.parseColor("#ff0000"));
        mUser.associateEmotion("sad", Color.parseColor("#ffff00"));
        mUser.associateEmotion("fear", Color.parseColor("#ff00ff"));
        mUser.associateEmotion("happy", Color.parseColor("#2b90f5"));
        mUser.associateEmotion("hug", Color.parseColor("#6CF6D2"));
    }
}