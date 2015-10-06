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
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class OwoputisBrownClusterFeature
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{

    public static final String PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES = "brownClassProbLocation";
    @ConfigurationParameter(name = PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES, mandatory = true)
    private File inputFile;

    HashSet<String> classes = new HashSet<String>();
    HashMap<String, String> map = null;

    public List<Feature> extract(JCas aJcas, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        init();

        String unit = aClassificationUnit.getCoveredText().toLowerCase().toLowerCase();

        unit = substituteHashtags(unit);
        unit = substituteUrl(unit);
        unit = substituteAtMentions(unit);

        List<Feature> features = createFeatures(unit);

        return features;
    }

    private List<Feature> createFeatures(String unit)
    {
        List<Feature> features = new ArrayList<Feature>();

        String bitCode = map.get(unit);

        features.add(new Feature("brown16_", bitCode != null && bitCode.length() >= 16 ? bitCode
                .substring(0, 16) : "*"));
        features.add(new Feature("brown14_", bitCode != null && bitCode.length() >= 14 ? bitCode
                .substring(0, 14) : "*"));
        features.add(new Feature("brown12_", bitCode != null && bitCode.length() >= 12 ? bitCode
                .substring(0, 12) : "*"));
        features.add(new Feature("brown10_", bitCode != null && bitCode.length() >= 10 ? bitCode
                .substring(0, 10) : "*"));
        features.add(new Feature("brown8_", bitCode != null && bitCode.length() >= 8 ? bitCode
                .substring(0, 8) : "*"));
        features.add(new Feature("brown6_", bitCode != null && bitCode.length() >= 6 ? bitCode
                .substring(0, 6) : "*"));
        features.add(new Feature("brown4_", bitCode != null && bitCode.length() >= 4 ? bitCode
                .substring(0, 4) : "*"));
        features.add(new Feature("brown2_", bitCode != null && bitCode.length() >= 2 ? bitCode
                .substring(0, 2) : "*"));
        return features;
    }

    private void init()
        throws TextClassificationException
    {

        if (map != null) {
            return;
        }
        map = new HashMap<String, String>();

        try {

            BufferedReader bf = openFile();
            String line = null;
            while ((line = bf.readLine()) != null) {
                String[] split = line.split("\t");
                map.put(split[1], split[0]);
            }

        }
        catch (Exception e) {
            throw new TextClassificationException(e);
        }
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

}
