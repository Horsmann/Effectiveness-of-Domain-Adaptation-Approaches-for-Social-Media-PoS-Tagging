package de.unidue.langTech.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public abstract class EfficientFeature
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    private String lastSeenDocumentId = "";

    protected static HashMap<Integer, Token> begin2Token = new HashMap<Integer, Token>();
    protected static HashMap<Integer, Integer> begin2Idx = new HashMap<Integer, Integer>();
    protected static List<String> tokens = new ArrayList<String>();

    public List<Feature> extract(JCas aView, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        if (isTheSameDocument(aView)){
            return null;
        }
        
        begin2Token = new HashMap<Integer, Token>();
        begin2Idx = new HashMap<Integer, Integer>();
        tokens = new ArrayList<String>();

        int i = 0;
        for (Token t : JCasUtil.select(aView, Token.class)) {
            Integer begin = t.getBegin();
            begin2Token.put(begin, t);
            begin2Idx.put(begin, i++);
            tokens.add(t.getCoveredText());
        }

        return null;
    }

    private boolean isTheSameDocument(JCas aView)
    {
        DocumentMetaData meta = JCasUtil.selectSingle(aView, DocumentMetaData.class);
        String currentId = meta.getDocumentId();
        boolean isSame = currentId.equals(lastSeenDocumentId);
        lastSeenDocumentId = currentId;
        return isSame;
    }

}
