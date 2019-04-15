package in.ac.adishankara.alumni.asietalumni;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
{
    private String Date = "";

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int year , month, day;
        if(Date.isEmpty())
        {
            Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }
        else
        {
            String[] arr = Date.split("/");
            year = Integer.parseInt(arr[2]);
            month = Integer.parseInt(arr[1])-1;
            day = Integer.parseInt(arr[0]);
        }
        return new DatePickerDialog(getActivity(),this,year,month,day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day)
    {
        Date = day + "/" + (month+1) + "/" + year;
        //SignUpActivity.setDate(Date);
    }
    public void setDate(String Date)
    {
        this.Date = Date;
    }
}
