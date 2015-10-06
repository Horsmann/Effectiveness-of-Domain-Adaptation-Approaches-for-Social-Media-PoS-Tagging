package de.unidue.langTech.reports;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

public class CoarseWordClassPerformanceReport extends ReportBase {
	public static final String OUTPUT_FILE = "coarseMappedPerformance.txt";
	public static String posMappingLocation = null;
	public static String language = null;

	MappingProvider posMappingProvider = null;
	private static String posTagset = null;

	public void execute() throws Exception {
		parameterTest();

		init();

		File storage = getContext().getStorageLocation(
				CRFSuiteTestTask.TEST_TASK_OUTPUT_KEY, AccessMode.READONLY);
		File predictionFile = new File(storage.getAbsolutePath()
				+ "/"
				+ CRFSuiteAdapter.getInstance().getFrameworkFilename(
						AdapterNameEntries.predictionsFile));

		List<String> readLines = (FileUtils.readLines(predictionFile));

		List<Result> results = calculateAccuracyOnCoarseTags(readLines);
		write2File(results);
	}

	private void write2File(List<Result> aResults) throws Exception {
		StringBuilder sb = new StringBuilder();

		String f = String
				.format("#%-5s\t%-10s\t%-10s\t\n", "Label", "Total-Occ", "Correct-Acc");
		sb.append(f);

		for (Result r : aResults) {
			f = String.format("%5s\t%10d\t%10.3f\t\n", r.label, r.frequency,
					r.accuracy);
			sb.append(f);
		}

		File storage = getContext().getStorageLocation(
				CRFSuiteTestTask.TEST_TASK_OUTPUT_KEY, AccessMode.READONLY);
		File outputFile = new File(storage.getAbsolutePath() + "/"
				+ OUTPUT_FILE);
		FileUtils.writeStringToFile(outputFile, sb.toString(), "UTF-8");

	}

	private void parameterTest() throws Exception {
		if (language == null) {
			throw new Exception("Language Code in report not set!");
		}
		if (posMappingLocation == null) {
			throw new Exception("Pos Mapping not set!");
		}

	}

	private void init() throws Exception {
		posMappingProvider = new MappingProvider();
		posMappingProvider
				.setDefault(
						MappingProvider.LOCATION,
						"classpath:/de/tudarmstadt/ukp/dkpro/"
								+ "core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
		posMappingProvider.setDefault(MappingProvider.BASE_TYPE,
				POS.class.getName());
		posMappingProvider.setDefault("tagger.tagset", "default");
		posMappingProvider.setOverride(MappingProvider.LOCATION,
				posMappingLocation);
		posMappingProvider.setOverride(MappingProvider.LANGUAGE, language);
		posMappingProvider.setOverride("tagger.tagset", posTagset );
		JCas jcas = JCasFactory.createJCas();
		posMappingProvider.configure(jcas.getCas());
	}

	private List<Result> calculateAccuracyOnCoarseTags(List<String> aReadLines)
			throws Exception {
		HashMap<String, Double> correct = new HashMap<String, Double>();
		HashMap<String, Double> wrong = new HashMap<String, Double>();
		for (String line : aReadLines) {
			if (line.startsWith("#")) {
				continue;
			}
			if (line.isEmpty()) {
				continue;
			}

			String[] split = line.split("\t");
			if (split.length < 2) {
				split = new String[2];
				split[0] = "ERROR_GOLD";
				split[1] = "ERROR_ACTUAL";
			}

			JCas dummyCas = JCasFactory.createJCas();
			Type rawExptected = posMappingProvider.getTagType(split[0]);
			POS expected = (POS) dummyCas.getCas().createAnnotation(
					rawExptected, 0, 1);

			Type rawActual = posMappingProvider.getTagType(split[1]);
			POS actual = (POS) dummyCas.getCas().createAnnotation(rawActual, 0,
					1);

			String sCoarseExpected = expected.getClass().getSimpleName();
			String sCoarseActual = actual.getClass().getSimpleName();

			if (sCoarseActual.equals(sCoarseExpected)) {
				Double val = correct.get(sCoarseExpected);
				if (val == null) {
					val = 1.0;
				} else {
					val++;
				}
				correct.put(sCoarseExpected, val);

			} else {
				Double val = wrong.get(sCoarseExpected);
				if (val == null) {
					val = 1.0;
				} else {
					val++;
				}
				wrong.put(sCoarseExpected, val);
			}

		}

		//
		List<Result> results = new ArrayList<Result>();
		Set<String> labels = new HashSet<String>();
		labels.addAll(correct.keySet());
		labels.addAll(wrong.keySet());

		String[] sortedLabels = ReportUtil.sortByName(labels);

		for (String label : sortedLabels) {
			Double c = correct.get(label);
			Double w = wrong.get(label);

			if (c == null) {
				c = 0.0;
			}
			if (w == null) {
				w = 0.0;
			}

			double accuracy = c / (c + w);
			Result r = new Result(label, new Double(c + w).intValue(), accuracy);
			results.add(r);
		}

		return results;

	}


	class Result {
		double accuracy;
		int frequency;
		String label;

		Result(String label, int frequency, double accuracy) {
			this.label = label;
			this.frequency = frequency;
			this.accuracy = accuracy;

		}
	}

}
