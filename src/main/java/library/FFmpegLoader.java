package library;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import utility.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FFmpegLoader {

    private static final String LIB_BIN = "/ffmpeg/";
    private static final String FFMPEG_EXE = "ffmpeg.exe";
    public static final String FFMPEG_DIR = Files.UTILS_DIR.getAbsolutePath() + File.separator + FFMPEG_EXE;
    public static final String FFMPEG_PATH = Files.UTILS_DIR.getAbsolutePath();

    static{
        System.out.println("Checking for ffmpeg.exe");
        if ( !(new File(FFMPEG_DIR).exists())){
            try {
                InputStream in = FFmpegLoader.class.getResourceAsStream(LIB_BIN + FFMPEG_EXE);
                File fileOut = new File(FFMPEG_DIR);
                OutputStream out = FileUtils.openOutputStream(fileOut);
                IOUtils.copy(in,out);
                in.close();
                out.close();
                System.out.println(FFMPEG_EXE + " has been unpacked in " + FFMPEG_DIR);
            } catch (IOException e) {
                System.out.println("Cannot put " + FFMPEG_EXE + " in " + FFMPEG_DIR);
            }
        } else {
            System.out.println("ffmpeg.exe has already existed in " + FFMPEG_DIR);
        }
    }
}
