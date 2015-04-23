package networking.mobile.mobilenetworkingproject.backup;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import networking.mobile.mobilenetworkingproject.R;

/**
 * Created by Bhavnesh Gugnani on 4/17/2015.
 */
public class BackupFilenameAdapter extends ArrayAdapter {
    private String[] fileNames = null;
    private Context context = null;

    public BackupFilenameAdapter(Context context, String[] fileNames) {
        super(context, R.layout.backup_file_names_row, fileNames);
        this.context = context;
        this.fileNames = fileNames;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.backup_file_names_row, parent, false);
        TextView name = (TextView) convertView.findViewById(R.id.textView1);
        name.setText(fileNames[position]);
        return convertView;
    }
}
