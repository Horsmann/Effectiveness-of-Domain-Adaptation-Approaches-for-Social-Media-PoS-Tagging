package de.unidue.langTech.converter.gimpel;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.RitterFormatWriter;
import de.unidue.langTech.pos.gimpel.Gimpel2011TweetCorpusReader;

public class Gimpel2RitterConverter
{

    public static void main(String[] args)
        throws Exception

    {

        CollectionReaderDescription nps = CollectionReaderFactory.createReaderDescription(
                Gimpel2011TweetCorpusReader.class, Gimpel2011TweetCorpusReader.PARAM_LANGUAGE, "en",
                Gimpel2011TweetCorpusReader.PARAM_SOURCE_LOCATION, "src/main/resources/Gimpel2011/train",
                Gimpel2011TweetCorpusReader.PARAM_PATTERNS, new String[] { "*.train" });

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                 RitterFormatWriter.class, RitterFormatWriter.PARAM_TARGET_LOCATION,
                "/Users/Tobias/Desktop/gimpel.train");

        SimplePipeline.runPipeline(nps, writer);
    }
}