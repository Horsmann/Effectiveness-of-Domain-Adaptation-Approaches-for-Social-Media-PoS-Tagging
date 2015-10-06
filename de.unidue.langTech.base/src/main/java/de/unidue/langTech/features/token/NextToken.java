package de.unidue.langTech.features.token;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.unidue.langTech.features.EfficientFeature;
import de.unidue.langTech.features.FeatureUtil;

public class NextToken
    extends EfficientFeature
{

    static final String FEATURE_NAME = "nextToken";
    private static final String END_OF_SEQUENCE = "EOS";

    public List<Feature> extract(JCas aView, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        super.extract(aView, aClassificationUnit);
        Integer idx = EfficientFeature.begin2Idx.get(aClassificationUnit.getBegin());

        String featureVal = nextToken(idx);
        Feature feature = FeatureUtil.wrapAsFeature(FEATURE_NAME, featureVal);

        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        return features;

    }

    private String nextToken(Integer idx)
    {
        if (idx + 1 < EfficientFeature.tokens.size()) {
            return tokens.get(idx + 1);
        }
        return END_OF_SEQUENCE;
    }

}
