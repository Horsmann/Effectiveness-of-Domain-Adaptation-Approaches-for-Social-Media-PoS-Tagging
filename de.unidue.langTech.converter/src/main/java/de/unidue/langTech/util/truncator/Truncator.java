package de.unidue.langTech.util.truncator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class Truncator extends JCasAnnotator_ImplBase {
	public static final String PARAM_TOKEN_LIMIT = "PARAM_TOKEN_LIMIT";
	@ConfigurationParameter(name = PARAM_TOKEN_LIMIT, mandatory = true)
	protected int tokenLimit;

	public static final String PARAM_TARGET_LOCATION = "PARAM_TARGET_LOCATION";
	@ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
	private String target;

	public static final String PARAM_ENCODING = "PARAM_ENCODING";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	static int tokenCount = 0;
	static BufferedWriter bw = null;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		try {
			init();

			if (tokenCount > tokenLimit) {
				return;
			}

			Collection<Sentence> sentences = JCasUtil.select(aJCas,
					Sentence.class);
			for (Sentence s : sentences) {
				List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class,
						s.getBegin(), s.getEnd());
				for (Token t : tokens) {
					String tokenText = t.getCoveredText();
					String posText = t.getPos().getPosValue();
					bw.write(tokenText + " " + posText);
					bw.write("\n");
				}
				bw.write("\n");

				tokenCount += tokens.size();
				if (tokenCount > tokenLimit) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() throws Exception {
		if (bw == null) {
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(target)), encoding));
		}

	}

	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("File was truncated to: " + tokenCount + " tokens");
	}

}
