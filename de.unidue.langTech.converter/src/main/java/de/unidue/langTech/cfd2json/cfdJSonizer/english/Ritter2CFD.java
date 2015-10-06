package de.unidue.langTech.cfd2json.cfdJSonizer.english;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.cfd2json.cfdJSonizer.CondFreqDis2Json;
import de.unidue.langTech.pos.ritter.Ritter2011TweetCorpusReader;

public class Ritter2CFD {

	public static void main(String[] args) throws Exception

	{
		String dataFolder = args[0];
		String outputFileName = args[1];

		String output = System.getProperty("user.home") + "/Desktop/"
				+ outputFileName;

		CollectionReaderDescription wsj = CollectionReaderFactory
				.createReaderDescription(Ritter2011TweetCorpusReader.class,
						Ritter2011TweetCorpusReader.PARAM_LANGUAGE, "en",
						Ritter2011TweetCorpusReader.PARAM_SOURCE_LOCATION,
						dataFolder,
						Ritter2011TweetCorpusReader.PARAM_PATTERNS,
						new String[] { "*.data" });

		AnalysisEngineDescription writer = AnalysisEngineFactory
				.createEngineDescription(CondFreqDis2Json.class,
						CondFreqDis2Json.PARAM_TARGET_LOCATION, output);

		SimplePipeline.runPipeline(wsj, writer);
	}
}