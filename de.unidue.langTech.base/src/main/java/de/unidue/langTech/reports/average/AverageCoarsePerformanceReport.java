package de.unidue.langTech.reports.average;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.unidue.langTech.misc.ExperimentConstants;
import de.unidue.langTech.reports.AverageCoarsePerformanceCalculator;
import de.unidue.langTech.reports.CoarseTriple;
import de.unidue.langTech.reports.CoarseWordClassPerformanceReport;
import de.unidue.langTech.reports.ReportUtil;

public class AverageCoarsePerformanceReport
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

        List<String> fileLocations = new ArrayList<>();
        for (String f : resultFolders) {

            String fileLocation = storageService.getStorageFolder(f, "").getAbsolutePath()
                    + "/output/" + CoarseWordClassPerformanceReport.OUTPUT_FILE;
            fileLocations.add(fileLocation);
        }
        AverageCoarsePerformanceCalculator acpc = new AverageCoarsePerformanceCalculator();
        List<CoarseTriple> computeAverage = acpc.computeAverage(acpc.readLines(fileLocations));
        

        String targetLocation = storageService.getStorageFolder(experimentContext.getLabel(), "")
                .getAbsolutePath() + "/" + ExperimentConstants.AVERAGE_COARSE_PERFORMANCE_REPORT;

        write2File(targetLocation, computeAverage);

    }

    private void write2File(String targetLocation, List<CoarseTriple> average)
        throws Exception
    {

        StringBuilder sb = new StringBuilder();

        String headline = String.format("#%-5s\t%-5s\t%-6s\n\n", "Label", "Freq", "Acc");
        sb.append(headline);

        for (CoarseTriple t : average) {
            String line = String.format("%-5s\t%5s\t%6.2f\n", t.getLabel(), t.getFrequency(), t.getAccuracy());
            sb.append(line);
        }

        FileUtils.writeStringToFile(new File(targetLocation), sb.toString(), "UTF-8");
    }
}
