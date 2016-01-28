package arashincleric.com.econsquarestudy;

import android.provider.BaseColumns;

/**
 * Created by Dan on 1/27/2016.
 */
public final class UsernameContract {
    public UsernameContract(){}

    /* Inner class that defines the table contents */
    public static abstract class Usernames implements BaseColumns {
        public static final String TABLE_NAME = "UserID";
        public static final String COLUMN_NAME_ENTRY_ID = "id";
    }
}
