package com.expresshue.smstest;

public class ContactsSchema
{
    public static final class ContactsTable
    {
        public static final String NAME = "Contacts";

        public static final class Cols
        {
            public static final String MOBILE_NUMBER = "mobile_number";
            public static final String FIRST_NAME = "first_name";
            public static final String LAST_NAME = "last_name";
            public static final String EMOTION = "emotion";
            public static final String COLOR = "color";
            public static final String POLARITY = "polarity";
            public static final String ANGRY_SEND = "angry_send";
            public static final String SAD_SEND = "sad_send";
            public static final String FEAR_SEND = "fear_send";
            public static final String HAPPY_SEND = "happy_send";
            public static final String MAYBE_SEND = "maybe_send";
            public static final String HUG_SEND = "hug_send";
            public static final String ANGRY_RECEIVE = "angry_receive";
            public static final String SAD_RECEIVE = "sad_receive";
            public static final String FEAR_RECEIVE = "fear_receive";
            public static final String HAPPY_RECEIVE = "happy_receive";
            public static final String MAYBE_RECEIVE = "maybe_receive";
            public static final String HUG_RECEIVE = "hug_receive";

        }
    }
}
