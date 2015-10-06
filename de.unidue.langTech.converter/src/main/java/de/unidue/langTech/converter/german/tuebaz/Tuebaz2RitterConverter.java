package de.unidue.langTech.converter.german.tuebaz;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.RitterFormatWriter;

public class Tuebaz2RitterConverter
{

    public static void main(String[] args)
        throws Exception

    {
        String dataFolder = "";
        String output = System.getProperty("user.home") + "/Desktop";
        if (args.length == 1) {
            dataFolder = args[0];
        }
        else {
            System.err.println("No path provided");
            return;
        }

        CollectionReaderDescription corpus = CollectionReaderFactory.createReaderDescription(
                TuebazChunkV9Reader.class, TuebazChunkV9Reader.PARAM_LANGUAGE, "de",
                TuebazChunkV9Reader.PARAM_LANGUAGE, "de",
                TuebazChunkV9Reader.PARAM_SOURCE_ENCODING, "ISO-8859-1", 
                TuebazChunkV9Reader.PARAM_SOURCE_LOCATION, dataFolder,
                TuebazChunkV9Reader.PARAM_PATTERNS, new String[] { "*.chunk" });

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                RitterFormatWriter.class,  
                RitterFormatWriter.PARAM_TARGET_LOCATION, output + "/tuebazv9.data");

        SimplePipeline.runPipeline(corpus, writer);
    }
}
