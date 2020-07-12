package com.expresshue.smstest;

import java.util.Date;
import java.util.Comparator;

/***********************Message**************************

    Class for containing all the pertinent data regarding
    messages: content, time stamp, ownership, associated
    hue and positive/negative polarity.

    Also contains comparators for sorting in chronological,
    reverse chronological and alphabetical order

***********************Message**************************/

public class Message implements Comparable<Message>
{
    private String content;
    private String emotion;
    private boolean owner;
    private boolean polarity;
    private Date timeStamp;
    private User user;

    public Message(String c, String e, boolean o, boolean p)
    {
        this.content = c;
        this.emotion = e;
        this.owner = o;
        this.polarity = p;
        user = User.getInstance();
        this.timeStamp = new Date();
    }

    public Message(String c, String e) //Secondary constructor for use in binary search
    {
        this.content = c;
        this.emotion = e;
    }

    public String getTimeStamp()
    {
        return timeStamp.toString();
    }

    public String getContent()
    {
        return content;
    }

    public int getEmotion()
    {
        return user.getAssociatedEmotion(this.emotion);
    }

    public String getEmotionName()
    {
        return this.emotion;
    }

    public boolean isMine()
    {
        return owner;
    }

    public boolean isPositive()
    {
        return this.polarity;
    }

    public final int compareTo(Message that) //sorts by chronology
    {
        if(this.timeStamp.after(that.timeStamp))
            return 1;
        else if(this.timeStamp.before(that.timeStamp))
            return -1;
        else
            return 0;
    }

    public final static Comparator<Date> byReverseChronology() //sort reverse chronologically
    {
        return new Comparator<Date>()
        {
            @Override
            public int compare(Date a, Date b)
            {
                if(a.after(b))
                    return -1;
                else if(a.before(b))
                    return 1;
                else
                    return 0;
            }
        };
    }

    public final static Comparator<Message> byLexicography() //sort alphabetically
    {
        return new Comparator<Message>()
        {
            @Override
            public int compare(Message a, Message b)
            {
                if(a.getEmotionName().compareToIgnoreCase(b.getEmotionName()) < 0)
                    return 1;
                else if(a.getEmotionName().compareToIgnoreCase(b.getEmotionName()) > 0)
                    return -1;
                else
                    return 0;
            }
        };
    }
}
