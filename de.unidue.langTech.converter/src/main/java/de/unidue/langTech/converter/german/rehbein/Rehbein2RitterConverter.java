package de.unidue.langTech.converter.german.rehbein;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.RitterFormatWriter;

public class Rehbein2RitterConverter
{
    public static void main(String[] args)
        throws Exception

    {
        String dataFolder = "";
        String outputFileName = "outFile";
        String output = System.getProperty("user.home") + "/Desktop";
        if (args.length >= 1) {
            dataFolder = args[0];
        }
        else if (args.length >= 2) {
            outputFileName = args[1];
        }
        else {
            System.err.println("No path provided");
            return;
        }

        CollectionReaderDescription tiger = CollectionReaderFactory.createReaderDescription(
                Rehbein2013TweetCorpusReader.class, Rehbein2013TweetCorpusReader.PARAM_LANGUAGE,
                "de", Rehbein2013TweetCorpusReader.PARAM_SOURCE_LOCATION, dataFolder,
                Rehbein2013TweetCorpusReader.PARAM_PATTERNS, new String[] { "*.xml" });

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                RitterFormatWriter.class, RitterFormatWriter.PARAM_TARGET_LOCATION, output + "/"
                        + outputFileName);

        SimplePipeline.runPipeline(tiger, writer);
    }
}
