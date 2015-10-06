package de.unidue.langTech.converter.brownTei;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.unidue.langTech.converter.RitterFormatWriter;

public class Brown2RitterConverter {

	public static void main(String[] args) throws Exception

	{
		
		String folder = args[0];

	    CollectionReaderDescription brown = CollectionReaderFactory
                .createReaderDescription(TeiReader.class,
                		TeiReader.PARAM_LANGUAGE, "en",
                		TeiReader.PARAM_SOURCE_LOCATION,
                        folder,
                        TeiReader.PARAM_PATTERNS,
                        new String[] { "*.xml" });

	    String output = System.getProperty("user.home") + "/Desktop/" + "brown.data";
		AnalysisEngineDescription writer = AnalysisEngineFactory
				.createEngineDescription(RitterFormatWriter.class,
						RitterFormatWriter.PARAM_TARGET_LOCATION,
						output);

		SimplePipeline.runPipeline(brown, writer);
	}
}