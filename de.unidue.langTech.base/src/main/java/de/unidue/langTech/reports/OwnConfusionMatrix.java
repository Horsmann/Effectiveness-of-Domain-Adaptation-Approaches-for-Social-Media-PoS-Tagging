package de.unidue.langTech.reports;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.lab.engine.TaskContext;
import de.tudarmstadt.ukp.dkpro.lab.reporting.ReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService.AccessMode;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

public class OwnConfusionMatrix
    extends ReportBase
{

    public void execute()
        throws Exception
    {
        File storage = getContext().getStorageLocation(CRFSuiteTestTask.TEST_TASK_OUTPUT_KEY,
                AccessMode.READONLY);
        File predictionFile = new File(storage.getAbsolutePath()
                + "/"
                + CRFSuiteAdapter.getInstance().getFrameworkFilename(
                        AdapterNameEntries.predictionsFile));

        List<String> readLines = (FileUtils.readLines(predictionFile));
        printConfusionMatrix(getContext(), readLines.toArray(new String[0]));
    }

    private void printConfusionMatrix(TaskContext aContext, String[] aLines)
        throws Exception
    {
        ConditionalFrequencyDistribution<String, String> cfd = buildCondFreqDis(aLines);

        outputConfusionMatrixWithAbsoluteFrequencies(aContext, cfd, cfd.getConditions().size());

        outputConfusionMatrixWithRatios(aContext, cfd, cfd.getConditions().size());
    }

    private void outputConfusionMatrixWithRatios(TaskContext aContext,
            ConditionalFrequencyDistribution<String, String> cfd, int aSize)
        throws Exception
    {
        String[][] data = new String[cfd.getConditions().size() + 1][cfd.getConditions().size() + 1];

        data = setColumnRowLabels(cfd, data);
        data = setContents(cfd, data, false);

        outputConfusionMatrix2File(aContext, data, cfd.getConditions().size(),
                "confusionMatrixRatio.txt");
    }

    private void outputConfusionMatrixWithAbsoluteFrequencies(TaskContext aContext,
            ConditionalFrequencyDistribution<String, String> cfd, int aSize)
        throws Exception
    {
        String[][] data = new String[cfd.getConditions().size() + 1][cfd.getConditions().size() + 1];

        data = setColumnRowLabels(cfd, data);
        data = setContents(cfd, data, true);

        outputConfusionMatrix2File(aContext, data, cfd.getConditions().size(),
                "confusionMatrixFreq.txt");
    }

    private void outputConfusionMatrix2File(TaskContext aContext, String[][] data, int size,
            String name)
        throws Exception
    {
        int maxLabelLen = 7;
        for (int i = 1; i < size; i++) {
            if (data[1][i].length() > maxLabelLen) {
                maxLabelLen = data[1][i].length();
            }
        }

        File confMatrix = new File(getContext().getStorageLocation(Constants.TEST_TASK_OUTPUT_KEY,
                AccessMode.READWRITE), name);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sb.append(String.format("%" + maxLabelLen + "s", data[i][j]));
            }
            sb.append("\n");
        }

        FileUtils.writeStringToFile(confMatrix, sb.toString());
    }

    private String[][] setContents(ConditionalFrequencyDistribution<String, String> cfd,
            String[][] data, boolean absFreq)
    {
        int maxCond = cfd.getConditions().size();

        int i = 2;
        for (String c : cfd.getConditions()) {
            FrequencyDistribution<String> fd = cfd.getFrequencyDistribution(c);
            long max = fd.getN();
            int j = 2;
            for (String key : cfd.getConditions()) {
                if (absFreq) {
                    data = setIntegerValue(data, fd, key, i, j);
                }
                else {
                    data = setDoubleValue(data, fd, key, i, j, max);
                }

                j++;
                if (j >= maxCond) {
                    break;
                }
            }
            i++;
            if (i >= maxCond) {
                break;
            }
        }
        return data;
    }

    private String[][] setDoubleValue(String[][] data, FrequencyDistribution<String> fd,
            String key, int i, int j, long max)
    {
        Double val = new Double((double) fd.getCount(key) / max);
        if (val > 0) {
            data[i][j] = "" + String.format("%.3f", val);
        }
        else {
            data[i][j] = "" + String.format("%d", 0);
        }
        return data;

    }

    private String[][] setIntegerValue(String[][] data, FrequencyDistribution<String> fd,
            String key, int i, int j)
    {
        Integer val = new Integer((int) fd.getCount(key));
        if (val > 0) {
            data[i][j] = "" + String.format("%d", val);
        }
        else {
            data[i][j] = "" + String.format("%d", 0);
        }
        return data;
    }

    private String[][] setColumnRowLabels(ConditionalFrequencyDistribution<String, String> cfd,
            String[][] data)
    {

        // Set headlines
        int maxConditions = cfd.getConditions().size();

        // Init outer row/column
        for (int k = 0; k < maxConditions; k++) {
            for (int j = 0; j < maxConditions; j++) {
                data[j][k] = "";
                data[k][j] = "";
            }
        }

        String[] prediction = { "P", "R", "E", "D" };
        int l = 0;
        for (int k = (maxConditions / 2) - (prediction.length / 2); k >= 0 && k < maxConditions; k++) {
            data[0][k] = prediction[l++];
            if (l >= prediction.length || l >= k) {
                break;
            }
        }

        String[] actual = { "A", "C", "T", "U", "A", "L" };
        l = 0;
        for (int k = (maxConditions / 2) - (actual.length / 2); k >= 0 && k < maxConditions; k++) {
            data[k][0] = actual[l++];
            if (l >= actual.length || l >= k) {
                break;
            }
        }

        int i = 2;
        for (String c : cfd.getConditions()) {
            data[i][1] = c;
            data[1][i] = c;
            i++;
            if (i >= cfd.getConditions().size()) {
                break;
            }
        }

        return data;
    }

    private ConditionalFrequencyDistribution<String, String> buildCondFreqDis(String[] aLines)
    {
        ConditionalFrequencyDistribution<String, String> cfd = new ConditionalFrequencyDistribution<String, String>();
        for (int i = 0; i < aLines.length; i++) {
            String line = aLines[i];
            if (line.startsWith("#")) {
                continue;
            }
            String[] split = line.split("\t");
            if (split.length > 1) {
                cfd.addSample(split[0], split[1], 1);
            }
        }
        return cfd;
    }
}
