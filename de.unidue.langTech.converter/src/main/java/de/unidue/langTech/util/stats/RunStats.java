package de.unidue.langTech.util.stats;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreebankChunkedReader;
import de.unidue.langTech.converter.penn.wsj.WallStreet2RitterConvert;
import de.unidue.langTech.pos.ritter.Ritter2011TweetCorpusReader;

public class RunStats {

	public static void main(String[] args) throws Exception

	{

		CollectionReaderDescription reader = CollectionReaderFactory
				.createReaderDescription(Ritter2011TweetCorpusReader.class,
						Ritter2011TweetCorpusReader.PARAM_LANGUAGE, "en",
						Ritter2011TweetCorpusReader.PARAM_SOURCE_LOCATION,
						args[0], Ritter2011TweetCorpusReader.PARAM_PATTERNS,
						new String[] { "*.txt", "*.data.gz" });

		AnalysisEngineDescription writer = AnalysisEngineFactory
				.createEngineDescription(CorpusStatistics.class);

		SimplePipeline.runPipeline(reader, writer);
	}
}