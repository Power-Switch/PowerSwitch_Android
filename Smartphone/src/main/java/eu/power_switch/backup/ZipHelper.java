/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.backup;

import android.support.annotation.NonNull;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Helper class for convenient access to zip/unzip methods
 * <p/>
 * Created by Markus on 31.08.2016.
 */
public class ZipHelper {

    /**
     * Creates a zip file from a given directory (recursive)
     * Zip files are encrypted by default
     *
     * @param targetPath  target path where the zip will be created
     * @param password    password of the zip file
     * @param sourcePaths source folder(s) who <b>content</b> will be added to the zip
     * @return zip file
     * @throws FileNotFoundException
     */
    public static ZipFile createZip(@NonNull String targetPath, char[] password, @NonNull String... sourcePaths) throws Exception {
        File targetFile = new File(targetPath);

        ZipFile zipFile = new ZipFile(targetFile);

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setPassword(password);
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
        zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

        if (sourcePaths.length == 0) {
            throw new IllegalArgumentException("No source path(s) defined!");
        }

        if (sourcePaths.length == 1) {
            zipFile.createZipFileFromFolder(sourcePaths[0], zipParameters, false, 0);
        } else {
            for (String sourcePath : sourcePaths) {
                zipFile.addFolder(sourcePath, zipParameters);
            }
        }

        return zipFile;
    }

    /**
     * Extracts a zip file to a specific folder
     *
     * @param sourcePath path of the zip file
     * @param targetPath path where to extract the files
     * @param password   password of the zip file
     * @throws Exception
     */
    public static void extractZip(String sourcePath, String targetPath, char[] password) throws Exception {
        try {
            ZipFile zipFile = new ZipFile(sourcePath);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }

            zipFile.extractAll(targetPath);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

}
