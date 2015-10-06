package de.unidue.langTech.features.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import de.unidue.langTech.features.jsonLoader.JsonLoader;

public class DictionaryTagFeature
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    public static final String PARAM_DICTIONARY_LOCATION = "dictLocation";
    @ConfigurationParameter(name = PARAM_DICTIONARY_LOCATION, mandatory = true)
    protected String dictionaryLocation;

    private static ConditionalFrequencyDistribution<String, String> cfd = null;

    private static String UNKNOWN = "*";

    public List<Feature> extract(JCas aView, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        List<Feature> features = new ArrayList<Feature>();

        initCFDIfNotLoadedYet();

        String tokenText = aClassificationUnit.getCoveredText();

        features.addAll(addTop3MostFrequentTags(tokenText));

        return features;
    }

    private Collection<? extends Feature> addTop3MostFrequentTags(String aTokenText)
    {
        List<Feature> topTags = new ArrayList<Feature>();

        List<String> freqOrderedTags = getTop3FrequencyOrderedTags(cfd, aTokenText);
        for (int i = 0; i < 3; i++) {
            if (i < freqOrderedTags.size()) {
                topTags.add(new Feature((i + 1) + "_DictTag", freqOrderedTags.get(i)));
            }
            else {
                topTags.add(new Feature((i + 1) + "_DictTag", UNKNOWN));
            }
        }

        return topTags;
    }

    private List<String> getTop3FrequencyOrderedTags(
            ConditionalFrequencyDistribution<String, String> aCfd, String aTokenText)
    {
        FrequencyDistribution<String> fd = cfd.getFrequencyDistribution(aTokenText);

        List<String> mostFrequentSamples = null;
        if (fd == null) {
            mostFrequentSamples = new ArrayList<String>();
            mostFrequentSamples.add(UNKNOWN);
            mostFrequentSamples.add(UNKNOWN);
            mostFrequentSamples.add(UNKNOWN);
        }
        else {
            mostFrequentSamples = fd.getMostFrequentSamples(3);
        }

        return mostFrequentSamples;
    }

    private void initCFDIfNotLoadedYet()
    {
        if (cfd != null) {
            return;
        }

        try {
            cfd = JsonLoader.loadCFD(dictionaryLocation);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
