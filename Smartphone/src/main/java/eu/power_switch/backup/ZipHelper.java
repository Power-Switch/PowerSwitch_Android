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
import android.support.annotation.WorkerThread;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Helper class for convenient access to zip/unzip methods
 * <p/>
 * Created by Markus on 31.08.2016.
 */
public class ZipHelper {

    private static final int PROGRESS_UPDATE_TIMEOUT = 16;

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
    @WorkerThread
    public static ZipFile createZip(@NonNull String targetPath, char[] password, @NonNull OnZipProgressChangedListener onZipProgressChangedListener, @NonNull String... sourcePaths) throws Exception {
        File targetFile = new File(targetPath);

        ZipFile zipFile = new ZipFile(targetFile);
        zipFile.setRunInThread(true);

        ZipParameters zipParameters = new ZipParameters();
        // TODO: Password and encryption are not decrypted correctly when extracting the files
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_STORE);
        zipParameters.setIncludeRootFolder(false);
        // zipParameters.setPassword(password);
//        zipParameters.setEncryptFiles(false);
//        zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);

        if (sourcePaths.length == 0) {
            throw new IllegalArgumentException("No source path(s) defined!");
        }

        if (sourcePaths.length == 1) {
            zipFile.createZipFileFromFolder(sourcePaths[0], zipParameters, false, 0);

            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
            // TODO: replace with... looper?
            while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
                onZipProgressChangedListener.onProgressChanged(progressMonitor);

                Thread.sleep(PROGRESS_UPDATE_TIMEOUT);
            }

            onZipProgressChangedListener.onProgressChanged(progressMonitor);
        } else {
            for (String sourcePath : sourcePaths) {
                zipFile.addFolder(sourcePath, zipParameters);

                ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
                // TODO: replace with... looper?
                while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
                    onZipProgressChangedListener.onProgressChanged(progressMonitor);

                    Thread.sleep(PROGRESS_UPDATE_TIMEOUT);
                }

                onZipProgressChangedListener.onProgressChanged(progressMonitor);
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
    @WorkerThread
    public static void extractZip(String sourcePath, String targetPath, char[] password, @NonNull OnZipProgressChangedListener onZipProgressChangedListener) throws Exception {
        ZipFile zipFile = new ZipFile(sourcePath);
        if (zipFile.isEncrypted()) {
            zipFile.setPassword(password);
        }
        zipFile.setRunInThread(true);

        zipFile.extractAll(targetPath);

        ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
        // TODO: replace with... looper?
        while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
            onZipProgressChangedListener.onProgressChanged(progressMonitor);

            Thread.sleep(PROGRESS_UPDATE_TIMEOUT);
        }

        onZipProgressChangedListener.onProgressChanged(progressMonitor);
    }

}
