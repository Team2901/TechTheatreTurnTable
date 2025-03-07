package org.firstinspires.ftc.teamcode;

import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class FileUtilities {
    private static final String TAG = "FileUtilities";
    public final static String TEAM_FOLDER_NAME = "Team";

    public static void writeObjectToFile(String filename, Object object) {
        try {
            final File teamDir = new File(Environment.getExternalStorageDirectory(), TEAM_FOLDER_NAME);
            boolean newDir = teamDir.mkdirs();
            final File file = new File(teamDir, filename);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            writer.write(jsonString);
            writer.flush();
            writer.close();
        } catch (IOException e){
            Log.e(TAG, "Exception writing config file: " + e.getMessage());
            Log.e(TAG, String.join(System.lineSeparator(),
                    Arrays.stream(e.getStackTrace())
                            .map(StackTraceElement::toString)
                            .toArray(String[]::new)));
        }
    }

    public static <T> T  readObjectFromFile(String filename, Class<T> valueType) {
        try {
            final File teamDir = new File(Environment.getExternalStorageDirectory(), TEAM_FOLDER_NAME);
            final File file = new File(teamDir, filename);

            ObjectMapper mapper = new ObjectMapper();
            return (T) mapper.readValue(file, valueType);
        } catch(IOException e) {
            Log.e(TAG, "Exception writing config file: " + e.getMessage());
            Log.e(TAG, String.join(System.lineSeparator(),
                    Arrays.stream(e.getStackTrace())
                            .map(StackTraceElement::toString)
                            .toArray(String[]::new)));
        }
        return null;
    }
}
