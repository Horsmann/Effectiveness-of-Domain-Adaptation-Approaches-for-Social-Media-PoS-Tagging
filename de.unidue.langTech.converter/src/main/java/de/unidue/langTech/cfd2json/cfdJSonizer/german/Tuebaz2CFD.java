package de.unidue.langTech.cfd2json.cfdJSonizer.german;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.cfd2json.cfdJSonizer.CondFreqDis2Json;
import de.unidue.langTech.converter.german.tuebaz.TuebazChunkV9Reader;

public class Tuebaz2CFD
{
    public static void main(String[] args)
        throws Exception

    {
        String dataFolder = null;
        if (args.length == 1) {
            dataFolder = args[0];
        }
        String output = System.getProperty("user.home") + "/Desktop/tuebaz2cfd.json";

        CollectionReaderDescription wsj = CollectionReaderFactory.createReaderDescription(
                TuebazChunkV9Reader.class, TuebazChunkV9Reader.PARAM_LANGUAGE, "de",
                TuebazChunkV9Reader.PARAM_SOURCE_ENCODING, "ISO-8859-1",
                TuebazChunkV9Reader.PARAM_SOURCE_LOCATION, dataFolder,
                TuebazChunkV9Reader.PARAM_PATTERNS, new String[] { "*.gz" });

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                CondFreqDis2Json.class, CondFreqDis2Json.PARAM_TARGET_LOCATION, output);

        SimplePipeline.runPipeline(wsj, writer);
    }
}
