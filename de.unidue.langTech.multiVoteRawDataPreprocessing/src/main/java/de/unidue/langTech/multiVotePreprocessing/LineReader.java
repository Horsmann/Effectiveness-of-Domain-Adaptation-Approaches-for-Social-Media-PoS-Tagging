package de.unidue.langTech.multiVotePreprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

public class LineReader
    extends JCasCollectionReader_ImplBase
{

    public static final String PARAM_INPUT_FILE = "InputFile";
    @ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
    private File inputFile;

    public static final String PARAM_ENCODING = "encoding";
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    private static BufferedReader br = null;
    private static String nextLine = null;

    public static boolean stop = false;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        try {
            InputStreamReader isr = null;
            if (inputFile.getAbsolutePath().endsWith(".gz")) {
                isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile)),
                        encoding);
            }
            else {
                isr = new InputStreamReader(new FileInputStream(inputFile), encoding);
            }
            br = new BufferedReader(isr);

        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean hasNext()
        throws IOException, CollectionException
    {
        if (stop == true) {
            return false;
        }

        nextLine = br.readLine();
        while (nextLine != null
                && (nextLine.trim().isEmpty() || nextLine.trim().length() < 10 || isFirstCharLatin(nextLine
                        .charAt(0))))

        {
            nextLine = br.readLine();
        }
        if (nextLine == null) {
            br.close();
            return false;
        }
        return true;
    }

    private boolean isFirstCharLatin(char charAt)
    {
        return !((charAt >= 'a' && charAt <= 'z') || (charAt >= 'A' && charAt <= 'Z')
                || (charAt >= '0' && charAt <= '9') || charAt == '@' || charAt == '#');
    }

    public Progress[] getProgress()
    {
        return null;
    }

    @Override
    public void getNext(JCas jCas)
        throws IOException, CollectionException
    {
        jCas.setDocumentText(nextLine);
        jCas.setDocumentLanguage("en");
    }

}
