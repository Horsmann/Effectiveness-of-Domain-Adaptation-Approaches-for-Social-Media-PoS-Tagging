package de.unidue.langTech.cfd2json.cfdJSonizer.verbdict;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class WordEndingDictionaryCFDWriter extends JCasAnnotator_ImplBase {

	public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
	@ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
	private String targetLocation;

	static ConditionalFrequencyDistribution<String, String> vbCfd = new ConditionalFrequencyDistribution<String, String>();

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		for (Token t : tokens) {
			POS pos = t.getPos();
			if (pos == null) {
				continue;
			}
			String tokenText = t.getCoveredText();
			if (tokenText.length() <= 3) {
				continue;
			}
			String threeLastChar = tokenText.substring(tokenText.length() - 3);
			String twoLastChar = tokenText.substring(tokenText.length() - 2);
			vbCfd.addSample(threeLastChar, pos.getPosValue(), 1);
			vbCfd.addSample(twoLastChar, pos.getPosValue(), 1);
		}
	}

	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		Gson gson = new Gson();
		String jsonedCfd = gson.toJson(vbCfd);

		try {
			FileUtils.writeStringToFile(new File(targetLocation), jsonedCfd,
					"UTF-8");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
}
