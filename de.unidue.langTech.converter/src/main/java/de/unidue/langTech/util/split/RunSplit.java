package de.unidue.langTech.util.split;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.pos.ritter.Ritter2011TweetCorpusReader;

public class RunSplit
{
    public static void main(String[] args)
        throws Exception

    {
        CollectionReaderDescription nps = CollectionReaderFactory.createReaderDescription(
                Ritter2011TweetCorpusReader.class, Ritter2011TweetCorpusReader.PARAM_LANGUAGE,
                "en", Ritter2011TweetCorpusReader.PARAM_SOURCE_LOCATION, args[0],
                Ritter2011TweetCorpusReader.PARAM_PATTERNS, new String[] { "*.txt" });

        String train = System.getProperty("user.home") + "/Desktop/" + "Train.data";
        String test = System.getProperty("user.home") + "/Desktop/" + "Test.data";
        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                TrainTestSplitter.class, TrainTestSplitter.PARAM_SPLIT_AT, 50000,
                TrainTestSplitter.PARAM_TARGET_LOCATION_TRAIN, train,
                TrainTestSplitter.PARAM_TARGET_LOCATION_TEST, test);

        SimplePipeline.runPipeline(nps, writer);
    }
}
