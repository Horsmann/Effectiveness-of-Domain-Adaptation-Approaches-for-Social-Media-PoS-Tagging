package de.unidue.langTech.features.character.extended;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.unidue.langTech.features.EfficientFeature;
import de.unidue.langTech.features.FeatureUtil;

public class SingleLetterCollapsedWord
    extends EfficientFeature
{

    private static final String FEATURE_NAME = "singleLetterCollapsed";

    public List<Feature> extract(JCas aView, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        super.extract(aView, aClassificationUnit);
        Integer idx = EfficientFeature.begin2Idx.get(aClassificationUnit.getBegin());
        String token = EfficientFeature.tokens.get(idx);

        Feature feature = FeatureUtil.wrapAsFeature(FEATURE_NAME, singleLetterCollapsedWord(token));
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        return features;
    }

    static String singleLetterCollapsedWord(String aToken)
    {
        StringBuilder sb = new StringBuilder();

        String prevChar = null;
        for (char c : aToken.toCharArray()) {
            if (prevChar == null) {
                prevChar = "" + c;
                sb.append("" + c);
                continue;
            }
            if (c == prevChar.charAt(0)) {
                if (sb.charAt(sb.length() - 1) != '+') {
                    sb.append("+");
                }
            }
            else {
                prevChar = "" + c;
                sb.append("" + c);
            }
        }

        return sb.toString();
    }

}
