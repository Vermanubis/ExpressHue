package com.expresshue.smstest;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.Set;

/******************************User******************************
    The User class holds all of the pertinent user information
    such as color-emotion associations (with HashMap) and other.

    The User class:

    -Holds messages and user preferences in temporary storage
        for quick access

    -Sorts all messages into sub-arrays of their respective
        colors

    -Communicates with spinners to fill them with hue names
        for selection
 *****************************User*******************************/

public class User
{
    private Map<String, Integer> mEmotionMap = new HashMap<>();
    private Map<String, Boolean> mEmotionPolarityMap = new HashMap<>();
    private ArrayList<Message> mMessages;
    private ArrayList<Message> mSortedMessages = new ArrayList<>();
    private static User mUser;

    public void associateEmotion(String emotion, int color) //creates association between passed values
    {
        mEmotionMap.put(emotion, color);
    }

    public int getAssociatedEmotion(String emotion) //returns the color code associated with given emotion
    {
        return mEmotionMap.get(emotion);
    }

    public boolean isPositiveEmotion(String emotion)
    {
        return mEmotionPolarityMap.get(emotion);
    }

    public void addMessage(Message message)
    {
        mMessages.add(message);
    }

    public Message getMessage(int position)
    {
        return mMessages.get(position);
    }

    public ArrayList<Message> getMessageList() {
        return mMessages;
    }

    public void setMessageList(ArrayList<Message> messages)
    {
       mMessages = messages;
    }

    public Set<String> getKeySet()
    {
        return mEmotionMap.keySet();
    }

    public void assignPolarity(String emotion, boolean polarity)
    {
        mEmotionPolarityMap.put(emotion, polarity);
    }

    public static User getInstance() //returns sole instance of User class
    {
        if(mUser == null)
        {
            mUser = new User();
        }
        return mUser;
    }


     /************************analyzeMessages************************
     Shallow copies messages and sorts the copies according
     to alphabetical order of emotion for two subsequent
     binary searches: the first of which finds the first
     instance of an emotion sub-array; the second finds
     the last. The count is then mapped in a HashMap
     that is returned for display.
     ****************************************************************/

    public Map<String, Integer> analyzeMessages()
    {
        Map<String, Integer> emotionTally = new HashMap<>(mEmotionMap.size());
        int first; //first index of sub-array
        int last; //last index of sub-array
        Message msg; //dummy message for passing key value

        mSortedMessages.clear();

        for(Message item : mMessages)
        {
            mSortedMessages.add(item);
        }
        Collections.sort(mSortedMessages, Message.byLexicography());

        for(String key : mEmotionMap.keySet()) //iterate through each unique emotion
        {
            msg = new Message("", key);
            first = firstIndexOf(mSortedMessages, msg, Message.byLexicography());
            last = lastIndexOf(mSortedMessages, msg, Message.byLexicography());

            if(first > -1) //at least one such message exists
                emotionTally.put(key, (last-first)+1); //associate number of instances with given emotion
            else
                emotionTally.put(key, 0);

        }
        return emotionTally;
    }

    public <Key> ArrayList<Key> copyArrayList(ArrayList<Key> a)
    {
        ArrayList<Key> arrayListCopy = new ArrayList<>();
        for(Key i : a)
        {
            arrayListCopy.add(i);
        }
        return arrayListCopy;
    }

    /************************getEmotionSubArray************************
     Finds the sub-array of the given emotion and returns an ArrayList
     containing all messages of the given emotion
     ****************************************************************/

    public ArrayList<Message> getEmotionSubArray(String emotion)
    {
        ArrayList<Message> emotionSubArray = new ArrayList<>();
        Message msg = new Message("", emotion);

        int first = firstIndexOf(mSortedMessages, msg, Message.byLexicography());
        int last = lastIndexOf(mSortedMessages, msg, Message.byLexicography());

        if(last >= 0)
        {
            for (int i = first; i <= last; i++)
                emotionSubArray.add(mSortedMessages.get(i));
        }

        return emotionSubArray;
    }

    /************************getSelectedColor************************
     Converts the ColorDrawable from the message box to a its color code
     ****************************************************************/

    public int getSelectedColor(Drawable color)
    {
        ColorDrawable cd = (ColorDrawable) color;
        return cd.getColor();
    }

    /************************getEmotionsAsString************************
     Populates an array list with emotions
     *********************************************************************/

    public ArrayList<String> getEmotionsAsString()
    {
        ArrayList<String> emotions = new ArrayList<>();
        for(String key : mUser.getKeySet())
        {
            emotions.add(key);
        }
        Collections.sort(emotions);
        return emotions;
    }

     /************************firstIndexOf************************
     Implements a modified binary search wherein the first instance
     of the sought key in the array is found and returned
     ****************************************************************/

    private static <Key> int firstIndexOf(ArrayList<Key> a, Key key, Comparator<Key> comparator)
    {
        if(a == null || key == null || comparator == null)
            throw new NullPointerException();

        int lo = 0;
        int hi = (a.size()) - 1;

        while(lo <= hi)
        {
            int mid = lo + (hi - lo) / 2;

            if(comparator.compare(key, a.get(mid)) < 0)
                hi = mid - 1;
            else if(comparator.compare(key, a.get(mid)) > 0)
                lo = mid + 1;
            else
            {
                while(mid > lo && comparator.compare(key, a.get(mid-1)) == 0)
                //keep checking left until first is found
                {
                    mid--;
                }
                return mid;
            }
        }
        return -1;
    }

    /************************lastIndexOf************************
     Implements a modified binary search wherein the last instance
     of the sought key in the array is found and returned
     ****************************************************************/

    private static <Key> int lastIndexOf(ArrayList<Key> a, Key key, Comparator<Key> comparator)
    {
        if(a == null || key == null || comparator == null)
            throw new NullPointerException();

        int lo = 0;
        int hi = (a.size()) - 1;

        while(lo <= hi)
        {
            int mid = lo + (hi - lo) / 2;

            if(comparator.compare(key, a.get(mid)) < 0)
                hi = mid - 1;
            else if(comparator.compare(key, a.get(mid)) > 0)
                lo = mid + 1;
            else
            {
                while(mid < hi && comparator.compare(key, a.get(mid+1)) == 0)
                //keep checking right until last is found
                {
                    mid++;
                }
                return mid;
            }
        }
        return -1;
    }
}