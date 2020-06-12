/*
 * Copyright (c) 2010-2019 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sscctv.nursecallapp.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.sscctv.nursecallapp.R;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallLog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CallListAdapter1 extends SelectableAdapter<CallListViewHolder> {
    private final List<CallLog> mLogs;
//    private final HistoryActivity context;
    private final CallListViewHolder.ClickListener mClickListener;
    private Context context;

    public CallListAdapter1(Context context, List<CallLog> logs, CallListViewHolder.ClickListener listener, SelectableHelper helper) {
        super(helper);
        mLogs = logs;
        this.context = context;
        mClickListener = listener;
    }

    public Object getItem(int position) {
        return mLogs.get(position);
    }

    @NonNull
    @Override
    public CallListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_history, parent, false);
        return new CallListViewHolder(v, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final CallListViewHolder holder, final int position) {
        CallLog log = mLogs.get(position);
        long timestamp = log.getStartDate() * 1000;
        final Address address;

        String callTime = secondsToDisplayableString(log.getDuration());
        String callDate = String.valueOf(log.getStartDate());
        long longDate = Long.parseLong(callDate);
        holder.time.setText("Time: " + callTime);
        holder.date.setText("Date: " + _timestampToHumanDate(context, longDate, context.getString(R.string.history_detail_date_format)));
//        Log.d("Jin","CallTime: " + callTime + " CallDate: " + _timestampToHumanDate(context, longDate, context.getString(R.string.history_detail_date_format)));
        holder.contact.setSelected(true); // For automated horizontal scrolling of long texts
        Calendar logTime = Calendar.getInstance();
        logTime.setTimeInMillis(timestamp);
        holder.separatorText.setText(timestampToHumanDate(logTime));
        holder.select.setVisibility(isEditionEnabled() ? View.VISIBLE : View.GONE);
        holder.select.setChecked(isSelected(position));

        if (position > 0) {
            CallLog previousLog = mLogs.get(position - 1);
            long previousTimestamp = previousLog.getStartDate() * 1000;
            Calendar previousLogTime = Calendar.getInstance();
            previousLogTime.setTimeInMillis(previousTimestamp);

            if (isSameDay(previousLogTime, logTime)) {
                holder.separator.setVisibility(View.GONE);
            } else {
                holder.separator.setVisibility(View.VISIBLE);
            }
        } else {
            holder.separator.setVisibility(View.VISIBLE);
        }

        Log.e("Jin", "getStatus: " + log.getStatus());
        if (log.getDir() == Call.Dir.Incoming) {
            address = log.getFromAddress();
            if (log.getStatus() == Call.Status.Missed) {
                holder.callDirection.setImageResource(R.drawable.call_status_missed);
            } else {
                holder.callDirection.setImageResource(R.drawable.call_status_incoming);
            }
        } else {
            address = log.getToAddress();
            holder.callDirection.setImageResource(R.drawable.call_status_outgoing);
        }



        String displayName = null;
        String sipUri = (address != null) ? address.asString() : "";

        if (address != null) {
            displayName = address.getDisplayName();
        }
        if (displayName == null) {
            assert address != null;
            holder.contact.setText(address.getUsername());
        } else {
            holder.contact.setText(displayName);
        }

//        holder.detail.setVisibility(isEditionEnabled() ? View.INVISIBLE : View.VISIBLE);
//        holder.detail.setOnClickListener(
//                !isEditionEnabled()
//                        ? (View.OnClickListener) v -> {
//                            DialogCallListDetail dialog = new DialogCallListDetail(context);
//                            dialog.init(address);
//                            dialog.show();
//                        }
//                        : null);
    }

    @Override
    public int getItemCount() {
        return mLogs.size();
    }

    @SuppressLint("SimpleDateFormat")
    private String timestampToHumanDate(Calendar cal) {
        SimpleDateFormat dateFormat;
        if (isToday(cal)) {
            return "Today";
        } else if (isYesterday(cal)) {
            return "Yesterday";
        } else {
            dateFormat =
                    new SimpleDateFormat(
                            "EEE d MMM");
        }

        return dateFormat.format(cal.getTime());
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            return false;
        }

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    private boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }

    private boolean isYesterday(Calendar cal) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.roll(Calendar.DAY_OF_MONTH, -1);
        return isSameDay(cal, yesterday);
    }

    @SuppressLint("SimpleDateFormat")
    private String secondsToDisplayableString(int secs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, 0, 0, secs);
        return dateFormat.format(cal.getTime());
    }

    public static String _timestampToHumanDate(Context context, long timestamp, String format) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp * 1000); // Core returns timestamps in seconds...

            SimpleDateFormat dateFormat;
//            if (_isToday(cal)) {
//                dateFormat =
//                        new SimpleDateFormat(
//                                context.getResources().getString(R.string.today_date_format),
//                                Locale.getDefault());
//            } else {
                dateFormat = new SimpleDateFormat(format, Locale.getDefault());
//            }

            return dateFormat.format(cal.getTime());
        } catch (NumberFormatException nfe) {
            return String.valueOf(timestamp);
        }
    }

    private static boolean _isToday(Calendar cal) {
        return _isSameDay(cal, Calendar.getInstance());
    }

    private static boolean _isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            return false;
        }

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
}
