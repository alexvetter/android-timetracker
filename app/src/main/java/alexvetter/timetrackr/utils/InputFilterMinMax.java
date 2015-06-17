package alexvetter.timetrackr.utils;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax implements InputFilter {

    private int min, max;

    public InputFilterMinMax(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException("max should be greater than min");
        }

        this.min = min;
        this.max = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String input = dest.toString() + source.toString();

            int number = Integer.parseInt(input);
            if (isInRange(number)) {
                return null;
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(nfe);
        }

        return "";
    }

    private boolean isInRange(int c) {
        return c >= min && c <= max;
    }
}