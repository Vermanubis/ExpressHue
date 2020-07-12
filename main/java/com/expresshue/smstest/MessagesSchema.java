package com.expresshue.smstest;

public class MessagesSchema
{
    public static final class MessagesTable
    {
        public static final String NAME = "Messages";

        public static final class Cols
        {
            public static final String MOBILE_NUMBER = "mobile_number";
            public static final String MESSAGE = "message";
            public static final String OWNER = "owner";
            public static final String EMOTION = "emotion";
            public static final String POLARITY = "polarity";
            public static final String DATE = "date";
        }
    }
}
