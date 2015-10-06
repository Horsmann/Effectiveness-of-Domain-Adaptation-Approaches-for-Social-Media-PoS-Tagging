package de.unidue.langTech.features.token;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.unidue.langTech.features.EfficientFeature;
import de.unidue.langTech.features.FeatureUtil;

public class PreviousToken
    extends EfficientFeature
{

    static final String FEATURE_NAME = "previousToken";
    private static final String BEGIN_OF_SEQUENCE = "BOS";

    public List<Feature> extract(JCas aView, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        super.extract(aView, aClassificationUnit);
        Integer idx = EfficientFeature.begin2Idx.get(aClassificationUnit.getBegin());

        String featureVal = previousToken(idx);
        Feature feature = FeatureUtil.wrapAsFeature(FEATURE_NAME, featureVal);

        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        return features;

    }

    private String previousToken(Integer idx)
    {
        if (idx - 1 >= 0) {
            return tokens.get(idx - 1);
        }
        return BEGIN_OF_SEQUENCE;
    }

}
