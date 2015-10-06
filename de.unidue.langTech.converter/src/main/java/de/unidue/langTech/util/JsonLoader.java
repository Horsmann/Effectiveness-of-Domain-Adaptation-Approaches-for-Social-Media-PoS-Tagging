package de.unidue.langTech.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;

public class JsonLoader
{
    public static ConditionalFrequencyDistribution loadCFD(String path) throws Exception
    {
        Gson g = new Gson();
        String readFileToString = FileUtils.readFileToString(new File(path));
        ConditionalFrequencyDistribution fromJson = g.fromJson(readFileToString, ConditionalFrequencyDistribution.class);
        return fromJson;
    }

}
