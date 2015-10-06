package de.unidue.langTech.cfd2json.cfdJSonizer.english;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.cfd2json.cfdJSonizer.CondFreqDis2Json;
import de.unidue.langTech.converter.penn.PennTreebankChunkedReader;

public class WSJ2CFD {

	public static void main(String[] args) throws Exception

	{
		String dataFolder = "src/main/resources/WSJ/";
		if (args.length == 1) {
			dataFolder = args[0] + "";
		}
		String output = System.getProperty("user.home")
				+ "/Desktop/wsj2cfd.json";

		CollectionReaderDescription wsj = CollectionReaderFactory
				.createReaderDescription(PennTreebankChunkedReader.class,
						PennTreebankChunkedReader.PARAM_LANGUAGE, "en",
						PennTreebankChunkedReader.PARAM_SOURCE_LOCATION,
						dataFolder, PennTreebankChunkedReader.PARAM_PATTERNS,
						new String[] { "*.pos" });

		AnalysisEngineDescription writer = AnalysisEngineFactory
				.createEngineDescription(CondFreqDis2Json.class,
						CondFreqDis2Json.PARAM_TARGET_LOCATION, output);

		SimplePipeline.runPipeline(wsj, writer);
	}
}