package de.unidue.langTech.converter.german.tiger;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.RitterFormatWriter;

public class Tiger2RitterConverter
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

        CollectionReaderDescription tiger = CollectionReaderFactory.createReaderDescription(
                SimpleXMLTigerCorpusReader.class, SimpleXMLTigerCorpusReader.PARAM_LANGUAGE, "de",
                SimpleXMLTigerCorpusReader.PARAM_LANGUAGE, "de",
                SimpleXMLTigerCorpusReader.PARAM_SOURCE_ENCODING, "ISO-8859-1", 
                SimpleXMLTigerCorpusReader.PARAM_SOURCE_LOCATION, dataFolder,
                SimpleXMLTigerCorpusReader.PARAM_PATTERNS, new String[] { "*.xml" });

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                RitterFormatWriter.class,  
                RitterFormatWriter.PARAM_TARGET_LOCATION, output + "/tiger.data");

        SimplePipeline.runPipeline(tiger, writer);
    }
}
