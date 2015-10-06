package de.unidue.langTech.features.lucene;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneCharacterNGramUFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.LuceneCharacterNGramUnitMetaCollector;

public class LuceneCharacterNGramPerUnitUFE extends LuceneCharacterNGramUFE
{
    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(LuceneCharacterNGramUnitMetaCollector.class);
        
        return metaCollectorClasses;
    }
}
