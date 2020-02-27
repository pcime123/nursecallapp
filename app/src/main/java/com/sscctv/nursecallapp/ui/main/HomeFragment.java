package com.sscctv.nursecallapp.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateLongClickListener;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.sscctv.nursecallapp.MainActivity;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.service.MainCallService;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;

public class HomeFragment extends Fragment  implements OnDateSelectedListener, OnMonthChangedListener, OnDateLongClickListener {

    private static final String TAG = "HomeFragment";
    MainActivity activity;
    MaterialCalendarView widget;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);
        widget = view.findViewById(R.id.calendarView);

        widget.setOnDateChangedListener(this);
        widget.setOnDateLongClickListener(this);
        widget.setOnMonthChangedListener(this);

        return view;
    }

    @Override
    public void onDateLongClick(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date) {
//        final String text = String.format("%s is available", formatter.format(date));
        Log.d(TAG, "onDateLongClick: " + date);
//        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
//        textView.setText(selected ? formatter.format(date) : "No Selection");
        Log.d(TAG, "onDateSelected: " + date + " Selected: " + selected);
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
//        getSupportActionBar().setTitle(FORMATTER.format(date.getDate()));
        Log.d(TAG, "onMonthChanged: " + date);
    }
}
