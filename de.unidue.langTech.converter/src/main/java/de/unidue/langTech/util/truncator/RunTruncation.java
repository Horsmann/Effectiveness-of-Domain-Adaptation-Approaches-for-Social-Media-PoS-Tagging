package de.unidue.langTech.util.truncator;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.german.tiger.SimpleXMLTigerCorpusReader;
import de.unidue.langTech.pos.ritter.Ritter2011TweetCorpusReader;

public class RunTruncation
{
    public static void main(String[] args)
        throws Exception

    {
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
        		Ritter2011TweetCorpusReader.class, SimpleXMLTigerCorpusReader.PARAM_LANGUAGE, "en",
        		Ritter2011TweetCorpusReader.PARAM_SOURCE_LOCATION, args[0],
        		Ritter2011TweetCorpusReader.PARAM_PATTERNS, new String[] { "*.data" });

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                Truncator.class, Truncator.PARAM_TOKEN_LIMIT, 50000,
                Truncator.PARAM_TARGET_LOCATION, args[1]);

        SimplePipeline.runPipeline(reader, writer);
    }
}
