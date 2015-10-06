package de.unidue.langTech.converter.penn.wsj;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.RitterFormatWriter;
import de.unidue.langTech.converter.penn.PennTreebankChunkedReader;

public class WallStreet2RitterConvert
{

    public static void main(String[] args)
        throws Exception

    {
        String dataFolder = "";
        String outputFolder = System.getProperty("user.home") + "/Desktop";
        if (args.length == 1) {
            dataFolder = args[0];
        }
        else {
            System.err.println("No path provided");
            return;
        }

        CollectionReaderDescription wsj = CollectionReaderFactory.createReaderDescription(
                PennTreebankChunkedReader.class, PennTreebankChunkedReader.PARAM_LANGUAGE, "en",
                PennTreebankChunkedReader.PARAM_SOURCE_LOCATION, dataFolder,
                PennTreebankChunkedReader.PARAM_PATTERNS, new String[] { "*.pos" });

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                RitterFormatWriter.class, RitterFormatWriter.PARAM_TARGET_LOCATION, outputFolder
                        + "/" + "wsj.data");

        SimplePipeline.runPipeline(wsj, writer);
    }
}
