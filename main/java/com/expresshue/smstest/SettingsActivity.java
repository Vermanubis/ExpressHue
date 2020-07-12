package com.expresshue.smstest;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity
{
    private ListView mEmotions;
    private RecyclerView mContacts;
    private User mUser;
    private DatabaseHelper mDB;
    private ColorDrawable mSelectedColor;
    private String mSelectedHue; //keeps track of the selected hue for communication between lists
    private View mSelectedView; //to allow access to a given selected View within ListView
    private SharedPreferences mPreferences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mUser = User.getInstance();
        mDB = DatabaseHelper.getInstance(this);

        mPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = mPreferences.edit();

        final Button red = (Button) findViewById(R.id.button4);
        final Button blue = (Button) findViewById(R.id.button3);
        final Button purple = (Button) findViewById(R.id.button);
        final Button pink = (Button) findViewById(R.id.button2);
        final Button yellow = (Button) findViewById(R.id.button5);

        mEmotions = (ListView) findViewById(R.id.emotion_select);
        mContacts = (RecyclerView) findViewById(R.id.contact_select);

        setAdapters();

        //TODO Fix glitch where wrong box changes color

        red.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                mSelectedColor = (ColorDrawable) red.getBackground();
                mUser.associateEmotion(mSelectedHue, mSelectedColor.getColor());
                mSelectedView.findViewById(R.id.emotion_color).setBackgroundColor(mSelectedColor.getColor());
                editor.putInt(mSelectedHue, mSelectedColor.getColor());
                editor.commit();
            }
        });

        blue.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                mSelectedColor = (ColorDrawable) blue.getBackground();
                mUser.associateEmotion(mSelectedHue, mSelectedColor.getColor());
                mSelectedView.findViewById(R.id.emotion_color).setBackgroundColor(mSelectedColor.getColor());
                editor.putInt(mSelectedHue, mSelectedColor.getColor());
                editor.commit();
            }
        });

        yellow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                mSelectedColor = (ColorDrawable) yellow.getBackground();
                mUser.associateEmotion(mSelectedHue, mSelectedColor.getColor());
                mSelectedView.findViewById(R.id.emotion_color).setBackgroundColor(mSelectedColor.getColor());
                editor.putInt(mSelectedHue, mSelectedColor.getColor());
                editor.commit();
            }
        });

        pink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                mSelectedColor = (ColorDrawable) pink.getBackground();
                mUser.associateEmotion(mSelectedHue, mSelectedColor.getColor());
                mSelectedView.findViewById(R.id.emotion_color).setBackgroundColor(mSelectedColor.getColor());
                editor.putInt(mSelectedHue, mSelectedColor.getColor());
                editor.commit();
            }
        });

        purple.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                mSelectedColor = (ColorDrawable) purple.getBackground();
                mUser.associateEmotion(mSelectedHue, mSelectedColor.getColor());
                mSelectedView.findViewById(R.id.emotion_color).setBackgroundColor(mSelectedColor.getColor());
                editor.putInt(mSelectedHue, mSelectedColor.getColor());
                editor.commit();
            }
        });
    }

    private void setAdapters() {
        ArrayList<String> hueList = mUser.copyArrayList(mUser.getEmotionsAsString());
        hueList.add(getString(R.string.custom_hue_creation));
        CustomHueListAdapter emotionSelectAdapter = new CustomHueListAdapter(this, hueList);
        mEmotions.setAdapter(emotionSelectAdapter);

        CustomContactListAdapter contactSelectAdapter = new CustomContactListAdapter(mDB.getContactsAsString(this), this); //fill spinner with contacts
        mContacts.setLayoutManager(new LinearLayoutManager(this));
        mContacts.setAdapter(contactSelectAdapter);
    }


    private void resetAdapters() //reset the contact list to reflect newly selected hue
    {
        CustomContactListAdapter contactSelectAdapter = new CustomContactListAdapter(mDB.getContactsAsString(this), this); //fill spinner with contacts
        mContacts.setLayoutManager(new LinearLayoutManager(this));
        mContacts.setAdapter(contactSelectAdapter);
    }

    /************************CustomHueListAdapter************************
     * Basically just a ListView adapter to store the hue names and their
     * respective colors. Every time a hue is clicked, the name
     * is stored in a global variable (mSelectedHue) for easy
     * communication between this and the contacts list
     ****************************************************************/

    //TODO make it so it doesn't crash when custom view is clicked

    private class CustomHueListAdapter extends ArrayAdapter<String>
    {
        private ArrayList<String> emotions;
        private Context context;
        int selectedPosition = 0; //variable to keep track of selected position in the list

        private CustomHueListAdapter(Context context, ArrayList<String> emotions) {
            super(context, R.layout.activity_settings, emotions);
            this.emotions = emotions;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE); //get layout inflater

            final View view = inflater.inflate(R.layout.activity_settings_hue_list_item, parent, false);
            final int pos = position;

            TextView emotionName = (TextView) view.findViewById(R.id.emotion_name);
            Button emotionColor = (Button) view.findViewById(R.id.emotion_color);
            EditText customName = (EditText) view.findViewById(R.id.custom_name);

            mSelectedHue = emotions.get(selectedPosition); //get hue of currently selected position

            emotionName.setText(emotions.get(position));

            if (position < emotions.size() - 1) //if not the "custom hue" item
            {
                view.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        selectedPosition = pos;
                        mSelectedHue = emotions.get(pos);
                        mSelectedView = view;
                        notifyDataSetChanged();
                        resetAdapters();
                    }
                });

                emotionColor.setBackgroundColor(mUser.getAssociatedEmotion(emotions.get(position)));
                emotionName.setVisibility(View.VISIBLE);
                customName.setVisibility(View.GONE);
            }

            else //if the "custom hue" item
            {
                customName.setHint(emotions.get(position));
                emotionName.setVisibility(View.GONE);
                customName.setVisibility(View.VISIBLE);
            }

            if (selectedPosition == position)
                view.setBackgroundColor(Color.parseColor("#000000")); //TODO make it so the box isn't pure black/white
            else
                view.setBackgroundColor(Color.parseColor("#ffffff"));

            return view;
        }

    }

    private class CustomContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView contactName;
        CheckBox hueEnabledForContact;

        private CustomContactHolder(View itemView)
        {
            super(itemView);
            itemView.setOnClickListener(this);
            contactName = (TextView) itemView.findViewById(R.id.contact_name);
            hueEnabledForContact = (CheckBox) itemView.findViewById(R.id.hue_enabled_for_contact);
        }

        @Override
        public void onClick(View v)
        {

        }

        private void bindResult(String contact)
        {
            contactName.setText(contact);
            hueEnabledForContact.setChecked(mDB.canSendOrReceive(mDB.getContactNumber( //checks the database to see if the given contact has the selected hue enabled
                    SettingsActivity.this, contactName.getText().toString()), mSelectedHue + "_send"));

            hueEnabledForContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                {
                    mDB.updateSendReceivePermissions(mDB.getContactNumber(SettingsActivity.this, contactName.getText().toString()),
                            mSelectedHue + "_send", hueEnabledForContact.isChecked());
                }
            });
        }
    }

    private class CustomContactListAdapter extends RecyclerView.Adapter<CustomContactHolder>
    {
        private ArrayList<String> contacts;
        private Context context;

        private CustomContactListAdapter(ArrayList<String> contacts, Context context)
        {
            this.contacts = contacts;
            this.context = context;
        }

        @Override
        public CustomContactHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.activity_settings_contact_list_item, parent, false);


            return new CustomContactHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomContactHolder holder, int position)
        {
            String row = contacts.get(position);
            holder.bindResult(row);
        }

        @Override
        public int getItemCount()
        {
            return contacts.size();
        }
    }
}
