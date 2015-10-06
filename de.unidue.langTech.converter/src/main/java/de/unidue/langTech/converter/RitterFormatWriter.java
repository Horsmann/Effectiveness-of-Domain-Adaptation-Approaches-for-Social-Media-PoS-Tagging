package de.unidue.langTech.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class RitterFormatWriter
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private String targetLocation;

    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    private static StringBuilder sb = new StringBuilder();
    
    public static final String PARAM_MISSING_POS = "PARAM_MISSING_POS";
    @ConfigurationParameter(name = PARAM_MISSING_POS, mandatory = false, defaultValue = "XYZ")
    private String missingPosDummy;

    static BufferedWriter bf = null;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {

        if (bf == null) {
            init();
        }

        for (Sentence s : JCasUtil.select(aJCas, Sentence.class)) {
            sb = new StringBuilder();
            for (Token token : JCasUtil.selectCovered(aJCas, Token.class, s.getBegin(), s.getEnd())) {
            	String posValue = missingPosDummy;
            	POS pos = token.getPos();
            	if (pos != null){
            		posValue = pos.getPosValue();
            	}
                sb.append(token.getCoveredText() + " " + posValue + "\n");
            }
            sb.append("\n");
            write(bf, sb);
        }

    }

    private void init()
    {
        try {
            bf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(
                    targetLocation)), encoding));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void write(BufferedWriter aBf, StringBuilder aSb)
    {
        try {
            bf.write(sb.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        if (bf != null) {
            try {
                bf.close();
                bf=null;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
