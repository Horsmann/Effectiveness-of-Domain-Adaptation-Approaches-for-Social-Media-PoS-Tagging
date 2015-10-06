package de.unidue.langTech.multiVotePreprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class AgreedTweetPrinter
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private String targetLocation;

    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    /**
     * Ends processing after the limit has been reached
     */
    public static final String PARAM_TWEET_LIMIT = "PARAM_TWEET_LIMIT";
    @ConfigurationParameter(name = PARAM_TWEET_LIMIT, mandatory = true)
    private Integer tweetLimit;

    int tweetCounter = 0;

    private int tokenCount = 0;
    private static BufferedWriter bw = null;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {

        initWriter();

        List<String[]> tweet = new ArrayList<String[]>();
        boolean disagreement = false;
        Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
        for (Token t : tokens) {
            String[] otherTaggers = getTags(aJCas, t.getBegin(), t.getEnd());

            String news1 = otherTaggers[0];
            String news2 = otherTaggers[1];
            String news3 = otherTaggers[2];

            if (news1.equals(news2) && news1.equals(news3)) {
                tweet.add(new String[] { t.getCoveredText(), news1 });
            }
            else {
                disagreement = true;
                break;
            }

        }
        if (disagreement) {
            return;
        }
        if (tweet.size() < 3) {
            return;
        }

        tokenCount += tweet.size();

        tweet = ritterizeTwitterTags(tweet);

        writeTweet(tweet);

    }

    private String[] getTags(JCas aJCas, int begin, int end)
    {
        List<POS> pos = JCasUtil.selectCovered(aJCas, POS.class, begin, end);

        List<String> tags = new ArrayList<String>();

        for (POS p : pos) {
            tags.add(p.getPosValue());
        }

        return tags.toArray(new String[0]);
    }

    private List<String[]> ritterizeTwitterTags(List<String[]> tweet)
    {
        List<String[]> updatedTweet = new ArrayList<String[]>();
        for (String[] e : tweet) {
            String[] updatedTag = e;
            if (e[0].startsWith("@")) {
                updatedTag[1] = "USR";
            }
            if (e[0].startsWith("#")) {
                updatedTag[1] = "HT";
            }
            if (e[0].equals("RT")) {
                updatedTag[1] = "RT";
            }
            if (e[0].startsWith("http://") || e[0].startsWith("www.")) {
                updatedTag[1] = "URL";
            }
            updatedTweet.add(updatedTag);
        }
        return updatedTweet;
    }

    private void writeTweet(List<String[]> tweet)
    {
        tweetCounter++;
        try {
            for (String[] e : tweet) {

                bw.write(e[0] + " " + e[1]);
                bw.write("\n");

            }
            bw.write("\n");
            bw.flush();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        if (tweetCounter >= tweetLimit) {
            LineReader.stop = true;
        }

    }

    private void initWriter()
    {
        if (bw != null) {
            return;
        }
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(
                    targetLocation)), encoding));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        System.out.println("Token: " + tokenCount);
        try {
            bw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
