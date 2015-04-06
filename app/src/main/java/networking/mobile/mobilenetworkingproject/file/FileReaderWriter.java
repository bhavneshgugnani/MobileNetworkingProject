package networking.mobile.mobilenetworkingproject.file;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Bhavnesh Gugnani on 4/5/2015.
 */
public class FileReaderWriter {
    public static final String STORAGE_FILENAME = "data_file.txt";

    public static String readFileFromMemory(Context context, FileInputStream inputStream) {
        String angelListData = "";
        File file = new File(context.getFilesDir(), STORAGE_FILENAME);
        try {
            if (!file.exists()){
                CharSequence message = "File does not exists";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
            int data = inputStream.read();
            while (data != -1) {
                char theChar = (char) data;
                angelListData += theChar;
                data = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return angelListData;
    }

    public static void writeFileToMemory(Context context, FileOutputStream outputStream, String text) {
        File file = new File(context.getFilesDir(), STORAGE_FILENAME);
        try {
            if (!file.exists())
                file.createNewFile();
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
            outputWriter.write(text);
            outputWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            CharSequence message = e.getMessage();
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
