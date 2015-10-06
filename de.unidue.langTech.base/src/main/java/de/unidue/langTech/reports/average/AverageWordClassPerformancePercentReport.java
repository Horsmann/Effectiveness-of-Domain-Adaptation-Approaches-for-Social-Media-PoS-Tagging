package de.unidue.langTech.reports.average;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.unidue.langTech.misc.ExperimentConstants;
import de.unidue.langTech.reports.ReportUtil;
import de.unidue.langTech.reports.WordClassPerformanceReport;

public class AverageWordClassPerformancePercentReport
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

        List<List<String[]>> allFiles = new ArrayList<List<String[]>>();
        Map<String, RawFrequencyPair> avgFreqOfLabel = new HashMap<String, RawFrequencyPair>();
        for (String f : resultFolders) {

            avgFreqOfLabel = readOccurenceInFrequency(f, storageService, avgFreqOfLabel);

            String percentFile = getPercentFile(f, storageService);
            List<String[]> values = ReportUtil.parsePerLabelPerfomance(percentFile);
            allFiles.add(values);
        }

        Map<String, FrequencyPair> aggregatedFreq = computeAverageFrequency(avgFreqOfLabel);
        List<String[]> aggregated = ReportUtil.computeAveragePerLabelAccuracies(allFiles);
        String outfile = getOutputFile(storageService, experimentContext);
        write2File(outfile, aggregatedFreq, aggregated);

    }

    private Map<String, FrequencyPair> computeAverageFrequency(
            Map<String, RawFrequencyPair> aAvgFreqOfLabel)
    {
        Map<String, FrequencyPair> lfp = new HashMap<String, FrequencyPair>();
        for (String key : aAvgFreqOfLabel.keySet()) {
            RawFrequencyPair rawFrequencyPair = aAvgFreqOfLabel.get(key);
            FrequencyPair fp = new FrequencyPair(rawFrequencyPair);
            lfp.put(key, fp);
        }

        return lfp;
    }

    private Map<String, RawFrequencyPair> readOccurenceInFrequency(String f,
            StorageService storageService, Map<String, RawFrequencyPair> aAvgFreqOfLabel)
        throws Exception
    {
        String freqFile = storageService.getStorageFolder(f, "").getAbsolutePath() + "/output/"
                + WordClassPerformanceReport.OUTPUT_FILE_FREQUENCY;

        List<String> lines = FileUtils.readLines(new File(freqFile), "utf-8");
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            if (line.isEmpty()) {
                continue;
            }
            String[] split = line.split("\t");
            String label = split[0].trim();
            Integer totalFreq = Integer.valueOf(split[1].trim()) + Integer.valueOf(split[2].trim());

            RawFrequencyPair storedVal = aAvgFreqOfLabel.get(label);
            if (storedVal == null) {
                storedVal = new RawFrequencyPair(label, totalFreq);
            }
            else {
                storedVal.frequency += totalFreq;
                storedVal.numAdded++;
            }
            aAvgFreqOfLabel.put(label, storedVal);
        }
        return aAvgFreqOfLabel;
    }

    private String getPercentFile(String f, StorageService storageService)
    {
        return storageService.getStorageFolder(f, "").getAbsolutePath() + "/output/"
                + WordClassPerformanceReport.OUTPUT_FILE_PERCENT;
    }

    private String getOutputFile(StorageService storageService,
            TaskContextMetadata experimentContext)
    {
        String targetLocation = storageService.getStorageFolder(experimentContext.getLabel(), "")
                .getAbsolutePath() + "/" + ExperimentConstants.AVERAGE_FINE_PERFORMANCE_REPORT;
        return targetLocation;
    }

    private void write2File(String outfile, Map<String, FrequencyPair> aAvgFreqOfLabel,
            List<String[]> aggregated)
        throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%5s\t%10s\t%10s\n\n", "Label", "Avg-Freq", "Avg-Acc."));
        for (String[] p : aggregated) {
            FrequencyPair fp = aAvgFreqOfLabel.get(p[0]);
            String line = String.format("%5s\t%10d\t%10.3f\n", p[0], fp.frequency, getDouble(p[1])*100);
            sb.append(line);
        }
        FileUtils.writeStringToFile(new File(outfile), sb.toString(), "utf-8");
    }

    private Double getDouble(String string)
    {
        String trim = string.trim();
        String decPoint = trim.replaceAll(",", ".");
        return Double.valueOf(decPoint);
    }

    class RawFrequencyPair
    {
        String label;
        Integer frequency;
        Integer numAdded;

        RawFrequencyPair(String label, Integer frequency)
        {
            this.label = label;
            this.frequency = frequency;
            this.numAdded = 1;
        }
    }

    class FrequencyPair
    {
        String label;
        Integer frequency;
        Integer numAdded;

        FrequencyPair(RawFrequencyPair rfp)
        {
            this.label = rfp.label;
            this.frequency = rfp.frequency / rfp.numAdded;
            this.numAdded = 1;
        }
    }

    class RawAccuracyPair
    {
        String label;
        Double acc;
        Integer numAdded;

        RawAccuracyPair(String label, Double acc)
        {
            this.label = label;
            this.acc = acc;
            this.numAdded = 1;
        }
    }

    class AccuracyPair
    {
        String label;
        Double acc;

        AccuracyPair(RawAccuracyPair rap)
        {
            this.label = rap.label;
            this.acc = rap.acc / rap.numAdded;
        }
    }
}
