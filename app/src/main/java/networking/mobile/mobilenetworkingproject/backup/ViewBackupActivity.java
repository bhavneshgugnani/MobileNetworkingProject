package networking.mobile.mobilenetworkingproject.backup;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import networking.mobile.mobilenetworkingproject.R;

public class ViewBackupActivity extends ActionBarActivity {
    private static final int VIEW_FILE_DATA_REQUEST_CODE = 500;

    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_backup);

        createView();
    }

    private void createView() {
        //read file names from backup directory
        final String[] fileNames = DataBackupReaderWriter.readBackupFileNames(getApplicationContext());
        listView = (ListView) findViewById(R.id.backup_files);

        if(fileNames != null && fileNames.length > 0) {
            BackupFilenameAdapter adapter = new BackupFilenameAdapter(this, fileNames);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String fileName = fileNames[position];

                    CharSequence text = fileName;
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ViewBackupActivity.this, ViewBackupFileDataActivity.class);
                    intent.putExtra("filename", fileName);
                    startActivityForResult(intent, VIEW_FILE_DATA_REQUEST_CODE);
                }
            });
        }





        /*LinearLayout linearLayout = (LinearLayout) findViewById(R.id.file_names);
        //clear older views
        if ((linearLayout).getChildCount() > 0)
            linearLayout.removeAllViews();
        //add new views
        TextView view;
        if (fileNames != null && fileNames.length > 0) {
            for (String name : fileNames) {
                view = new TextView(this);
                view.setText(name);
                linearLayout.addView(view);
            }
        } else {
            view = new TextView(this);
            view.setText(getString(R.string.no_backup_files_found));
            linearLayout.addView(view);
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_backup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.title_delete_backup) {
            DataBackupReaderWriter.deleteNetworkBackup(getApplicationContext());
            setResult(RESULT_OK);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Adding angels
        if(requestCode == VIEW_FILE_DATA_REQUEST_CODE){
            if(resultCode == RESULT_CANCELED){
                //Adding angels failed due to some problem
            } else if(requestCode == RESULT_OK){
                //Adding angels successful
            }
        }
    }
}
