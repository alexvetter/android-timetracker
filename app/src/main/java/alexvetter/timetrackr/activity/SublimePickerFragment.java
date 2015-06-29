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

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import alexvetter.timetrackr.R;

public class SublimePickerFragment extends DialogFragment {

    /**
     * Callback to activity
     */
    private Callback callback;

    private SublimeListenerAdapter listener = new SublimeListenerAdapter() {
        @Override
        public void onCancelled() {
            if (callback != null) {
                callback.onCancelled();
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
            if (callback != null) {
                callback.onSet(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour);
            }

            // Should actually be called by activity inside `Callback.onCancelled()`
            dismiss();
        }
    };

    public SublimePickerFragment() {
        super();
    }

    /**
     * Set activity callback, which is also the provider for SublimeOptions
     */
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * Creates and initializes the SublimePicker
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SublimePicker picker = (SublimePicker) getActivity().getLayoutInflater().inflate(R.layout.sublime_picker, container);

        SublimeOptions options = null;
        if (callback != null) {
            options = callback.getOptions();
        }

        picker.initializePicker(options, listener);
        return picker;
    }

    /**
     * Callback interface for interacting with activity
     */
    static abstract class Callback {
        void onCancelled() {
            // ignore
        }

        abstract void onSet(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour);

        abstract SublimeOptions getOptions();
    }

    /**
     * Callback interface for interacting with activity
     */
    static abstract class DateCallback extends Callback {
        @Override
        void onSet(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour) {
            onSet(new LocalDate(year, monthOfYear + 1, dayOfMonth));
        }

        abstract void onSet(LocalDate date);
    }

    /**
     * Callback interface for interacting with activity
     */
    static abstract class TimeCallback extends Callback {
        @Override
        void onSet(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour) {
            onSet(new LocalTime(hourOfDay, minuteOfHour));
        }

        abstract void onSet(LocalTime time);
    }
}
