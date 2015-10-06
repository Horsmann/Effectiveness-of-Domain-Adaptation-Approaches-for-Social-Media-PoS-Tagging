package de.unidue.langTech.reports.average;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.unidue.langTech.misc.ExperimentConstants;
import de.unidue.langTech.reports.ReportUtil;

public class AverageItemAccuracyReport
    extends BatchReportBase
{

    public void execute()
        throws Exception
    {
        StorageService storageService = getContext().getStorageService();
        TaskContextMetadata experimentContext = ReportUtil.getExperimentFolder(storageService,
                getSubtasks());
        List<String> resultFolders = ReportUtil.getResultFolderOfSingleRuns(storageService,
                experimentContext);

        List<String> fullPaths = new ArrayList<String>();
        for (String f : resultFolders) {
            fullPaths.add(storageService.getStorageFolder(f, "").getAbsolutePath());
        }

        List<Double> accuracies = ReportUtil.extractAccuracies(fullPaths);

        double avgAccuracy = getAverageAccuracy(accuracies);

        String targetLocation = storageService.getStorageFolder(experimentContext.getLabel(), "")
                .getAbsolutePath() + "/" + ExperimentConstants.AVERAGE_ACCURACY_REPORT;

        write2File(accuracies, avgAccuracy, targetLocation);

    }

    private void write2File(List<Double> accuracies, double avgAccuracy, String targetLocation)
        throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Accuracy\n\n");
        for (Double d : accuracies) {
            sb.append(String.format("%-6.3f", d));
            sb.append("\n");
        }
        sb.append("--------\n");
        sb.append(String.format("%-6.3f", avgAccuracy));

        FileUtils.writeStringToFile(new File(targetLocation), sb.toString(), "utf-8");

        Logger.getLogger(getClass()).info(
                String.format("%s %6.2f\n", "Average-Accuracy:", avgAccuracy));

    }

    private double getAverageAccuracy(List<Double> accuracies)
    {
        double total = 0.0;
        for (Double d : accuracies) {
            total += d;
        }
        return total / accuracies.size();
    }

}
