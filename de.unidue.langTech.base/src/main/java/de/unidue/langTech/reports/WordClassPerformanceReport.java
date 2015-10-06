package de.unidue.langTech.reports;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

public class WordClassPerformanceReport extends ReportBase {
	public static final String OUTPUT_FILE_FREQUENCY = "perClassPerformanceFrequency.txt";
	public static final String OUTPUT_FILE_PERCENT = "perClassPerformanceRatio.txt";
	public static String mappingPosLocation = null;
	public static String posTagset = null;
	public static String language = null;
	public static String posMappingLocation = null;

	MappingProvider posMappingProvider = null;

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

		HashMap<String, Distribution> wc = createInterIntraResults(readLines);
		String createdReport = createFrequencyReport(wc);
		writeFrequencies2File(createdReport);

		String ratioReport = createRatioReport(wc);
		writeRatio2File(ratioReport);
	}

	private void writeRatio2File(String aRatioReport) throws IOException {
		File storage = getContext().getStorageLocation(
				CRFSuiteTestTask.TEST_TASK_OUTPUT_KEY, AccessMode.READONLY);
		File outputFile = new File(storage.getAbsolutePath() + "/"
				+ OUTPUT_FILE_PERCENT);
		FileUtils.writeStringToFile(outputFile, aRatioReport, "UTF-8");

	}

	private String createRatioReport(HashMap<String, Distribution> wc) {
		StringBuilder sb = new StringBuilder();
		sb.append(setRatioHeadline());
		String[] sortedKeys = sortLabelsByName(wc.keySet());
		for (String key : sortedKeys) {

			Distribution distribution = wc.get(key);

			double fine = calculateFineRatio(distribution);
			String f = String.format("%15s\t%15.3f\n", key, fine);
			sb.append(f);
		}

		return sb.toString();
	}

	private String setRatioHeadline() {
		String f = null;
		f = String.format("#%15s\t%15s\n", "", "Fine-Acc");

		return f;
	}

	private double calculateFineRatio(Distribution distribution) {
		double fine = (double) distribution.fineClassCorrect
				/ (distribution.fineClassCorrect + distribution.foreignClass);

		return fine;
	}

	private void parameterTest() throws Exception {
		if (language == null) {
			throw new Exception("Language Code in report not set!");
		}
		if (posMappingLocation == null) {
			throw new Exception("PosMappingLocation in report not set!");
		}
	}

	private void writeFrequencies2File(String aCreatedReport) throws Exception {
		File storage = getContext().getStorageLocation(
				CRFSuiteTestTask.TEST_TASK_OUTPUT_KEY, AccessMode.READONLY);
		File outputFile = new File(storage.getAbsolutePath() + "/"
				+ OUTPUT_FILE_FREQUENCY);
		FileUtils.writeStringToFile(outputFile, aCreatedReport, "UTF-8");

	}

	private String createFrequencyReport(HashMap<String, Distribution> aWc) {
		StringBuilder sb = new StringBuilder();

		// Headline
		sb.append(setHeadline());
		String[] sortedKeys = sortLabelsByName(aWc.keySet());
		for (String key : sortedKeys) {
			Distribution distribution = aWc.get(key);
			String f;
			f = makeFineLine(key, distribution);

			sb.append(f);
		}

		return sb.toString();
	}

	private String[] sortLabelsByName(Set<String> unsortedLabels) {
		String[] labels = unsortedLabels.toArray(new String[0]);
		Arrays.sort(labels, new Comparator<String>() {

			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		return labels;
	}

	private String makeFineLine(String key, Distribution distribution) {
		Integer fine = distribution.fineClassCorrect;
		Integer wrong = distribution.foreignClass;

		return String.format("%10s\t%15s\t%15s\n", key, numFormat(fine),
				numFormat(wrong));
	}

	private String numFormat(Integer num) {
		String value = num.toString().replaceAll("^0+", "");
		if (value.isEmpty()) {
			value = "0";
		}

		return value;
	}

	private String setHeadline() {
		String f = String.format("#%10s\t%15s\t%15s\n", "", "Correct class",
				"Wrong class");
		return f;
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
				mappingPosLocation);
		posMappingProvider.setOverride(MappingProvider.LANGUAGE, language);
		posMappingProvider.setOverride("tagger.tagset", posTagset);
		JCas jcas = JCasFactory.createJCas();
		posMappingProvider.configure(jcas.getCas());
	}

	private HashMap<String, Distribution> createInterIntraResults(
			List<String> aReadLines) throws Exception {
		HashMap<String, Distribution> wc = new HashMap<String, Distribution>();

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

			String sFineExpected = split[0];
			String sFineActual = split[1];

			Distribution distribution = wc.get(sFineExpected);
			if (distribution == null) {
				distribution = new Distribution();
			}

			distribution = processFine(distribution, sFineExpected, sFineActual);

			wc.put(sFineExpected, distribution);

		}

		return wc;
	}

	private Distribution processFine(Distribution distribution,
			String sFineExpected, String sFineActual) {
		if (sFineExpected.equals(sFineActual)) {
			distribution.fineClassCorrect++;
		} else {
			distribution.foreignClass++;
		}
		return distribution;
	}

	class Distribution {
		Integer fineClassCorrect = new Integer(0);
		Integer foreignClass = new Integer(0);
	}

}
