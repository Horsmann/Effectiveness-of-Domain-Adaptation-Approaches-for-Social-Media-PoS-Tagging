package de.unidue.langTech.cfd2json.cfdJSonizer;

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
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.langTech.util.FeatureUtil;

public class CondFreqDis2Json extends JCasAnnotator_ImplBase {
	public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
	@ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
	private String targetLocation;

	public static final String PARAM_ENCODING = "PARAM_ENCODING";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	private static ConditionalFrequencyDistribution<String, String> cfd = new ConditionalFrequencyDistribution<String, String>();

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		for (Token t : tokens) {
			String token = t.getCoveredText();
			if (!FeatureUtil.isTrash(token)) {
				cfd.addSample(token.toLowerCase(), t.getPos().getPosValue(), 1);
			}
		}
	}

	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {

		Gson gson = new Gson();
		String jsonedCfd = gson.toJson(cfd);

		try {
			FileUtils.writeStringToFile(new File(targetLocation), jsonedCfd,
					encoding);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
}
