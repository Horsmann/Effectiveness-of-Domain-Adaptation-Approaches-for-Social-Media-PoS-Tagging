package de.unidue.langTech.converter.npsChat;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.RitterFormatWriter;

public class NPS2RitterConverter
{

    public static void main(String[] args)
        throws Exception

    {
    	
    	String outputFolder = args[0];

        CollectionReaderDescription nps = CollectionReaderFactory.createReaderDescription(
                NPSIRCCorpusReader.class, NPSIRCCorpusReader.PARAM_LANGUAGE, "en",
                NPSIRCCorpusReader.PARAM_SOURCE_LOCATION, "/Users/Tobias/Documents/gscl/corpora/english/npsChat/",
                NPSIRCCorpusReader.PARAM_PATTERNS, new String[] { "*.xml" });

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                 RitterFormatWriter.class, RitterFormatWriter.PARAM_TARGET_LOCATION,
                outputFolder + "/nps.train");

        SimplePipeline.runPipeline(nps, writer);
    }
}