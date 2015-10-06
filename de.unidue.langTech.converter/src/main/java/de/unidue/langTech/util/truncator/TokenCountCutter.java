package de.unidue.langTech.util.truncator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TokenCountCutter
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_TARGET_LOCATION = "PARAM_TARGET_LOCATION";
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private String outputFileLocation;

    public static final String PARAM_TOKEN_LIMIT = "PARAM_TOKEN_LIMIT";
    @ConfigurationParameter(name = PARAM_TOKEN_LIMIT, mandatory = true)
    private int tokenLimit;

    public static final String PARAM_ENCODING = "PARAM_ENCODING";
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    private int counterOfWrittenFiles = 0;

    private int tokenCount = 0;

    private static BufferedWriter bf = null;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
        tokenCount += tokens.size();

        writeToFile(tokens);

        if (tokenCount >= tokenLimit) {
            closeCurrentFileOpenNext();
            tokenCount = 0;
        }

    }

    private void closeCurrentFileOpenNext()
        throws AnalysisEngineProcessException
    {
        try {
            bf.close();
            openFileStream();

        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void writeToFile(Collection<Token> aTokens)
        throws AnalysisEngineProcessException
    {
        try {
            if (bf == null) {
                openFileStream();
            }
            for (Token t : aTokens) {
                bf.write(t.getCoveredText() + " " + t.getPos().getPosValue());
                bf.write("\n");
            }
            bf.write("\n");
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private void openFileStream()
        throws AnalysisEngineProcessException
    {
        String fileName = outputFileLocation + "/" + "file"
                + String.format("%5d", ++counterOfWrittenFiles).replaceAll(" ", "0") + ".data";
        try {
            bf = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(fileName)), encoding));
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        try {
            bf.close();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
