package de.unidue.langTech.features.jsonLoader;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;

public class JsonLoader
{
    @SuppressWarnings("unchecked")
    public static ConditionalFrequencyDistribution<String, String> loadCFD(String path) throws Exception
    {
        Gson g = new Gson();
        String readFileToString = FileUtils.readFileToString(new File(path));
        ConditionalFrequencyDistribution<String, String> fromJson = g.fromJson(readFileToString, ConditionalFrequencyDistribution.class);
        return fromJson;
    }

}
