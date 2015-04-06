package networking.mobile.mobilenetworkingproject.file;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import networking.mobile.mobilenetworkingproject.R;

public class EditFileActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file_activity);

        EditText editText = (EditText) findViewById(R.id.file_text);
        String fileText = "";
        try {
            fileText += FileReaderWriter.readFileFromMemory(getApplicationContext(), openFileInput(FileReaderWriter.STORAGE_FILENAME));
        } catch(Exception e){
            e.printStackTrace();
        }
        editText.setText(fileText);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_file_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save_changes_to_file) {
            saveChangesToFile();
        } else if(id == R.id.cancel_changes_to_file){
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChangesToFile(){
        EditText editText = (EditText) findViewById(R.id.file_text);
        String newText = editText.getText().toString();
        try {
            FileReaderWriter.writeFileToMemory(getApplicationContext(), openFileOutput(FileReaderWriter.STORAGE_FILENAME, Context.MODE_PRIVATE), newText);
        } catch(IOException e){
            e.printStackTrace();
            CharSequence message = "Failure!Could not write text to file";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }finally {
            setResult(RESULT_OK);
            finish();
        }
    }
}
