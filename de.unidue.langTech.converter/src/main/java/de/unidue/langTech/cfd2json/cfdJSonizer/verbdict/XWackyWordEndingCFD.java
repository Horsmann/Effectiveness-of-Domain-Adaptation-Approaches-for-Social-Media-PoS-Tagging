package de.unidue.langTech.cfd2json.cfdJSonizer.verbdict;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.langTech.converter.wacky.XWackyCorpusReader;

public class XWackyWordEndingCFD {
	public static void main(String[] args) throws Exception

	{
		String dataFolder = null;
		String fileSuffix = null;
		String outFile = null;
		if (args.length == 3) {
			dataFolder = args[0];
			fileSuffix = args[1];
			outFile = args[2];
		}
		String output = System.getProperty("user.home") + "/Desktop/" + outFile;

		CollectionReaderDescription dewac = CollectionReaderFactory
				.createReaderDescription(XWackyCorpusReader.class,
						XWackyCorpusReader.PARAM_LANGUAGE, "de",
						XWackyCorpusReader.PARAM_READ_LEMMA, false,
						XWackyCorpusReader.PARAM_SOURCE_LOCATION, dataFolder,
						XWackyCorpusReader.PARAM_PATTERNS, new String[] { "*."
								+ fileSuffix });

		AnalysisEngineDescription writer = AnalysisEngineFactory
				.createEngineDescription(WordEndingDictionaryCFDWriter.class,
						WordEndingDictionaryCFDWriter.PARAM_TARGET_LOCATION, output);

		SimplePipeline.runPipeline(dewac, writer);
	}
}
