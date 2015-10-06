package de.unidue.langTech.cfd2json.cfdJSonizer;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.wacky.XWackyCorpusReader;

public class XWacky2CFD {
	public static void main(String[] args) throws Exception

	{
		String dataFolder = null;
		String fileSuffix = null;
		String posMapping = null;
		String outFile = null;
			dataFolder = args[0];
			fileSuffix = args[1];
			posMapping = args[2];
			outFile = args[3];
			
		String output = System.getProperty("user.home") + "/Desktop/" + outFile;

		CollectionReaderDescription dewac = CollectionReaderFactory
				.createReaderDescription(XWackyCorpusReader.class,
						XWackyCorpusReader.PARAM_LANGUAGE, "de",
						XWackyCorpusReader.PARAM_READ_LEMMA, false,
						XWackyCorpusReader.PARAM_POS_MAPPING_LOCATION, posMapping,
						XWackyCorpusReader.PARAM_SOURCE_LOCATION, dataFolder,
						XWackyCorpusReader.PARAM_PATTERNS, new String[] { "*."
								+ fileSuffix });

		AnalysisEngineDescription writer = AnalysisEngineFactory
				.createEngineDescription(CondFreqDis2Json.class,
						CondFreqDis2Json.PARAM_TARGET_LOCATION, output);

		SimplePipeline.runPipeline(dewac, writer);
	}
}
