package utility;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Files {

    private static final String TMP_DIR = System.getProperty("user.dir") + File.separator + "tmp";
    public static final String UTILS_PATH = System.getProperty("user.dir")+ File.separator + "utils";
    public static File UTILS_DIR;

    public static void cleanAfterQuit() {

        File files[] = new File(System.getProperty("user.dir")).listFiles();
        for (File f : files) {

            if (f.getName().toLowerCase().startsWith("tmp")) {
                try {
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String createTmpFolder(){

        File file = new File(TMP_DIR);
        if ( file.mkdir()){
            return file.getAbsolutePath();
        }

        return null;
    }

    public static void cleanTmpFolder(){

        try {
            FileUtils.deleteDirectory(new File(TMP_DIR));
        } catch (IOException e) {
            System.out.println("Temporary folder doesn't exist");
        }
    }

    public static void createUtilsFolder(){

        if(!(new File(UTILS_PATH).exists())){
            Path utilsDir = null;
            try {
                utilsDir = java.nio.file.Files.createDirectory(Paths.get(UTILS_PATH));
                java.nio.file.Files.setAttribute(Paths.get(UTILS_PATH),"dos:hidden",true);
                System.out.println("Utils folder has been created");
            } catch (IOException e) {
                e.printStackTrace();
            }
            UTILS_DIR = new File(utilsDir.toString());
        } else {
            UTILS_DIR = new File(UTILS_PATH);
        }
    }

    public static int extractNumber(String name) {
        int i = 0;
        try {
            int s = name.indexOf('_') + 1;
            int e = name.lastIndexOf('.');
            String number = name.substring(s, e);
            i = Integer.parseInt(number);
        } catch(Exception e) {
            i = 0; // if filename does not match the format
            // then default to 0
        }
        return i;
    }
}
