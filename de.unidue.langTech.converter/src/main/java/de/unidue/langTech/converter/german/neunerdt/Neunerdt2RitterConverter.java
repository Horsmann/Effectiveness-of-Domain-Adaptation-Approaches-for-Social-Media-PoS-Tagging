package de.unidue.langTech.converter.german.neunerdt;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.RitterFormatWriter;

public class Neunerdt2RitterConverter
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
        else {
            System.err.println("No path provided");
            return;
        }

        CollectionReaderDescription tiger = CollectionReaderFactory.createReaderDescription(
                Neunerdt2013HeiseCorpusReader.class, Neunerdt2013HeiseCorpusReader.PARAM_LANGUAGE,
                "de", Neunerdt2013HeiseCorpusReader.PARAM_SOURCE_LOCATION, dataFolder,
                Neunerdt2013HeiseCorpusReader.PARAM_PATTERNS, new String[] { "*.txt" });

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                RitterFormatWriter.class, RitterFormatWriter.PARAM_TARGET_LOCATION, output + "/"
                        + outputFileName);

        SimplePipeline.runPipeline(tiger, writer);
    }
}
