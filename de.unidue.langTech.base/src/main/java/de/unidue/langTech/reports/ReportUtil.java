package de.unidue.langTech.reports;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

public class ReportUtil
{

    public static List<String> getResultFolderOfSingleRuns(StorageService storage,
            TaskContextMetadata experiment)
        throws Exception
    {

        return extractFoldersWithResultPerRun(storage, experiment);
    }

    public static String[] sortByName(Set<String> unsortedLabels)
    {
        String[] labels = unsortedLabels.toArray(new String[0]);
        Arrays.sort(labels, new Comparator<String>()
        {

            public int compare(String o1, String o2)
            {
                return o1.compareTo(o2);
            }
        });
        return labels;
    }

    private static List<String> extractFoldersWithResultPerRun(StorageService store,
            TaskContextMetadata subcontext)
        throws Exception
    {
        File propertiesTXT = store.getStorageFolder(subcontext.getId(), "PROPERTIES.txt");
        List<String> crfsuiteResultFolders = getFoldersOfSingleRuns(propertiesTXT);
        return crfsuiteResultFolders;
    }

    public static TaskContextMetadata getExperimentFolder(StorageService storageService,
            TaskContextMetadata[] subtasks)
    {
        for (TaskContextMetadata subcontext : subtasks) {
            if (subcontext.getType().contains("ExperimentCrossValidation")) {
                return subcontext;
            }
        }
        return null;
    }

    private static List<String> getFoldersOfSingleRuns(File propertiesTXT)
        throws Exception
    {
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
        int start = line.indexOf("[") + 1;
        int end = line.indexOf("]");
        String subTasks = line.substring(start, end);

        String[] tasks = subTasks.split(",");

        List<String> resultFolderSingleRun = new ArrayList<String>();
        for (String task : tasks) {
            task = task.trim();
            if (task.startsWith("CRFSuiteTestTask")) {
                resultFolderSingleRun.add(task);
            }
        }

        return resultFolderSingleRun;
    }

    public static List<Double> extractAccuracies(List<String> resultOutputFolder)
        throws Exception
    {
        List<Double> accuracies = new ArrayList<>();
        for (String f : resultOutputFolder) {

            String fileLocation = f + "/output/" + CRFSuiteTestTask.FILE_PER_CLASS_PRECISION_RECALL_F1;

            double accuracy = getItemAccuracy(fileLocation);

            accuracies.add(accuracy);
        }
        return accuracies;
    }

    public static List<String[]> parsePerLabelPerfomance(String fileLocation)
        throws Exception
    {
        List<String[]> pairs = new ArrayList<String[]>();
        List<String> lines = FileUtils.readLines(new File(fileLocation));
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            if (line.isEmpty()) {
                continue;
            }
            if (line.trim().startsWith("(null)")) {
                continue;
            }
            String[] split = line.split("\t");
            String[] entry = new String[2];
            entry[0] = split[0].trim();
            entry[1] = split[1].trim();
            pairs.add(entry);
        }

        return pairs;
    }

    private static double getItemAccuracy(String outputFilePath)
        throws Exception
    {
        List<String> readLines = FileUtils.readLines(new File(outputFilePath));
        String itemAccLine = null;
        for (String line : readLines) {
            if (line.startsWith("Item accuracy:")) {
                int begin = line.indexOf("(") + 1;
                int end = line.indexOf(")");
                itemAccLine = line.substring(begin, end);
                break;
            }
        }

        return Double.valueOf(itemAccLine) * 100;
    }

    public static List<String[]> computeAveragePerLabelAccuracies(List<List<String[]>> allFiles)
    {

        Map<String, RawAccuracyPair> summedUpValues = new HashMap<String, RawAccuracyPair>();
        for (List<String[]> singleFile : allFiles) {
            for (String[] p : singleFile) {
                String label = p[0];
                RawAccuracyPair val = summedUpValues.get(label);
                if (val == null) {
                    val = new RawAccuracyPair(label, getDouble(p[1]));
                }
                else {
                    val.acc += getDouble(p[1]);
                    val.numAdded++;
                }
                summedUpValues.put(label, val);
            }
        }
        List<String[]> aggregatedAccuracies = new ArrayList<String[]>();
        String[] sortedKeys = ReportUtil.sortByName(summedUpValues.keySet());
        for (String key : sortedKeys) {
            RawAccuracyPair rap = summedUpValues.get(key);

            Double avg = rap.acc / rap.numAdded;

            aggregatedAccuracies.add(new String[] { key, avg.toString() });
        }

        return aggregatedAccuracies;
    }

    private static Double getDouble(String string)
    {
        String trim = string.trim();
        String decPoint = trim.replaceAll(",", ".");
        return Double.valueOf(decPoint);
    }

    public static List<CoarseTriple> getAveragedCoarseData(List<String> aList)
        throws Exception
    {
        AverageCoarsePerformanceCalculator acpc = new AverageCoarsePerformanceCalculator();
        List<List<CoarseTriple>> readLines = acpc.readLines(aList);
        return acpc.computeAverage(readLines);
    }

}
