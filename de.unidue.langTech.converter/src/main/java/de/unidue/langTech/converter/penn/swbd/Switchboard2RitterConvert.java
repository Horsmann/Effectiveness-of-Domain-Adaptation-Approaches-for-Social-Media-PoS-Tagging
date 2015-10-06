package de.unidue.langTech.converter.penn.swbd;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.RitterFormatWriter;
import de.unidue.langTech.converter.penn.PennTreebankChunkedReader;

public class Switchboard2RitterConvert {

	public static void main(String[] args) throws Exception

	{

		CollectionReaderDescription wsj = CollectionReaderFactory
				.createReaderDescription(PennTreebankChunkedReader.class,
						PennTreebankChunkedReader.PARAM_LANGUAGE, "en",
						PennTreebankChunkedReader.PARAM_SOURCE_LOCATION,
						"src/main/resources/swbd/**/*",
						PennTreebankChunkedReader.PARAM_PATTERNS,
						new String[] { "*.pos" });

		AnalysisEngineDescription writer = AnalysisEngineFactory
				.createEngineDescription(RitterFormatWriter.class,
						RitterFormatWriter.PARAM_TARGET_LOCATION,
						"/Users/Tobias/Desktop/swbd.train");

		SimplePipeline.runPipeline(wsj, writer);
	}
}