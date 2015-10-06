package de.unidue.langTech.features.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class LDAClusterFeature
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{

    public static final String PARAM_LDA_CLUSTER_CLASS_PROPABILITIES = "ldaClassProbLocation";
    @ConfigurationParameter(name = PARAM_LDA_CLUSTER_CLASS_PROPABILITIES, mandatory = true)
    private File inputFile;

    public static final String PARAM_USE_LEMMAS = "ldaUseLemma";
    @ConfigurationParameter(name = PARAM_USE_LEMMAS, mandatory = true)
    private boolean useLemma;

    private static final String FEATURE_NAME = "ldaCluster";
    private static final Double MIN_PROBABILITY = 0.001;
    private int LIMIT = 5;

    HashSet<String> classes = new HashSet<String>();
    HashMap<String, List<Pair>> map = null;

    public List<Feature> extract(JCas aJcas, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        init();

        String unit = getAccesUnit(aJcas, aClassificationUnit);
        unit = substituteHashtags(unit);
        unit = substituteUrl(unit);
        unit = substituteAtMentions(unit);

        List<Feature> features = new ArrayList<Feature>();

        List<Pair> classesOfUnit = map.get(unit);
        if (classesOfUnit == null) {
            classesOfUnit = new ArrayList<Pair>();
        }

        features = createFeatures(features, classesOfUnit);

        return features;
    }

    private String getAccesUnit(JCas aJcas, TextClassificationUnit aClassificationUnit)
    {
        String unit = null;

        if (useLemma) {
            List<Lemma> TCUlemma = new ArrayList<Lemma>(JCasUtil.selectCovered(aJcas, Lemma.class,
                    aClassificationUnit.getBegin(), aClassificationUnit.getEnd()));

            unit = TCUlemma.get(0).getValue();
        }
        else {
            unit = JCasUtil
                    .selectCovered(aJcas, Token.class, aClassificationUnit.getBegin(),
                            aClassificationUnit.getEnd()).get(0).getCoveredText();
        }
        return unit;
    }

    private String substituteHashtags(String token)
    {
        return token.startsWith("#") ? "<HASHTAG>" : token;
    }

    private String substituteUrl(String token)
    {
        return token.startsWith("www.") || token.startsWith("http:/") ? "<URL>" : token;
    }

    private String substituteAtMentions(String token)
    {
        return token.startsWith("@") ? "<ATMENTION>" : token;
    }

    private List<Feature> createFeatures(List<Feature> features, List<Pair> topNClustersOfWord)
    {
        int i = 0;
        for (Pair p : topNClustersOfWord) {
            if (p.prob < MIN_PROBABILITY) {
                continue;
            }

            features.add(new Feature(FEATURE_NAME + "_" + (i + 1), p.clustNum));
            i++;
        }
        // create always the same number of features ...
        for (; i < LIMIT; i++) {
            features.add(new Feature(FEATURE_NAME + "_" + (i + 1), "*"));
        }

        return features;
    }

    private void init()
        throws TextClassificationException
    {

        if (map != null) {
            return;
        }
        map = new HashMap<String, List<Pair>>();
        try {
            determineAllClasses();
            getTopNClassesPerWord();
        }
        catch (Exception e) {
            throw new TextClassificationException(e);
        }
    }

    private void getTopNClassesPerWord()
        throws Exception
    {
        BufferedReader bfr = openFile();

        String line = "";
        while ((line = bfr.readLine()) != null) {
            String[] split = line.split("\t");

            List<Pair> mostProbClasses = new ArrayList<LDAClusterFeature.Pair>();
            int numClustersAdded = 0;
            for (int i = 1; i < split.length && numClustersAdded < LIMIT; i = i + 2) {
                mostProbClasses.add(new Pair(Integer.valueOf(split[i]), Double
                        .valueOf(split[i + 1])));
                numClustersAdded++;
            }
            map.put(split[0], mostProbClasses);
        }

    }

    private void determineAllClasses()
        throws Exception
    {
        BufferedReader bfr = openFile();

        String line = "";
        while ((line = bfr.readLine()) != null) {
            String[] split = line.split("\t");

            for (int i = 1; i < split.length; i = i + 2) {
                classes.add(split[i]);
            }
        }
        bfr.close();
    }

    private BufferedReader openFile()
        throws Exception
    {
        InputStreamReader isr = null;
        if (inputFile.getAbsolutePath().endsWith(".gz")) {
            isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile)),
                    "UTF-8");
        }
        else {
            isr = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
        }
        return new BufferedReader(isr);
    }

    class Pair
    {
        private Integer clustNum;
        private Double prob;

        Pair(Integer k, Double prob)
        {
            this.clustNum = k;
            this.prob = prob;
        }
    }
}
