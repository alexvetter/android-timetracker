/*
 * Copyright 2015 Vikram Kakkar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alexvetter.timetrackr.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appeaser.sublimepickerlibrary.SublimePicker;
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

import alexvetter.timetrackr.R;

public class SublimePickerFragment extends DialogFragment {
    /**
     * Date & Time formatter used for formatting
     * text on the switcher button
     */
    DateFormat mDateFormatter, mTimeFormatter;

    /**
     * Picker
     */
    SublimePicker mSublimePicker;

    /**
     * Callback to activity
     */
    Callback mCallback;

    SublimeListenerAdapter mListener = new SublimeListenerAdapter() {
        @Override
        public void onCancelled() {
            if (mCallback != null) {
                mCallback.onCancelled();
            }

            // Should actually be called by activity inside `Callback.onCancelled()`
            dismiss();
        }

        @Override
        public void onDateTimeRecurrenceSet(SublimePicker sublimePicker,
                                            int year, int monthOfYear, int dayOfMonth,
                                            int hourOfDay, int minuteOfHour,
                                            SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                            String recurrenceRule) {
            if (mCallback != null) {
                mCallback.onSet(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour);
            }

            // Should actually be called by activity inside `Callback.onCancelled()`
            dismiss();
        }
    };

    /**
     * Initialize picker and its formatters
     */
    public SublimePickerFragment() {
        mDateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        mTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

        mTimeFormatter.setTimeZone(TimeZone.getDefault());
    }

    /**
     * Set activity callback
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSublimePicker = (SublimePicker) getActivity().getLayoutInflater().inflate(R.layout.sublime_picker, container);

        mSublimePicker.initializePicker(mCallback.getOptions(), mListener);
        return mSublimePicker;
    }

    /**
     * Callback interface for interacting with activity
     */
    public static abstract class Callback {
        void onCancelled() {
            // ignore
        }

        abstract void onSet(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour);

        abstract SublimeOptions getOptions();
    }

    public static abstract class DateCallback extends Callback {
        @Override
        void onSet(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour) {
            onSet(new LocalDate(year, monthOfYear, dayOfMonth));
        }

        abstract void onSet(LocalDate date);
    }

    public static abstract class TimeCallback extends Callback {
        @Override
        void onSet(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour) {
            onSet(new LocalTime(hourOfDay, minuteOfHour));
        }

        abstract void onSet(LocalTime time);
    }
}
