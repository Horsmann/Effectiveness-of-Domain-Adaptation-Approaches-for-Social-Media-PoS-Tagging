package de.unidue.langTech.reports;
public class RawAccuracyPair
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
