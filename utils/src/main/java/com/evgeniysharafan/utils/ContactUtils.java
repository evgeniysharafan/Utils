package com.evgeniysharafan.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;

@SuppressWarnings("unused")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class ContactUtils {

    /* Special characters
             * (See "What is a phone number?" doc)
             * 'p' --- GSM pause character, same as comma
             * 'n' --- GSM wild character
             * 'w' --- GSM wait character */
    private static final char PAUSE = ',';
    private static final char WAIT = ';';
    private static final char WILD = 'N';

    // showContactInfoIfExists()
    interface PhoneLookupQuery {
        Uri URI = ContactsContract.PhoneLookup.CONTENT_FILTER_URI;

        String[] PROJECTION = new String[]{
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.LOOKUP_KEY,
        };

        int PHONE_ID = 0;
        int PHONE_LOOKUP = 1;
    }

    // createFrequentLoader() createStarredLoader() createStrequentLoader()
    public interface ContactsQuery {
        Uri CONTENT_FREQUENT_URI = Contacts.CONTENT_FREQUENT_URI;
        Uri CONTENT_URI = Contacts.CONTENT_URI;
        Uri CONTENT_STREQUENT_URI = Contacts.CONTENT_STREQUENT_URI;

        String[] PROJECTION = new String[]{
                Contacts._ID, // ..........................................0
                Contacts.DISPLAY_NAME, // .................................1
                Contacts.STARRED, // ......................................2
                Contacts.PHOTO_URI, // ....................................3
                Contacts.LOOKUP_KEY, // ...................................4
                Contacts.CONTACT_PRESENCE, // .............................5
                Contacts.CONTACT_STATUS, // ...............................6
        };

        String STARRED_SELECTION = Contacts.STARRED + "=?";
        String[] SELECTION_ARGS_0 = new String[]{"0"};
        String[] SELECTION_ARGS_1 = new String[]{"1"};

        String STARRED_ORDER = Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC";

        int CONTACT_ID = 0;
        int DISPLAY_NAME = 1;
        int STARRED = 2;
        int PHOTO_URI = 3;
        int LOOKUP_KEY = 4;
        int CONTACT_PRESENCE = 5;
        int CONTACT_STATUS = 6;
    }

    // createStrequentPhoneOnlyLoader()
    public interface ContactsStrequentQuery {
        Uri CONTENT_STREQUENT_PHONE_ONLY_URI = Contacts.CONTENT_STREQUENT_URI.buildUpon()
                .appendQueryParameter(ContactsContract.STREQUENT_PHONE_ONLY, "true").build();

        /**
         * Projection used for the {@link Contacts#CONTENT_STREQUENT_URI}
         * query when {@link ContactsContract#STREQUENT_PHONE_ONLY} flag
         * is set to true. The main difference is the lack of presence
         * and status data and the addition of phone number and label.
         */
        String[] STREQUENT_PHONE_ONLY_PROJECTION = new String[]{
                Contacts._ID, // ..........................................0
                Contacts.DISPLAY_NAME, // .................................1
                Contacts.STARRED, // ......................................2
                Contacts.PHOTO_URI, // ....................................3
                Contacts.LOOKUP_KEY, // ...................................4
                Phone.NUMBER, // ..........................................5
                Phone.TYPE, // ............................................6
                Phone.LABEL, // ...........................................7
                Phone.IS_SUPER_PRIMARY, //.................................8
                Contacts.PINNED, // .......................................9
                Phone.CONTACT_ID //........................................10
        };

        int CONTACT_ID = 0;
        int DISPLAY_NAME = 1;
        int STARRED = 2;
        int PHOTO_URI = 3;
        int LOOKUP_KEY = 4;
        int PHONE_NUMBER = 5;
        int PHONE_NUMBER_TYPE = 6;
        int PHONE_NUMBER_LABEL = 7;
        int IS_DEFAULT_NUMBER = 8;
        int PINNED = 9;
        // The _ID field returned for strequent items actually contains data._id instead of
        // contacts._id because the query is performed on the data table. In order to obtain the
        // contact id for strequent items, we thus have to use Phone.contact_id instead.
        int CONTACT_ID_FOR_DATA = 10;
    }

    // getContactInfoForPhoneNumber() getContactNameForPhoneNumber()
    public interface ContactInfoQuery {
        /**
         * For a specified phone number, 2 rows were inserted into phone_lookup
         * table. One is the phone number's E164 representation, and another is
         * one's normalized format. If the phone number's normalized format in
         * the lookup table is the suffix of the given number's one, it is
         * treated as matched CallerId. E164 format number must fully equal.
         * <p/>
         * For example: Both 650-123-4567 and +1 (650) 123-4567 will match the
         * normalized number 6501234567 in the phone lookup.
         * <p/>
         * The min_match is used to narrow down the candidates for the final
         * comparison.
         */
        // query params for caller id lookup
        String CALLER_ID_SELECTION = " Data._ID IN "
                + " (SELECT lookup.data_id "
                + " FROM "
                + " (SELECT data_id, normalized_number, length(normalized_number) as len "
                + " FROM phone_lookup "
                + " WHERE min_match = ?) AS lookup)";
        Uri PHONES_WITH_PRESENCE_URI = ContactsContract.Data.CONTENT_URI;

        String[] CALLER_ID_PROJECTION = new String[]{
                Phone._ID,                      // 0
                Phone.NUMBER,                   // 1
                Phone.LABEL,                    // 2
                Phone.DISPLAY_NAME,             // 3
                Phone.CONTACT_ID,               // 4
                Phone.CONTACT_PRESENCE,         // 5
                Phone.CONTACT_STATUS,           // 6
                Utils.hasJellyBean() ? Phone.NORMALIZED_NUMBER : Phone.DATA4, // 7
                Contacts.SEND_TO_VOICEMAIL      // 8
        };

        int PHONE_ID = 0;
        int PHONE_NUMBER_COLUMN = 1;
        int PHONE_LABEL = 2;
        int CONTACT_NAME = 3;
        int CONTACT_ID_COLUMN = 4;
        int CONTACT_PRESENCE_COLUMN = 5;
        int CONTACT_STATUS_COLUMN = 6;
        int PHONE_NORMALIZED_NUMBER = 7;
        int SEND_TO_VOICEMAIL = 8;

        int CONTACT_PHONE_PATTERN_LENGTH = 9;
    }

    // getThumbnailSize()
    private static int sThumbnailSize = -1;

    private ContactUtils() {
    }

    public static boolean canBeCalled(@Nullable String number) {
        String strippedNumber = PhoneNumberUtils.stripSeparators(number);
        return !TextUtils.isEmpty(strippedNumber) && strippedNumber.length() > 2 && strippedNumber.length() < 16;
    }

    /**
     * Strips separators from a phone number string.
     *
     * @param phoneNumber phone number to strip.
     * @return phone string stripped of separators.
     */
    public static String stripSeparators(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            // Character.digit() supports ASCII and Unicode digits (fullwidth, Arabic-Indic, etc.)
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                ret.append(digit);
            } else if (isNonSeparator(c)) {
                ret.append(c);
            }
        }

        return ret.toString();
    }

    /**
     * True if c is ISO-LATIN characters 0-9, *, # , +, WILD, WAIT, PAUSE
     */
    private static boolean isNonSeparator(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == WILD || c == WAIT || c == PAUSE;
    }

    public static boolean showContactInfoIfExists(@NonNull Activity activity, View view, @NonNull String rawPhoneNumber) {
        if (Utils.isEmpty(rawPhoneNumber)) {
            return false;
        }

        boolean exists = false;
        Cursor cursor = Utils.getApp().getContentResolver().query(Uri.withAppendedPath(PhoneLookupQuery.URI,
                rawPhoneNumber), PhoneLookupQuery.PROJECTION, null, null, null);

        if (cursor != null) {
            Uri lookupUri = null;
            try {
                if (cursor.moveToFirst()) {
                    long contactId = cursor.getLong(PhoneLookupQuery.PHONE_ID);
                    String lookupKey = cursor.getString(PhoneLookupQuery.PHONE_LOOKUP);
                    lookupUri = Contacts.getLookupUri(contactId, lookupKey);
                }
            } finally {
                cursor.close();
            }

            if (lookupUri != null) {
                exists = true;
                showContactDetails(activity, view, lookupUri);
            }
        }

        return exists;
    }

    public static void showContactDetails(@NonNull Activity activity, View view, Uri lookupUri) {
        ContactsContract.QuickContact.showQuickContact(activity, view, lookupUri,
                ContactsContract.QuickContact.MODE_LARGE, null);
    }

    public static void launchAddContactActivity(@NonNull Activity activity, @NonNull String phone) {
        Intent addContactIntent = new Intent(ContactsContract.Intents.Insert.ACTION, Contacts.CONTENT_URI);
        addContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
        activity.startActivity(addContactIntent);
    }

    public static CursorLoader createStrequentLoader(Context context) {
        return new CursorLoader(context, ContactsQuery.CONTENT_STREQUENT_URI, ContactsQuery.PROJECTION, null, null,
                ContactsQuery.STARRED_ORDER);
    }

    public static CursorLoader createStarredLoader(Context context) {
        return new CursorLoader(context, ContactsQuery.CONTENT_URI, ContactsQuery.PROJECTION,
                ContactsQuery.STARRED_SELECTION, ContactsQuery.SELECTION_ARGS_1, ContactsQuery.STARRED_ORDER);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static CursorLoader createStrequentPhoneOnlyLoader(Context context) {
        return new CursorLoader(context, ContactsStrequentQuery.CONTENT_STREQUENT_PHONE_ONLY_URI,
                ContactsStrequentQuery.STREQUENT_PHONE_ONLY_PROJECTION, null, null, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static CursorLoader createFrequentLoader(Context context) {
        return new CursorLoader(context, ContactsQuery.CONTENT_FREQUENT_URI, ContactsQuery.PROJECTION,
                ContactsQuery.STARRED_SELECTION, ContactsQuery.SELECTION_ARGS_0, null);
    }

    @Nullable
    public static Cursor getContactInfoForPhoneNumber(String number) {
        L.d("number = " + number);
        Cursor cursor = null;

        String normalizedNumber = PhoneNumberUtils.normalizeNumber(PhoneNumberUtils.stripSeparators(number));
        String minMatch = PhoneNumberUtils.toCallerIDMinMatch(normalizedNumber);
        if (!TextUtils.isEmpty(normalizedNumber) && !TextUtils.isEmpty(minMatch)) {
            cursor = Utils.getApp().getContentResolver().query(ContactInfoQuery.PHONES_WITH_PRESENCE_URI,
                    ContactInfoQuery.CALLER_ID_PROJECTION, ContactInfoQuery.CALLER_ID_SELECTION, new String[]{minMatch}, null);
            if (cursor == null) {
                L.w("queryContactInfoByNumber(" + number + ") returned NULL cursor!"
                        + " contact uri used " + ContactInfoQuery.PHONES_WITH_PRESENCE_URI);
            }
        }

        return cursor;
    }

    public static String getContactNameForPhoneNumber(String number) {
        L.d("number = " + number);
        String name = "";

        Cursor cursor = getContactInfoForPhoneNumber(number);
        if (cursor != null) {
            try {
                String normalizedNumber = PhoneNumberUtils.normalizeNumber(PhoneNumberUtils.stripSeparators(number));
                int startIndex = (normalizedNumber.length() >= ContactInfoQuery.CONTACT_PHONE_PATTERN_LENGTH) ?
                        (normalizedNumber.length() - ContactInfoQuery.CONTACT_PHONE_PATTERN_LENGTH) : 0;

                String phonePattern = normalizedNumber.substring(startIndex);
                while (cursor.moveToNext()) {
                    String phone = PhoneNumberUtils.stripSeparators(cursor.getString(ContactInfoQuery.PHONE_NUMBER_COLUMN));
                    if (phone.endsWith(phonePattern)) {
                        name = cursor.getString(ContactInfoQuery.CONTACT_NAME);
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return name;
    }

    public static int getThumbnailSize(Context context) {
        if (sThumbnailSize == -1) {
            final Cursor c = context.getContentResolver().query(
                    ContactsContract.DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI,
                    new String[]{ContactsContract.DisplayPhoto.THUMBNAIL_MAX_DIM}, null, null, null);

            if (c != null) {
                try {
                    c.moveToFirst();
                    sThumbnailSize = c.getInt(0);
                } finally {
                    c.close();
                }
            }
        }

        return sThumbnailSize;
    }

}
