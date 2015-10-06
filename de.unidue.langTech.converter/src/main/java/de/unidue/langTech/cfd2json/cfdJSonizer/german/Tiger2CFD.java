package de.unidue.langTech.cfd2json.cfdJSonizer.german;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.cfd2json.cfdJSonizer.CondFreqDis2Json;
import de.unidue.langTech.converter.german.tiger.SimpleXMLTigerCorpusReader;

public class Tiger2CFD
{
    public static void main(String[] args) throws Exception

    {
        String dataFolder = null;
        if (args.length == 1) {
            dataFolder = args[0];
        }
        String output = System.getProperty("user.home")
                + "/Desktop/tiger2cfd.json";

        CollectionReaderDescription wsj = CollectionReaderFactory
                .createReaderDescription(SimpleXMLTigerCorpusReader.class,
                        SimpleXMLTigerCorpusReader.PARAM_LANGUAGE, "de",
                        SimpleXMLTigerCorpusReader.PARAM_SOURCE_LOCATION,
                        dataFolder, SimpleXMLTigerCorpusReader.PARAM_PATTERNS,
                        new String[] { "*.xml" });

        AnalysisEngineDescription writer = AnalysisEngineFactory
                .createEngineDescription(CondFreqDis2Json.class,
                        CondFreqDis2Json.PARAM_TARGET_LOCATION, output);

        SimplePipeline.runPipeline(wsj, writer);
    }
}
