package de.unidue.langTech.converter.wacky;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.RitterFormatWriter;

public class XWacky2RitterConverter
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
        if (args.length >= 2) {
            outputFileName = args[1];
        }
        if (args.length == 0){
            System.err.println("No path provided");
            return;
        }

        CollectionReaderDescription tiger = CollectionReaderFactory.createReaderDescription(
                XWackyCorpusReader.class, XWackyCorpusReader.PARAM_LANGUAGE, "de",
                XWackyCorpusReader.PARAM_SOURCE_LOCATION, dataFolder,
                XWackyCorpusReader.PARAM_PATTERNS, new String[] { "*.dewac" });

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                RitterFormatWriter.class, RitterFormatWriter.PARAM_TARGET_LOCATION, output + "/"
                        + outputFileName);

        SimplePipeline.runPipeline(tiger, writer);
    }
}
