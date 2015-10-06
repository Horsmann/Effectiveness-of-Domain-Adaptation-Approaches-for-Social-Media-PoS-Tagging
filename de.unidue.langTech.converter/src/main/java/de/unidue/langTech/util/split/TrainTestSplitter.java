package de.unidue.langTech.util.split;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TrainTestSplitter
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_SPLIT_AT = "PARAM_SPLIT_AT";
    @ConfigurationParameter(name = PARAM_SPLIT_AT, mandatory = true)
    protected int tokenLimit;

    public static final String PARAM_TARGET_LOCATION_TRAIN = "PARAM_TARGET_LOCATION_TRAIN";
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION_TRAIN, mandatory = true)
    private String train;
    
    public static final String PARAM_TARGET_LOCATION_TEST = "PARAM_TARGET_LOCATION_TEST";
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION_TEST, mandatory = true)
    private String test;

    static List<MySentence> allSentence = new ArrayList<MySentence>();

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        Collection<Sentence> sentences = JCasUtil.select(aJCas, Sentence.class);
        for (Sentence s : sentences) {
            List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, s.getBegin(),
                    s.getEnd());

            List<String> toks = new ArrayList<String>();
            List<String> pos = new ArrayList<String>();
            for (Token t : tokens) {
                String tokenText = t.getCoveredText();
                String posText = t.getPos().getPosValue();
                toks.add(tokenText);
                pos.add(posText);
            }
            allSentence.add(new MySentence(toks, pos));
        }
    }

    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {

        List<MySentence> trainSubset = new ArrayList<MySentence>();
        List<MySentence> testSubset = new ArrayList<MySentence>();
        int tokenCount = 0;
        for (MySentence s : allSentence) {
            tokenCount += s.token.size();
            if (tokenCount < tokenLimit){
                trainSubset.add(s);
            }else
            {
                testSubset.add(s);
            }
        }
        
        write(train, trainSubset);
        write(test, testSubset);
    }

    private void write(String target, List<MySentence> sent)
    {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(target)), "UTF-8"));

            for (MySentence s : sent) {
                int N = s.pos.size();
                for (int i = 0; i < N; i++) {
                    bw.write(s.token.get(i) + " " + s.pos.get(i));
                    bw.write("\n");
                }
                bw.write("\n");
            }
            bw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }        
    }

    class MySentence
    {
        private List<String> token;
        private List<String> pos;

        MySentence(List<String> token, List<String> pos)
        {
            this.token = token;
            this.pos = pos;

        }
    }

}
