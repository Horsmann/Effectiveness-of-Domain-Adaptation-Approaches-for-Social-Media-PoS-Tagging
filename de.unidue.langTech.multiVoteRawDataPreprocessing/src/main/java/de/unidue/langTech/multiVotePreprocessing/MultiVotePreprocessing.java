package de.unidue.langTech.multiVotePreprocessing;

import org.apache.log4j.PropertyConfigurator;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.langTech.provider.FileProvider;

public class MultiVotePreprocessing
{

    public static void main(String[] args)
        throws Exception
    {
        int argNum = 3;
        if (args.length != argNum) {
            System.err
                    .println("Require "
                            + argNum
                            + " arguments:\n"
                            + "\t(1)  input file with one raw tweets per line (file path)\n"
                            + "\t(2)  output file (file path)\n"
                            + "\t(3)  stop limit, processing ends if N tweet were multi-agreed tagged (integer value)");
            return;
        }

        String inputFile = args[0];
        String ouputFile = args[1];
        Integer limit = Integer.valueOf(args[2]);

        PropertyConfigurator.configure(new FileProvider().getLog4jPropertyFilePath());

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                LineReader.class, LineReader.PARAM_INPUT_FILE, inputFile);

        AnalysisEngineDescription tokenizer = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription clearNLP = AnalysisEngineFactory.createEngineDescription(
                ClearNlpPosTagger.class, ClearNlpPosTagger.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription openNLP = AnalysisEngineFactory.createEngineDescription(
                OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription stanford = AnalysisEngineFactory.createEngineDescription(
                StanfordPosTagger.class, StanfordPosTagger.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription printer = AnalysisEngineFactory.createEngineDescription(
                AgreedTweetPrinter.class, AgreedTweetPrinter.PARAM_TARGET_LOCATION, ouputFile,
                AgreedTweetPrinter.PARAM_TWEET_LIMIT, limit);

        SimplePipeline.runPipeline(reader, tokenizer, clearNLP, stanford, openNLP, printer);
    }

}
