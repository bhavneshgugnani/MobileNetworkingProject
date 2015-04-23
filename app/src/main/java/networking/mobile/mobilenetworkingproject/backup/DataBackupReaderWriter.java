package networking.mobile.mobilenetworkingproject.backup;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import networking.mobile.mobilenetworkingproject.constant.Constants;

/**
 * Created by Bhavnesh Gugnani on 4/7/2015.
 */
public class DataBackupReaderWriter {

    public static String[] readBackupFileNames(Context context) {
        String[] fileNames = null;
        File folder = new File(context.getFilesDir() + Constants.BACKUP_PATH);
        try {
            if (!folder.exists()) {
                CharSequence message = "Backup Folder does not exists";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            } else {
                File[] files = folder.listFiles();
                if (files != null && files.length > 0) {
                    fileNames = new String[files.length];
                    for (int i = 0; i < files.length; i++) {
                        fileNames[i] = files[i].getName();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            CharSequence message = e.getMessage();
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } finally {
            return fileNames;
        }
    }

    public static String readBackupFileData(Context context, String fileName) throws Exception {
        File file = new File(context.getFilesDir() + Constants.BACKUP_PATH, fileName);
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream inStream = new FileInputStream(file);
        try {
            if (!file.exists())
                throw new Exception("No file present in backup");

            inStream.read(bytes);
        } catch (IOException e) {
            throw new Exception("Unable to read backup file data from memory file.");
        } finally {
            inStream.close();
        }
        return new String(bytes);
    }

    public static void writeNetworkBackupToLocalMemory(Context context, String deviceName, String text) {
        File file = new File(context.getFilesDir() + Constants.BACKUP_PATH, deviceName + ".txt");
        FileOutputStream outputStream = null;
        try {
            /*if (file.exists()) {
                //trash old data
                file.delete();
            } else {
                //file.mkdirs();
                file.createNewFile();
            }*/
            outputStream = new FileOutputStream(file, false);//overwrite existing file
            outputStream.write(text.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            CharSequence message = e.getMessage();
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteNetworkBackup(Context context) {
        File folder = new File(context.getFilesDir() + Constants.BACKUP_PATH);
        try {
            if (folder.exists()) {
                File[] files = folder.listFiles();
                if (files != null && files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        files[i].delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
