package de.unidue.langTech.reports;


public class CoarseTriple
{
    String label;
    Integer frequency;
    Double accuracy;

    CoarseTriple(String label, Integer frequency, Double accuracy)
    {
        this.label = label;
        this.frequency = frequency;
        this.accuracy = accuracy;
    }

    public String getLabel()
    {
        return label;
    }

    public Integer getFrequency()
    {
        return frequency;
    }

    public Double getAccuracy()
    {
        return accuracy;
    }
    
    

}