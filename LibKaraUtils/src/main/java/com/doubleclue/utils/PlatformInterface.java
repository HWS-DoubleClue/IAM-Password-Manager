package com.doubleclue.utils;

/**
 * Created by andreas.koerner on 29.03.2017.
 */

public interface PlatformInterface {

    /**
     *
     * @return
     * @throws Exception
     */
    String getModel() throws Exception;

    /**
     *
     * @return
     * @throws Exception
     */
    String getManufacture() throws Exception;

    /**
     *
     * @return
     * @throws Exception
     */
    String getOsVersion() throws Exception;

    /**
     *
     * @return
     * @throws Exception
     */
    byte[] generateUdid() throws Exception;

    /**
     *
     * @param clazz
     * @return
     * @throws Exception
     */
    ProductVersion getProductVersion(Class<?> clazz) throws Exception;

    /**
     * @param name This is only the file without path.
     * @return
     */
    byte[] readFile(String name, boolean cache) throws Exception;


    /**
     * @param name the name of the file
     * @param data     the content which will be stored in the file
     * @param cache    Decides whether the file is stored in the Cache or in the standard storage
     * @throws Exception when something goes wrong while writing the file
     */
    void writeFile(String name, byte[] data, boolean cache) throws Exception;
    
    void renameFile (String currentFileName, String newFileName);

    String[] getFileList();

    /**
     * This only applies for Windows
     *
     * @param appDirectory
     * @throws Exception
     */
    void setDirectory(String appDirectory) throws Exception;

    /**
     * @param fileName the name of the file to delete
     * @throws Exception when something goes wring while deleting the file
     */
    void deleteFile(String fileName) throws Exception;
}
