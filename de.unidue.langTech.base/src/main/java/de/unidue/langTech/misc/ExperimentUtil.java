package de.unidue.langTech.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;

import com.google.common.io.Files;

import de.unidue.langTech.reports.CoarseTriple;
import de.unidue.langTech.reports.CoarseWordClassPerformanceReport;
import de.unidue.langTech.reports.ReportUtil;
import de.unidue.langTech.reports.WordClassPerformanceReport;

public class ExperimentUtil
{
    private static void computeAverageCoarsePerformance(HashMap<String, List<String>> resultFiles)
        throws Exception
    {
        for (String k : resultFiles.keySet()) {
            List<String> list = resultFiles.get(k);

            List<String> fullFilePath = new ArrayList<String>();
            for (String s : list) {
                fullFilePath.add(s + "/output/" + CoarseWordClassPerformanceReport.OUTPUT_FILE);
            }

            List<CoarseTriple> averagedCoarseData = ReportUtil.getAveragedCoarseData(fullFilePath);

            StringBuilder sb = new StringBuilder();
            for (CoarseTriple c : averagedCoarseData) {
                sb.append(String.format("%10s\t%6.2f\n", c.getLabel(), c.getAccuracy()));
            }

            FileUtils.write(new File(list.get(0) + "/"
                    + ExperimentConstants.AVERAGE_COARSE_PERFORMANCE_REPORT), sb.toString());
        }
    }

    private static void computeAverageLabelPerformance(HashMap<String, List<String>> resultFiles)
        throws Exception
    {

        for (String k : resultFiles.keySet()) {
            List<String> list = resultFiles.get(k);
            List<List<String[]>> allFiles = new ArrayList<List<String[]>>();
            for (String file : list) {
                List<String[]> parsePerLabelPerfomance = ReportUtil.parsePerLabelPerfomance(file
                        + "/output/" + WordClassPerformanceReport.OUTPUT_FILE_PERCENT);

                allFiles.add(parsePerLabelPerfomance);
            }
            List<String[]> computeAveragePerLabelAccuracies = ReportUtil
                    .computeAveragePerLabelAccuracies(allFiles);

            StringBuilder sb = new StringBuilder();
            for (String[] e : computeAveragePerLabelAccuracies) {
                sb.append(String.format("%5s\t%6.3f\n", e[0], Double.valueOf(e[1])));
            }

            FileUtils.writeStringToFile(new File(list.get(0) + "/"
                    + ExperimentConstants.AVERAGE_FINE_PERFORMANCE_REPORT), sb.toString());
        }

    }

    private static void computeAverageAccuraciesValues(HashMap<String, List<String>> resultFiles)
        throws Exception
    {
        for (String k : resultFiles.keySet()) {
            List<String> list = resultFiles.get(k);
            List<Double> accuracies = ReportUtil.extractAccuracies(list);
            writeAccuracies(list.get(0), accuracies);
        }
    }

    private static void writeAccuracies(String aString, List<Double> aAccuracies)
        throws Exception
    {
        double sum = 0.0;
        for (Double d : aAccuracies) {
            sum += d;
        }
        double avg = sum / aAccuracies.size();

        StringBuilder sb = new StringBuilder();
        for (Double d : aAccuracies) {
            sb.append(String.format("%6.2f\n", d));
        }
        sb.append("--------\n");
        sb.append(String.format("%6.2f\n", avg));

        FileUtils.writeStringToFile(new File(aString + "/"
                + ExperimentConstants.AVERAGE_ACCURACY_REPORT), sb.toString());

        LogFactory.getLog(ExperimentUtil.class.getName()).info(
                String.format("%s %6.2f\n", "Average-Accuracy:", avg));
    }

    public static List<String[]> createCrossValidationSetsSplittingOnlyOneCorpus(
            String trainTestCorpus, String miscCorpusFolder, int split)
        throws Exception
    {
        List<String[]> trainTestSets = new ArrayList<String[]>();

        List<String> readLines = FileUtils.readLines(new File(trainTestCorpus), "utf-8");
        int countTokens = countTokens(readLines);

        int lowerLimit = 0;
        int upperLimit = lowerLimit + countTokens / split;

        String tempDir = System.getProperty("java.io.tmpdir");
        for (int i = 0; i < split; i++) {
            String currentRunId = "SPLIT_" + (i + 1) + "_" + System.currentTimeMillis();
            String trainDirPath = tempDir + "/" + currentRunId + "_train/";
            String testDirPath = tempDir + "/" + currentRunId + "_test/";

            File train = new File(trainDirPath);
            train.mkdir();
            train.deleteOnExit();
            File test = new File(testDirPath);
            test.mkdir();
            test.deleteOnExit();

            File trainSplit = new File(trainDirPath + "/" + "trainSplit.data");
            File testSplit = new File(testDirPath + "/" + "testSplit.data");

            List<String> trainTokens = new ArrayList<String>();
            List<String> testTokens = new ArrayList<String>();

            for (int j = 0; j < readLines.size(); j++) {
                List<String> seq = new ArrayList<String>();

                for (; j < readLines.size(); j++) {
                    String line = readLines.get(j);
                    seq.add(line);
                    if (line.isEmpty()) {
                        break;
                    }
                }

                if (j + seq.size() >= lowerLimit && j <= upperLimit) {
                    testTokens.addAll(seq);
                }
                else {
                    trainTokens.addAll(seq);
                }

            }

            lowerLimit = lowerLimit + countTokens / split;
            upperLimit = lowerLimit + countTokens / split;

            FileUtils.writeLines(trainSplit, "utf-8", trainTokens);

            copyFilesOfSecondaryCorpus(trainDirPath, miscCorpusFolder);

            FileUtils.writeLines(testSplit, "utf-8", testTokens);

            trainTestSets.add(new String[] { train.getAbsolutePath(), test.getAbsolutePath() });
        }

        return trainTestSets;
    }

    private static void copyFilesOfSecondaryCorpus(String aTrainDirPath, String miscCorpusFolder)
        throws Exception
    {
        if (miscCorpusFolder == null) {
            return;
        }
        int counter = 0;
        for (File f : new File(miscCorpusFolder).listFiles()) {
            if (f.isHidden() || f.isDirectory()) {
                continue;
            }
            File vcb = new File(aTrainDirPath + "/" + "fold_" + (counter++ + 1) + ".data");
            Files.copy(f, vcb);
        }
    }

    public static List<String> splitIntoNFilesIntoTemporaryFolder(String trainTestCorpus, int split)
        throws Exception
    {
        List<String> outfiles = new ArrayList<String>();

        List<String> readLines = FileUtils.readLines(new File(trainTestCorpus), "utf-8");
        int countLines = readLines.size();
        int splitPoint = countLines / split;

        String tempDir = System.getProperty("java.io.tmpdir");
        String currentRunId = "learningCurve_" + System.currentTimeMillis();
        String outPath = tempDir + "/" + currentRunId;

        File out = new File(outPath);
        out.mkdir();
        out.deleteOnExit();

        List<String> buffer = new ArrayList<String>();
        int j = 0;
        for (int i = 0; i < split; i++) {

            while (j < splitPoint * (i + 1)) {
                String line = readLines.get(j++);
                buffer.add(line);

                if (j >= splitPoint) {
                    do {
                        line = readLines.get(j++);
                        buffer.add(line);
                    }
                    while (line != null && !line.isEmpty());
                }
            }

            String filePath = outPath + "/" + i + ".data";
            FileUtils.writeLines(new File(filePath), buffer);

            outfiles.add(filePath);
            buffer = new ArrayList<String>();
        }

        return outfiles;
    }

    private static int countTokens(List<String> readLines)
        throws Exception
    {
        int tokCount = 0;
        for (String l : readLines) {
            if (!l.isEmpty()) {
                tokCount++;
            }
        }

        return tokCount;
    }

    public static List<List<String>> createLearningCurvesplits(List<String> files)
        throws Exception
    {

        List<List<String>> allSplits = new ArrayList<List<String>>();

        for (int i = 0; i < files.size(); i++) {
            int testIdx = i;
            String tempDir = System.getProperty("java.io.tmpdir");
            String currentRunId = "learningCurve_" + testIdx + "_" + System.currentTimeMillis();
            String outPath = tempDir + "/" + currentRunId;

            String trainFolderPath = outPath + "/train";
            File trainFolder = new File(trainFolderPath);
            trainFolder.mkdirs();
            trainFolder.deleteOnExit();

            String testFolderPath = outPath + "/test";
            File testFolder = new File(testFolderPath);
            testFolder.mkdirs();
            testFolder.deleteOnExit();

            // copy test file outside of loop
            File f = new File(files.get(testIdx));
            File testFile = new File(testFolder.getAbsolutePath() + "/" + f.getName());
            Files.copy(f, testFile);

            List<String> trainFolders = new ArrayList<String>();
            for (int j = 0; j < files.size(); j++) {
                String subTrain = trainFolderPath + "/" + j;
                new File(subTrain).mkdirs();
                trainFolders.add(subTrain);
            }

            int j = 0;
            for (int k = 0; k < files.size(); k++) {
                j = k;
                String currFile = files.get(k);
                for (; j < trainFolders.size(); j++) {
                    String folder = trainFolders.get(j);
                    if (j == testIdx) {
                        continue;
                    }
                    String targetFile = folder + "/" + k + ".data";
                    Files.copy(new File(currFile), new File(targetFile));
                }
            }
            List<String> split = new ArrayList<String>();
            split.add(testFolder.getAbsolutePath());

            for (String fold : trainFolders) {
                new File(fold + "/" + testIdx + ".data").delete();
            }

            split.addAll(trainFolders);

            allSplits.add(split);
        }

        return allSplits;
    }

    public static List<String[]> createCVoversampleSplits(List<String> files,
            String aSecondaryFolder, Integer aOversample, int aFolds)
        throws Exception
    {
        List<String[]> trainTest = new ArrayList<String[]>();

        for (int i = 0; i < aFolds; i++) {
            int testFileIdx = i;

            String tempDir = System.getProperty("java.io.tmpdir");
            String currentRunId = aOversample + "_SPLIT_" + (i + 1) + "_"
                    + System.currentTimeMillis();
            String trainDirPath = tempDir + "/" + currentRunId + "/train/";
            new File(trainDirPath).mkdirs();
            new File(trainDirPath).deleteOnExit();
            String testDirPath = tempDir + "/" + currentRunId + "/test/";
            new File(testDirPath).mkdirs();
            new File(testDirPath).deleteOnExit();

            // Oversample pieces of primary data
            for (int j = 0; j < aFolds; j++) {
                if (j == testFileIdx) {
                    continue;
                }
                File trainSplitFile = new File(files.get(j));
                for (int k = 0; k < aOversample; k++) {
                    File ovsNr = new File(trainDirPath + "/" + (j + 1) + "_ovs_" + (k + 1)
                            + ".data");
                    Files.copy(trainSplitFile, ovsNr);
                }
            }

            // copy secondary resources into train folder
            int secondaryId = 0;
            for (File f : new File(aSecondaryFolder).listFiles()) {
                if (f.isHidden() || f.isDirectory()) {
                    continue;
                }
                File vcb = new File(trainDirPath + "/" + "foreign_" + (secondaryId++ + 1) + ".data");
                Files.copy(f, vcb);
            }

            File testFile = new File(files.get(testFileIdx));
            Files.copy(testFile, new File(testDirPath + "/" + (testFileIdx + 1) + ".data"));

            trainTest.add(new String[] { trainDirPath, testDirPath });
        }
        return trainTest;
    }

    public static void computeAverages(String aExperimentName,
            HashMap<String, List<String>> resultFiles)
        throws Exception
    {
        ExperimentUtil.computeAverageAccuraciesValues(resultFiles);
        ExperimentUtil.computeAverageLabelPerformance(resultFiles);
        ExperimentUtil.computeAverageCoarsePerformance(resultFiles);

        LogFactory.getLog(ExperimentUtil.class.getName()).info(
                "Averaged results are located into the experiment folder: " + aExperimentName
                        + "_1");
    }

}
