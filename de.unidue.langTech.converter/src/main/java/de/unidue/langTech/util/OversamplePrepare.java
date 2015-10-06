package de.unidue.langTech.util;

import java.io.File;

import com.google.common.io.Files;

public class OversamplePrepare
{
    /*
     * 1 = file to be copied 2 = folder where the copied file will be written to 3 = number of
     * copies requested
     */
    public static void main(String[] args)
        throws Exception
    {
        String fileName = args[0];
        String targetFolder = args[1];
        String nrCopies = args[2];
        
        File inputFile = new File(fileName);

        String inputFileName = inputFile.getName();
        String fileEnding = inputFileName.substring(inputFileName.indexOf(".") + 1);
        inputFileName = inputFileName.substring(0, inputFileName.indexOf("."));

        int numCopies = Integer.valueOf(nrCopies);

        String outputFolder = targetFolder + leadingZeroPad(numCopies);
        new File(outputFolder).mkdirs();

        for (int i = 1; i <= numCopies; i++) {
            Files.copy(inputFile, new File(outputFolder + "/" + inputFileName + leadingZeroPad(i)
                    + "." + fileEnding));
        }

    }

    public static String leadingZeroPad(int i)
    {
        return (i > 99 ? "" + i : ((i > 9) ? "0" + i : "00" + i));
    }
}
