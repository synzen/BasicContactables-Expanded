/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.basiccontactables;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.support.v4.app.ActivityCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Helper class to handle all the callbacks that occur when interacting with loaders.  Most of the
 * interesting code in this sample app will be in this file.
 */

public class ContactablesLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    Context mContext;
    public static final String QUERY_KEY = "query";
    public static final String TAG = "ContactablesCallbacks";
    static final int expandImages[] = new int[]{ R.drawable.ic_expand, R.drawable.ic_expand_less};
    static final LinearLayout.LayoutParams itemWrapperLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    static final LinearLayout.LayoutParams itemMainSummaryLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
    static final LinearLayout.LayoutParams expandButtonLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    static final LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 7);
    static final LinearLayout.LayoutParams callButtonLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);

    static final LinearLayout.LayoutParams itemDropdownTableLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    static final LinearLayout.LayoutParams itemDropdownTableRowLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    static final LinearLayout.LayoutParams itemDropdownTableCellKeyLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3);
    static final LinearLayout.LayoutParams itemDropdownTableCellValueLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 7);
    static final LinearLayout.LayoutParams itemDropdownTableCellButtonLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);

    static {
        itemDropdownTableCellKeyLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        itemDropdownTableCellValueLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        itemDropdownTableCellButtonLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        itemWrapperLayoutParams.setMargins(0, 0, 0, 30);
    }

    public ContactablesLoaderCallbacks(Context context) {
        mContext = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderIndex, Bundle args) {
        String query = args.getString(QUERY_KEY);
        ContentResolver cr = mContext.getContentResolver();
//        String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME };
        Uri contentUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(query));
        Uri fullContentUri = ContactsContract.Contacts.CONTENT_URI;
        return new CursorLoader(
                mContext, // Context
                query.isEmpty() ? fullContentUri : contentUri, // Search this
                null, // Projection
                null, // Selection
                null, // Selection args
                CommonDataKinds.Phone.DISPLAY_NAME + " ASC" // Sort Order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        LinearLayout linearLayout = ((Activity) mContext).findViewById(R.id.linearLayout);
        linearLayout.removeAllViews();
        if (mContext == null) Log.e(TAG, "Context is null?");
        else Log.e(TAG, "Nothing is null?!");

        // For each search result
        LinearLayout itemWrapper;
        LinearLayout itemSummary;
        TextView tv;
        LinearLayout row;
        ImageButton expand;
        ContentResolver cr = mContext.getContentResolver();

        if (cursor.getCount() == 0) {
            tv = new TextView(mContext);
            tv.setText(mContext.getText(R.string.no_search_results));
            tv.setGravity(Gravity.CENTER);
            linearLayout.addView(tv);
            return;
        }

        // Reset text in case of a previous query
        TextView desc = new TextView(mContext);
        desc.setText(mContext.getText(R.string.intro_message) + "\n\n");
        linearLayout.addView(desc);

        while (cursor.moveToNext()) {
            boolean hasExtraData = false;
            itemWrapper = new LinearLayout(mContext);
            itemWrapper.setOrientation(LinearLayout.VERTICAL);
            itemSummary = new LinearLayout(mContext);
            itemSummary.setOrientation(LinearLayout.HORIZONTAL);
            itemSummary.setWeightSum(10);

            final LinearLayout expansionTable = new LinearLayout(mContext);
            expansionTable.setBackgroundColor(mContext.getColor(R.color.itemSummaryBackground));
            expansionTable.setOrientation(LinearLayout.VERTICAL);
            expansionTable.setVisibility(View.GONE);

            row = new LinearLayout(mContext);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(12);

            Map<String, String> itemData = new HashMap<String, String>();
            ((Activity) mContext).registerForContextMenu(itemSummary);
            itemSummary.setBackgroundColor(mContext.getColor(R.color.itemSummaryBackground));
            itemSummary.setPadding(20, 20, 20, 20);

            tv = new TextView(mContext);
            tv.setPadding(45, 0, 0, 0);
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
            final String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            SpannableStringBuilder name = new SpannableStringBuilder(displayName + "\n");
            name.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, displayName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.append(name);

            // PHONE NUMBERS
            Cursor phoneCur = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                    null,
                    null);
            while (phoneCur.moveToNext()) {
                String number = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int type = phoneCur.getInt(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                switch (type) {
                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME: {
                        tv.append("Home Number: " + number + "\n");
                        itemData.put("phoneHome", number);
                        break;
                    }
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE: {
                        tv.append("Mobile Number: " + number + "\n");
                        itemData.put("phoneMobile", number);
                        break;
                    }
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK: {
                        tv.append("Work Number: " + number + "\n");
                        itemData.put("phoneWork", number);
                        break;
                    }
                    default: {
                        tv.append("Phone Number: " + number + "\n");
                        itemData.put("phonePhone", number);
                    }
                }
            }
            phoneCur.close();

            // EMAILS
            Cursor emailsCur = cr.query(CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            while (emailsCur.moveToNext()) {
                row = new LinearLayout(mContext);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setWeightSum(12);
                final String address = emailsCur.getString(emailsCur.getColumnIndex(CommonDataKinds.Email.DATA));
                TextView labelView = new TextView(mContext);
                labelView.setPadding(20, 10, 20, 10);
                labelView.setText("Email");

                TextView addressView = new TextView(mContext);
                addressView.setPadding(20, 10, 20, 10);
                addressView.setText(address);

                ImageButton emailButton = new ImageButton(mContext);
                emailButton.setImageResource(R.drawable.ic_email);
                emailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContext.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null)), "Email by:"));
                    }
                });

                row.addView(labelView, itemDropdownTableCellKeyLayoutParams);
                row.addView(addressView, itemDropdownTableCellValueLayoutParams);
                row.addView(emailButton, itemDropdownTableCellButtonLayoutParams);

                expansionTable.addView(row, itemDropdownTableRowLayoutParams);
                itemData.put("email", address);
                hasExtraData = true;
            }

            // BIRTHDAY
            String where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY + " and " +
                    ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and " + ContactsContract.Data.CONTACT_ID + " = " + contactId;
            Cursor birthdayCur = cr.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    where,
                    null,
                    null);
            while (birthdayCur.moveToNext()) {
                String bday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                String beautifiedBday = "";
                Date date = new Date();

                try {
                    date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(bday);
                    beautifiedBday = new SimpleDateFormat("MMM d, yyyy").format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                final Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 8);
                cal.set(Calendar.MINUTE, 0);

                row = new LinearLayout(mContext);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setWeightSum(12);
                itemData.put("birthday", bday);


                TextView labelView = new TextView(mContext);
                labelView.setPadding(20, 10, 20, 10);
                labelView.setText("Birthday");

                TextView bdayView = new TextView(mContext);
                bdayView.setPadding(20, 10, 20, 10);
                bdayView.setText(beautifiedBday.isEmpty() ? bday : beautifiedBday);

                ImageButton alarmButton = new ImageButton(mContext);
                alarmButton.setImageResource(R.drawable.ic_alarm);
                alarmButton.setOnClickListener(v -> {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, 2);
                        return;
                    }
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra("title", "Birthday for " + displayName);
                    intent.putExtra("description", "Celebrate!");
                    intent.putExtra("beginTime", cal.getTime().getTime());
                    intent.putExtra("endTime", cal.getTime().getTime());
                    mContext.startActivity(intent);
                });

                row.addView(labelView, itemDropdownTableCellKeyLayoutParams);
                row.addView(bdayView, itemDropdownTableCellValueLayoutParams);
                row.addView(alarmButton, itemDropdownTableCellButtonLayoutParams);

                expansionTable.addView(row, itemDropdownTableRowLayoutParams);
                hasExtraData = true;
            }
            birthdayCur.close();

            // ADDRESS
            Cursor addressCur = cr.query(
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + contactId,
                    null,
                    null);
            while (addressCur.moveToNext()) {
                final String street = addressCur.getString(addressCur.getColumnIndex(CommonDataKinds.StructuredPostal.STREET));
                row = new LinearLayout(mContext);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setWeightSum(12);
                itemData.put("address", street);

                TextView labelView = new TextView(mContext);
                labelView.setPadding(20, 10, 20, 10);
                labelView.setText("Address");

                TextView addressView = new TextView(mContext);
                addressView.setPadding(20, 10, 20, 10);
                addressView.setText(street);

                ImageButton navButton = new ImageButton(mContext);
                navButton.setImageResource(R.drawable.ic_directions);
                navButton.setOnClickListener(v -> mContext.startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + street))));

                row.addView(labelView, itemDropdownTableCellKeyLayoutParams);
                row.addView(addressView, itemDropdownTableCellValueLayoutParams);
                row.addView(navButton, itemDropdownTableCellButtonLayoutParams);

                expansionTable.addView(row, itemDropdownTableRowLayoutParams);
                hasExtraData = true;
            }
            addressCur.close();

            // Add the call button
            Log.d(TAG, displayName);
            ImageButton call = new ImageButton(mContext);
            call.setImageResource(R.drawable.ic_phone);
            call.setBackgroundColor(mContext.getColor(R.color.callButton));
            final String phoneNumbers[] = new String[4];
            final String phoneNumberTextsTemp[] = new String[4];
            int phoneCount = 0;
            for (String type : itemData.keySet()) {
                String data = itemData.get(type);
                switch (type) {
                    case "phoneMobile":
                        phoneNumbers[phoneCount] = data;
                        phoneNumberTextsTemp[phoneCount] = "Mobile: " + data;
                        Log.d(TAG, "Setting mobile at index " + Integer.toString(phoneCount) + " to " + data);
                        phoneCount++;
                        break;
                    case "phoneWork":
                        phoneNumbers[phoneCount] = data;
                        phoneNumberTextsTemp[phoneCount] = "Work: " + data;
                        Log.d(TAG, "Setting work at index " + Integer.toString(phoneCount) + " to " + data);
                        phoneCount++;
                        break;
                    case "phoneHome":
                        phoneNumbers[phoneCount] = data;
                        phoneNumberTextsTemp[phoneCount] = "Home: " + data;
                        phoneCount++;
                        break;
                    case "phonePhone":
                        phoneNumbers[phoneCount] = data;
                        phoneNumberTextsTemp[phoneCount] = "Phone: " + data;
                        phoneCount++;
                        break;
                }
            }
            final String phoneNumberTexts[] = Arrays.stream(phoneNumberTextsTemp).filter(s -> s != null && s.length() > 0).toArray(String[]::new);
            call.setOnClickListener(v -> {
                // Ask for permission if it doesn't have permission
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CALL_PHONE}, 2);
                    return;
                }
                // If there's only one phone, automatically start the call
                if (phoneNumberTexts.length == 1) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phoneNumbers[0]));
                    mContext.startActivity(intent);
                    return;
                }
                // Select which phone to call
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Call");
                builder.setItems(phoneNumberTexts, (dialog, index) -> {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phoneNumbers[index]));
                    mContext.startActivity(intent);
                });
                builder.show();
            });
            itemSummary.addView(call, callButtonLayoutParams);

            // Add the main text
            itemSummary.addView(tv, textViewLayoutParams);
            itemSummary.setTag(itemData);

            // Add the expand button, and the expand items
            if (hasExtraData) {
                expand = new ImageButton(mContext);
                expand.setPadding(0, 0, 10, 0);
                expand.setBackground(null);
                expand.setTag(0); // Indicates the index for the resource image to use in clicks
                expand.setImageResource(R.drawable.ic_expand);
                expand.setOnClickListener(view -> {
                    ImageButton b = (ImageButton) view;
                    int newTag = (int) b.getTag() == 0 ? 1 : 0;
                    b.setTag(newTag);
                    b.setImageResource(expandImages[newTag]);
                    expansionTable.setVisibility(expansionTable.isShown() ? View.GONE : View.VISIBLE);
                });
                itemSummary.addView(expand, expandButtonLayoutParams);
            }
            itemWrapper.addView(itemSummary, itemMainSummaryLayoutParams);
            itemWrapper.addView(expansionTable, itemDropdownTableLayoutParams);
            linearLayout.addView(itemWrapper, itemWrapperLayoutParams);

        }
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}
