package library;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Tess4jAssetsLoader {

    private static final String WORK_DIR = System.getProperty("user.dir");
    private static final String TESS4J_ASSETS = "/tessdata/";
    private static final String CONFIG_ASSETS = "/tessdata/configs/";
    private static final String TESSDATA_DIR = WORK_DIR + File.separator + "tessdata";
    private static final String CONFIG_DIR = TESSDATA_DIR + File.separator + "configs";

    private static String[] configAssets = {CONFIG_ASSETS + "api_config", CONFIG_ASSETS + "digits", CONFIG_ASSETS  + "digits2", CONFIG_ASSETS + "hocr"};
    private static String[] tessAssets = {TESS4J_ASSETS + "eng.traineddata", TESS4J_ASSETS + "osd.traineddata", TESS4J_ASSETS + "pdf.ttf", TESS4J_ASSETS + "pdf.ttx"};

    static{
        System.out.println("Checking for tessdata..");
        if ( !(new File(TESSDATA_DIR).exists())){
            try {
                Files.createDirectory(Paths.get(TESSDATA_DIR));
                Files.createDirectory(Paths.get(CONFIG_DIR));
                for(String tessAsset: tessAssets){
                    copyAsset(tessAsset,TESSDATA_DIR);
                }
                for(String configAsset: configAssets) {
                    copyAsset(configAsset, CONFIG_DIR);
                }
            } catch (IOException e) {
                System.out.println("Cannot unpack tessdata in " + TESSDATA_DIR);
            }
        } else {
            System.out.println("tessdata has already existed in " + WORK_DIR);
        }
    }

    private static void copyAsset(String name, String dest) throws IOException {
        InputStream in = Tess4jAssetsLoader.class.getResourceAsStream(name);
        File fileOut = new File(dest + File.separator + name.substring(name.lastIndexOf("/")));
        OutputStream out = FileUtils.openOutputStream(fileOut);
        IOUtils.copy(in,out);
        in.close();
        out.close();
    }
}
