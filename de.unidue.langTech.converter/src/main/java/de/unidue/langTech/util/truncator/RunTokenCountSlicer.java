package de.unidue.langTech.util.truncator;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.german.tiger.SimpleXMLTigerCorpusReader;
import de.unidue.langTech.pos.ritter.Ritter2011TweetCorpusReader;

public class RunTokenCountSlicer
{
    public static void main(String[] args)
        throws Exception

    {
        String inputFolder = args[0];
        String outputFolder = args[1];
        int count = Integer.valueOf(args[2]);
        
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
        		Ritter2011TweetCorpusReader.class, SimpleXMLTigerCorpusReader.PARAM_LANGUAGE, "en",
        		Ritter2011TweetCorpusReader.PARAM_SOURCE_LOCATION, inputFolder,
        		Ritter2011TweetCorpusReader.PARAM_PATTERNS, new String[] { "*.data" });

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                TokenCountCutter.class, TokenCountCutter.PARAM_TOKEN_LIMIT, count,
                TokenCountCutter.PARAM_TARGET_LOCATION, outputFolder);

        SimplePipeline.runPipeline(reader, writer);
    }
}
