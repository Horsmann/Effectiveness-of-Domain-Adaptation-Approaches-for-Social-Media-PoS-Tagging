package de.unidue.langTech.reports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.CRFSuiteTestTask;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentCrossValidation;

public class AccuracyOnUnknownWordsReport extends BatchReportBase implements
		Constants {

	static AverageOOV average = new AverageOOV();

	static String OOV_AVG_CROSSVALIDATIION_STAT_FILE = "outOfVocabularyAveragedPerformance.txt";
	static String OOV_STAT_FILE = "outOfVocabularyPerformance.txt";

	static Set<String> training;
	static HashMap<Integer, List<String>> oovWithLineNumber;
	static HashMap<Integer, String> prediction;

	static String featureFile = null;
	static String predictionFile = null;
	{
		TCMachineLearningAdapter adapter = CRFSuiteAdapter.getInstance();
		featureFile = adapter
				.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
		predictionFile = adapter
				.getFrameworkFilename(AdapterNameEntries.predictionsFile);
	}

	public void execute() throws Exception {
		// TODO lower-case?
		StorageService store = getContext().getStorageService();

		boolean isCrossValidation = false;

		for (TaskContextMetadata subcontext : getSubtasks()) {
			if (subcontext.getType().contains(ExperimentCrossValidation.class.getName())) {
				isCrossValidation = true;
			}
		}

		if (isCrossValidation) {
			doCrossValidation(store);
			outputAggregatedReportOverAllFolds(store);
		} else {
			doTrainTest(store);
		}

	}

	private void outputAggregatedReportOverAllFolds(StorageService store)
			throws Exception {

		String outputContext = null;
		for (TaskContextMetadata subcontext : getSubtasks()) {
			if (subcontext.getType().contains("BatchTaskCrossValidation")) {
				outputContext = subcontext.getId();
				break;
			}
		}

		double result = average.getAveragedValueForKey(AverageOOV.TOTAL);
		StringBuilder sb = new StringBuilder();

		sb.append("AVG OOV-Acc: " + String.format("%5.3f", result) + "\n\n");
		sb.append(String.format("%10s\t%5s\n", "Label", "Correct"));

		for (String key : average.getKeys()) {
			if (key.equals(AverageOOV.TOTAL)) {
				continue;
			}
			double avgLabel = average.getAveragedValueForKey(key);
			sb.append(String.format("%10s\t%5.3f\n", key, avgLabel));

		}

		String storageLocation = store.getStorageFolder(outputContext, "")
				.getAbsolutePath() + "/" + OOV_AVG_CROSSVALIDATIION_STAT_FILE;

		FileUtils.writeStringToFile(new File(storageLocation), sb.toString());

	}

	private void doCrossValidation(StorageService store) throws Exception {
		for (TaskContextMetadata subcontext : getSubtasks()) {
			if (subcontext.getType().contains("BatchTaskCrossValidation")) {
				File propertiesTXT = store.getStorageFolder(subcontext.getId(),
						"/" + "PROPERTIES.TXT");
				List<List<String>> sets = getFoldersOfSingleRuns(propertiesTXT);
				for (List<String> set : sets) {
					File train = buildFileLocation(store, set.get(0),
							TEST_TASK_OUTPUT_KEY + "/" + featureFile);
					extractTrainingVocab(train);
					File test = buildFileLocation(store, set.get(1),
							TEST_TASK_OUTPUT_KEY + "/" + featureFile);
					getOOV(test);
					File prediction = buildFileLocation(store, set.get(2),
							TEST_TASK_OUTPUT_KEY + "/" + predictionFile);
					getPredictionsFromFile(prediction);
					evaluateOOVErrorRate(store, set.get(2) + "/"
							+ TEST_TASK_OUTPUT_KEY);
				}
			}
		}
	}

	private File buildFileLocation(StorageService store, String context,
			String fileName) {
		String path = store.getStorageFolder(context, "").getAbsolutePath()
				+ "/" + fileName;
		return new File(path);
	}

	private List<List<String>> getFoldersOfSingleRuns(File propertiesTXT)
			throws Exception {
		List<String> readLines = FileUtils.readLines(propertiesTXT);

		int idx = 0;
		for (String line : readLines) {
			if (line.startsWith("#")) {
				idx++;
				continue;
			}
			break;
		}
		String line = readLines.get(idx);
		int start = line.indexOf("[");
		int end = line.indexOf("]");
		String subTasks = line.substring(start, end);

		String[] tasks = subTasks.split(",");

		List<List<String>> sets = new ArrayList<List<String>>();

		String train = "", test = "", main = "";
		for (String task : tasks) {
			if (task.contains("ExtractFeaturesTask-Train")) {
				train = task;
			}
			if (task.contains("ExtractFeaturesTask-Test")) {
				test = task;
			}
			if (task.contains("CRFSuiteTestTask")) {
				main = task;
			}
			if (!train.isEmpty() && !test.isEmpty() && !main.isEmpty()) {
				List<String> list = new ArrayList<String>();
				list.add(train.trim());
				list.add(test.trim());
				list.add(main.trim());

				sets.add(list);
				train = test = main = "";
			}
		}

		return sets;
	}

	private void doTrainTest(StorageService store) throws Exception {

		File train = null, test = null, predictions = null;
		String outputFolderId = null;
		for (TaskContextMetadata subcontext : getSubtasks()) {
			if (subcontext.getType().contains("ExtractFeaturesTask-Train")) {
				train = store.getStorageFolder(subcontext.getId(),
						TEST_TASK_OUTPUT_KEY + "/" + featureFile);
				extractTrainingVocab(train);
			}
			if (subcontext.getType().contains("ExtractFeaturesTask-Test")) {
				test = store.getStorageFolder(subcontext.getId(),
						TEST_TASK_OUTPUT_KEY + "/" + featureFile);
				getOOV(test);
			}

			if (subcontext.getType().contains(CRFSuiteTestTask.class.getName())) {
				outputFolderId = subcontext.getId();
				predictions = store.getStorageFolder(subcontext.getId(),
						TEST_TASK_OUTPUT_KEY + "/" + predictionFile);

				getPredictionsFromFile(predictions);
			}
		}
		evaluateOOVErrorRate(store, outputFolderId + "/" + TEST_TASK_OUTPUT_KEY);
	}

	private void getPredictionsFromFile(File predictions) throws Exception {
		prediction = new HashMap<Integer, String>();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(predictions), "UTF-8"));

		int i = 0;
		String line = null;
		while ((line = br.readLine()) != null) {

			if (i == 0) {
				i++;
				continue;
			}
			i++;

			if (line.isEmpty()) {
				continue;
			}
			prediction.put(i - 1, line);
		}
		br.close();
	}

	private void getOOV(File test) throws Exception {
		oovWithLineNumber = new HashMap<Integer, List<String>>();

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(test), "UTF-8"));

		int i = 0;
		String line = null;
		while ((line = br.readLine()) != null) {

			if (i == 0) {
				i++;
				continue;
			}
			i++;

			if (line.isEmpty()) {
				continue;
			}
			String tag = extractTag(line);
			String extractUnit = extractUnit(line);

			List<String> oov = new ArrayList<String>();
			oov.add(extractUnit);
			oov.add(tag);
			if (training.contains(extractUnit)) {
				continue;
			}
			oovWithLineNumber.put(i, oov);
		}
		br.close();
	}

	private void evaluateOOVErrorRate(StorageService store, String outputContext)
			throws Exception {
		double correct = 0;
		double incorrect = 0;
		ConditionalFrequencyDistribution<String, String> cfd = new ConditionalFrequencyDistribution<String, String>();

		for (int key : oovWithLineNumber.keySet()) {
			String string = prediction.get(key);
			if (string == null) {
				incorrect++;
				continue;
			}
			String[] split = string.split("\t");
			if (split.length < 2) {
				continue;
			}

			if (split[0].equals(split[1])) {
				correct++;
			} else {
				incorrect++;
			}

			cfd.addSample(split[0], split[1], 1);
		}

		String storageLocation = store.getStorageFolder(outputContext, "")
				.getAbsolutePath() + "/" + OOV_STAT_FILE;

		StringBuilder sb = new StringBuilder();
		double accuracy = (correct / (correct + incorrect));
		sb.append("OOV Accuracy: " + accuracy + "%\n\n");
		sb.append(String.format("%10s\t%5s\t%12s\n", "Label", "Correct",
				"Worst-Conf"));
		for (String key : cfd.getConditions()) {
			FrequencyDistribution<String> fd = cfd
					.getFrequencyDistribution(key);
			double accPerLabel = (double) fd.getCount(key) / fd.getN();

			// In case this is cross validation we keep track of each
			// iteration's result here
			average.add(key, accPerLabel);

			String worstConfusion = getClassThatWasMostOftenChosenWronglyForLabel(
					fd, key);

			sb.append(String.format("%10s\t%5.2f\t%-10s", key, accPerLabel,
					worstConfusion) + "\n");
		}

		average.add(AverageOOV.TOTAL, accuracy);
		FileUtils.write(new File(storageLocation), sb.toString());

	}

	private String getClassThatWasMostOftenChosenWronglyForLabel(
			FrequencyDistribution<String> fd, String gold) {
		String mostOftenWronglyChosenLabel = null;
		long maxCount = 0;
		for (String key : fd.getKeys()) {
			if (key.equals(gold)) {
				continue;
			}
			long count = fd.getCount(key);
			if (maxCount < count) {
				mostOftenWronglyChosenLabel = key;
				maxCount = count;
			}
		}

		double worstConf = ((double) maxCount / fd.getN());
		String output = String.format("(%.2f%5s)", worstConf,
				mostOftenWronglyChosenLabel);

		return output;
	}

	private String extractTag(String line) {
		return line.split("\t")[0];
	}

	private void extractTrainingVocab(File train) {
		training = new HashSet<String>();
		try {
			InputStreamReader streamReader = new InputStreamReader(
					new FileInputStream(train), "UTF-8");
			BufferedReader br = new BufferedReader(streamReader);

			String next = null;
			while ((next = br.readLine()) != null) {

				if (next.isEmpty()) {
					continue;
				}

				String word = extractUnit(next);
				training.add(word);
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String extractUnit(String next) {
		int start = next.indexOf(ID_FEATURE_NAME);
		int end = next.indexOf("\t", start);
		start = next.lastIndexOf("_", end);

		String word = next.substring(start + 1, end);

		return word;
	}
}

class AverageOOV {
	public static final String TOTAL = "overAllAccuracyIteration";
	HashMap<String, List<Double>> avg;

	public AverageOOV() {
		avg = new HashMap<String, List<Double>>();
	}

	public Set<String> getKeys() {
		return avg.keySet();
	}

	public double getAveragedValueForKey(String key) {

		double total = 0;
		int countVal = 0;
		for (double v : avg.get(key)) {
			total += v;
			countVal++;
		}

		return total / countVal;
	}

	public HashMap<String, List<Double>> getDataPerRun() {
		return avg;
	}

	public void add(String key, double value) {
		List<Double> list = avg.get(key);
		if (list == null) {
			list = new ArrayList<Double>();
		}
		list.add(value);
		avg.put(key, list);
	}
}
