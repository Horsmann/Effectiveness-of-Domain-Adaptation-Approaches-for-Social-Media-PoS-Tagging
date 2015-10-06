package de.unidue.langTech.util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class DictionaryWordCoverageAnalysis
{
    static HashMap<String, Integer[]> wc = new HashMap<String, Integer[]>();

    public static void main(String[] args)
        throws Exception
    {

        String corpus = args[0];
        String dictionary = args[1];

        List<String> lines = FileUtils.readLines(new File(corpus), "utf-8");
        ConditionalFrequencyDistribution dict = JsonLoader.loadCFD(dictionary);

        int contain = 0;
        int missing = 0;
        for (String l : lines) {
            if (l.isEmpty()) {
                continue;
            }
            String[] split = l.split(" ");
            String token = split[0];

            FrequencyDistribution fd = dict.getFrequencyDistribution(token);
            if (fd == null) {
                missing++;
                perWCUpdate(split[1], false);
            }
            else {
                contain++;
                perWCUpdate(split[1], true);
            }

        }
        System.out.println("Of " + (contain + missing) + " token " + contain
                + " were contained in the dictionary that are " + (double) contain
                / (contain + missing) + "\n");

        String[] keySet = wc.keySet().toArray(new String[0]);
        Arrays.sort(keySet, new Comparator<String>(){

            @Override
            public int compare(String aO1, String aO2)
            {
                return aO1.compareTo(aO2);
            }});
        for (String k : keySet) {
            Integer[] v = wc.get(k);
            int sum = v[0] + v[1];
            String s = String.format("%5s %4d %6.2f", k, sum, (double) v[0] / sum*100);
            System.out.println(s);
        }

    }

    private static void perWCUpdate(String wordClass, boolean aB)
    {

        String coarse = map2Coarse(wordClass);

        Integer[] integers = wc.get(coarse);
        if (integers == null) {
            integers = new Integer[2];
            integers[0] = 0;
            integers[1] = 0;
        }

        if (aB) {
            integers[0]++;
        }
        else {
            integers[1]++;
        }
        wc.put(coarse, integers);
    }

    private static String map2Coarse(String aWordClass)
    {
        if (aWordClass.startsWith("JJ")) {
            return "ADJ";
        }
        if (aWordClass.startsWith("VB") || aWordClass.equals("MD")) {
            return "VERB";
        }
        if (aWordClass.startsWith("NN")) {
            return "NOUN";
        }
        if (aWordClass.equals("WRB") || aWordClass.startsWith("RB")) {
            return "ADVERB";
        }
        if (aWordClass.equals("IN") || aWordClass.equals("RP") || aWordClass.equals("TO")) {
            return "PREP";
        }

        return aWordClass;
    }

}
