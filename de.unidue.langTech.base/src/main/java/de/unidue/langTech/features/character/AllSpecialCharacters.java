package de.unidue.langTech.features.character;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.unidue.langTech.features.EfficientFeature;
import de.unidue.langTech.features.FeatureUtil;

public class AllSpecialCharacters
    extends EfficientFeature
{

    private static final String FEATURE_NAME = "allSpecial";

    public List<Feature> extract(JCas aView, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        super.extract(aView, aClassificationUnit);
        Integer idx = EfficientFeature.begin2Idx.get(aClassificationUnit.getBegin());
        String token = EfficientFeature.tokens.get(idx);
        
        
        Feature feature = FeatureUtil.wrapAsFeature(FEATURE_NAME, allSpecialCharacter(token));

        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        return features;
    }

    static boolean allSpecialCharacter(String aCoveredText)
    {
        boolean isSpecialCharacter = false;
        for (char c : aCoveredText.toCharArray()) {
            if (FeatureUtil.isNoNumberAndNoCharacter(c)) {
                isSpecialCharacter = true;
            }
            else {
                isSpecialCharacter = false;
                break;
            }
        }

        return isSpecialCharacter;
    }

}
