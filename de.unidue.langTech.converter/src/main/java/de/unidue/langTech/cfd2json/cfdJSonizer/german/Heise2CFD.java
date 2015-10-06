package de.unidue.langTech.cfd2json.cfdJSonizer.german;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.cfd2json.cfdJSonizer.CondFreqDis2Json;
import de.unidue.langTech.converter.german.neunerdt.Neunerdt2013HeiseCorpusReader;
import de.unidue.langTech.converter.german.tiger.SimpleXMLTigerCorpusReader;
import de.unidue.langTech.pos.ritter.Ritter2011TweetCorpusReader;

public class Heise2CFD
{
    public static void main(String[] args) throws Exception

    {
        String dataFolder = null;
        if (args.length == 1) {
            dataFolder = args[0];
        }
        String output = System.getProperty("user.home")
                + "/Desktop/heise2cfd.json";

        // Wir lesen die Trainingsdaten! (ohne Testdaten)
        CollectionReaderDescription reader = CollectionReaderFactory
                .createReaderDescription(Ritter2011TweetCorpusReader.class,
                        Ritter2011TweetCorpusReader.PARAM_LANGUAGE, "de",
                        Ritter2011TweetCorpusReader.PARAM_SOURCE_LOCATION,
                        dataFolder, Ritter2011TweetCorpusReader.PARAM_PATTERNS,
                        new String[] { "*.train" });

        AnalysisEngineDescription writer = AnalysisEngineFactory
                .createEngineDescription(CondFreqDis2Json.class,
                        CondFreqDis2Json.PARAM_TARGET_LOCATION, output);

        SimplePipeline.runPipeline(reader, writer);
    }
}
