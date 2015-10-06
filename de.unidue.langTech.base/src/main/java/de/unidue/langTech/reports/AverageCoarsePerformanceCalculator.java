package de.unidue.langTech.reports;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class AverageCoarsePerformanceCalculator
{
    boolean didRun = false;

    public List<CoarseTriple> computeAverage(List<List<CoarseTriple>> allFiles)
    {

        Map<String, RawPair> map = new HashMap<String, RawPair>();
        for (List<CoarseTriple> file : allFiles) {
            for (CoarseTriple t : file) {
                RawPair pair = map.get(t.label);
                if (pair == null) {
                    pair = new RawPair(t.frequency, t.accuracy);
                }
                else {
                    pair.freq += t.frequency;
                    pair.accuracy += t.accuracy;
                    pair.valuesAdded++;
                }
                map.put(t.label, pair);
            }
        }

        String[] sortedKeys = ReportUtil.sortByName(map.keySet());
        List<CoarseTriple> averageResult = new ArrayList<CoarseTriple>();
        for (String k : sortedKeys) {
            RawPair rawPair = map.get(k);
            Pair pair = new Pair(rawPair);
            averageResult.add(new CoarseTriple(k, pair.freq, pair.accuracy));
        }

        return averageResult;
    }

    public List<List<CoarseTriple>> readLines(List<String> fileLocations)
        throws Exception
    {
        List<List<CoarseTriple>> allTriples = new ArrayList<>();
        for (String fileLocation : fileLocations) {
            List<CoarseTriple> triples = new ArrayList<CoarseTriple>();
            List<String> lines = FileUtils.readLines(new File(fileLocation), "utf-8");
            for (String line : lines) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] split = line.split("\t");
                String label = split[0].trim();
                Integer freq = Integer.valueOf(split[1].trim());
                Double acc = Double.valueOf(split[2].trim().replaceAll(",", ".")) * 100;
                CoarseTriple t = new CoarseTriple(label, freq, acc);
                triples.add(t);
            }
            allTriples.add(triples);
        }
        return allTriples;
    }

    class RawPair
    {
        public int valuesAdded;
        private Integer freq;
        private Double accuracy;

        RawPair(Integer freq, Double accuracy)
        {
            this.freq = freq;
            this.accuracy = accuracy;
            this.valuesAdded = 1;
        }
    }

    class Pair
    {
        private Integer freq;
        private Double accuracy;

        Pair(RawPair rp)
        {
            this.freq = rp.freq / rp.valuesAdded;
            this.accuracy = rp.accuracy / rp.valuesAdded;
        }
    }

}
