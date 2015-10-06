package de.unidue.langTech.features.character.extended;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.unidue.langTech.features.EfficientFeature;
import de.unidue.langTech.features.FeatureUtil;

public class AlphabeticOrderedBagOfChar
    extends EfficientFeature
{
    private static final String FEATURE_NAME = "charBagAlpha";

    public List<Feature> extract(JCas aView, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        super.extract(aView, aClassificationUnit);
        Integer idx = EfficientFeature.begin2Idx.get(aClassificationUnit.getBegin());
        String token = EfficientFeature.tokens.get(idx);

        Feature feature = FeatureUtil
                .wrapAsFeature(FEATURE_NAME, alphabeticOrderedBagOfChar(token));
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        return features;
    }

    static String alphabeticOrderedBagOfChar(String aToken)
    {
        String boc = FeatureUtil.makeBagOfChars(aToken);
        String alpOrdBoc = orderAlphabetic(boc);
        return alpOrdBoc;
    }

    private static String orderAlphabetic(String str)
    {
        Character[] chars = new Character[str.length()];
        for (int i = 0; i < chars.length; i++)
            chars[i] = str.charAt(i);

        // sort the array
        Arrays.sort(chars, new Comparator<Character>()
        {
            public int compare(Character c1, Character c2)
            {
                int cmp = Character.compare(Character.toLowerCase(c1.charValue()),
                        Character.toLowerCase(c2.charValue()));
                if (cmp != 0)
                    return cmp;
                return Character.compare(c1.charValue(), c2.charValue());
            }
        });
        StringBuilder sb = new StringBuilder(chars.length);
        for (char c : chars)
            sb.append(c);
        str = sb.toString();
        return str;
    }

}
