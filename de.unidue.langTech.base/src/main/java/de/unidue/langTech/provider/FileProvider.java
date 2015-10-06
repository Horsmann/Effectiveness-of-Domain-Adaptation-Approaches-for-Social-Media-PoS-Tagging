package de.unidue.langTech.provider;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.IOUtil;

public class FileProvider
{
    public String getFine2CoarsePOSMappingFilePath()
        throws Exception
    {
        return copy2TmpDirectoryAndReturnPath2File("pos.map");
    }

    public String getLog4jPropertyFilePath()
        throws Exception
    {
        return copy2TmpDirectoryAndReturnPath2File("log4j.properties");
    }

    private String copy2TmpDirectoryAndReturnPath2File(String name)
        throws Exception
    {
        InputStream inputStream = getClass().getResourceAsStream(name);
        String pathFile = IOUtil.toString(inputStream);

        File tmpLocation = new File(System.getProperty("java.io.tmpdir") + "/"
                + System.currentTimeMillis() + "_" + name);
        tmpLocation.deleteOnExit();
        FileUtils.writeStringToFile(tmpLocation, pathFile, "utf-8");
        return tmpLocation.getAbsolutePath();
    }

}
