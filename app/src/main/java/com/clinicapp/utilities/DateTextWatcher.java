package com.clinicapp.utilities;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.Calendar;

public class DateTextWatcher implements TextWatcher {
    private String current = "";
    private String ddmmyyyy = "MMDDYYYY";
    private Calendar cal = Calendar.getInstance();

    final EditText editText;
    private DateTextWatcher.DateTextChangeListener listener;

    public DateTextWatcher(EditText editText, DateTextWatcher.DateTextChangeListener listener){
        this.editText = editText;
        this.listener = listener;
        editText.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!s.toString().equals(current)) {
            String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
            String cleanC = current.replaceAll("[^\\d.]|\\.", "");

            int cl = clean.length();
            int sel = cl;
            for (int i = 2; i <= cl && i < 6; i += 2) {
                sel++;
            }
            //Fix for pressing delete next to a forward slash
            if (clean.equals(cleanC)) sel--;

            if (clean.length() < 8){
//                if(clean.length()==1 && Integer.parseInt(clean)<10)
//                {
//                    clean = "0"+ clean;
//                }
                clean = clean + ddmmyyyy.substring(clean.length());
            }else{
                //This part makes sure that when we finish entering numbers
                //the date is correct, fixing it otherwise
                int mon  = Integer.parseInt(clean.substring(0,2));
                int  day = Integer.parseInt(clean.substring(2,4));
                int year = Integer.parseInt(clean.substring(4,8));

                mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                cal.set(Calendar.MONTH, mon-1);
                year = (year<1900)?1900:(year> Calendar.getInstance().get(Calendar.YEAR))?
                        Calendar.getInstance().get(Calendar.YEAR) :year;
                cal.set(Calendar.YEAR, year);
                // ^ first set year for the line below to work correctly
                //with leap years - otherwise, date e.g. 29/02/2012
                //would be automatically corrected to 28/02/2012

                day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                clean = String.format("%02d%02d%02d",mon, day, year);

                this.listener.onChange(s.toString());
            }

            clean = String.format("%s/%s/%s", clean.substring(0, 2),
                    clean.substring(2, 4),
                    clean.substring(4, 8));

            sel = sel < 0 ? 0 : sel;
            current = clean;
            editText.setText(current);
            editText.setSelection(sel < current.length() ? sel : current.length());


        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public static interface DateTextChangeListener {
        void onChange(String value);
    }
}
