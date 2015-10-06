package de.unidue.langTech.converter.german.tiger;

/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit??t Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class SimpleXMLTigerCorpusReader
    extends JCasResourceCollectionReader_ImplBase
    implements TCReaderSequence
{

    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String mappingPosLocation;

    public static final String PARAM_POS_TAGSET = ComponentParameters.PARAM_POS_TAG_SET;
    @ConfigurationParameter(name = PARAM_POS_TAGSET, mandatory = false)
    protected String posTagset;

    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, defaultValue = "UTF-8")
    protected String encoding;

    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    /**
     * Whether coarse-grained or fine-grained POS tags should be used.
     */
    public static final String PARAM_USE_COARSE_GRAINED = "useCoarseGrained";
    @ConfigurationParameter(name = PARAM_USE_COARSE_GRAINED, mandatory = true, defaultValue = "false")
    protected boolean useCoarseGrained;

    public static final String ENCODING_AUTO = "auto";

    private MappingProvider posMappingProvider;

    private BufferedReader br;

    private List<String[]> sentence = new ArrayList<String[]>();

    private List<BufferedReader> bfs = new ArrayList<BufferedReader>();
    private int currentReader = 0;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        posMappingProvider = new MappingProvider();
        posMappingProvider.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/"
                        + "core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
        posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        posMappingProvider.setDefault("tagger.tagset", "default");
        posMappingProvider.setOverride(MappingProvider.LOCATION, mappingPosLocation);
        posMappingProvider.setOverride(MappingProvider.LANGUAGE, language);
        posMappingProvider.setOverride("tagger.tagset", posTagset);

        try {
            for (Resource r : getResources()) {
                String name = r.getResource().getFilename();
                InputStreamReader is = null;
                if (name.endsWith(".gz")) {
                    is = new InputStreamReader(new GZIPInputStream(r.getInputStream()), encoding);
                }
                else {
                    is = new InputStreamReader(r.getInputStream(), encoding);
                }

                br = new BufferedReader(is);
                bfs.add(br);
            }
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {

        DocumentMetaData md = new DocumentMetaData(aJCas);
        md.setDocumentTitle("Tiger");

        md.setDocumentId("" + makeId());
        md.setLanguage(language);
        md.addToIndexes();

        try {
            posMappingProvider.configure(aJCas.getCas());
        }
        catch (AnalysisEngineProcessException e1) {
            e1.printStackTrace();
        }

        String documentText = "";
        for (int i = 0; i < sentence.size(); i++) {

            String[] entry = sentence.get(i);

            String tokenText = entry[0];
            String posText = entry[1];

            int tokenLen = tokenText.length();
            documentText += tokenText;

            int tokStart = documentText.length() - tokenLen;
            int tokEnd = documentText.length();
            Token token = new Token(aJCas, tokStart, tokEnd);
            token.addToIndexes();

            if (i + 1 < sentence.size()) {
                documentText += " ";
            }

            Type posTag = posMappingProvider.getTagType(posText);
            POS pos = (POS) aJCas.getCas().createAnnotation(posTag, token.getBegin(),
                    token.getEnd());
            pos.setPosValue(posText);
            pos.addToIndexes();

            token.setPos(pos);

        }
        aJCas.setDocumentText(documentText);
        closeBuilderAndSetSentenceAndClassificationSequenceType(aJCas);
        try {
            setClassificationOutcome(aJCas);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    private String makeId()
    {
        Random random = new Random();
        int nextInt = random.nextInt();
        return "" + ((nextInt < 0) ? nextInt * (-1) : nextInt);
    }

    private void setClassificationOutcome(JCas aJCas)
        throws Exception
    {
        Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);

        int id = 0;
        for (Token token : tokens) {
            TextClassificationUnit unit = new TextClassificationUnit(aJCas, token.getBegin(),
                    token.getEnd());
            unit.setId(id++);
            unit.addToIndexes();

            TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas,
                    token.getBegin(), token.getEnd());
            outcome.setOutcome(getTextClassificationOutcome(aJCas, unit));
            outcome.addToIndexes();
        }
    }

    private void closeBuilderAndSetSentenceAndClassificationSequenceType(JCas aJCas)
    {
        Sentence sentence = new Sentence(aJCas, 0, aJCas.getDocumentText().length());
        sentence.addToIndexes();

        TextClassificationSequence sequence = new TextClassificationSequence(aJCas, 0, aJCas
                .getDocumentText().length());
        sequence.addToIndexes();
    }

    public String getTextClassificationOutcome(JCas jcas, TextClassificationUnit unit)
        throws CollectionException
    {
        List<POS> posList = JCasUtil.selectCovered(jcas, POS.class, unit);
        if (posList.size() != 1) {
            throw new CollectionException(new Throwable(
                    "Could not get unique POS annotation to be used as TC outome."));
        }

        String outcome = "";
        if (useCoarseGrained) {
            outcome = posList.get(0).getClass().getSimpleName();
        }
        else {
            outcome = posList.get(0).getPosValue();
        }
        return outcome;
    }

    public boolean hasNext()
        throws IOException, CollectionException
    {

        BufferedReader br = getBufferedReader();

        sentence = new ArrayList<String[]>();
        String readLine = null;
        while ((readLine = br.readLine()) != null) {
            if (skipLine(readLine)) {
                continue;
            }

            if (endOfSentence(readLine)) {
                break;
            }
            String[] data = extractInfoFromXML(readLine);

            sentence.add(data);
        }
        if (!sentence.isEmpty()) {
            return true;
        }

        return closeReaderOpenNext();

    }

    private boolean endOfSentence(String readLine)
    {
        return readLine.contains("</terminal");
    }

    private boolean skipLine(String readLine)
    {
        return !readLine.contains("<t ") && !readLine.contains("</terminals");
    }

    private String[] extractInfoFromXML(String aReadLine)
    {
        String POS_PATTERN = "pos=\"";
        String WORD_PATTERN = " word=\"";
        String PATTERN_END = "\"";

        int posStart = aReadLine.indexOf(POS_PATTERN);
        int posEnd = aReadLine.indexOf("\"", posStart + POS_PATTERN.length());
        String pos = aReadLine.substring(posStart + POS_PATTERN.length(), posEnd);

        int wordStart = aReadLine.indexOf(WORD_PATTERN);
        int wordEnd = aReadLine.indexOf(PATTERN_END, wordStart + WORD_PATTERN.length());
        String word = aReadLine.substring(wordStart + WORD_PATTERN.length(), wordEnd);

        word = substituteUnicodeSequences(word);

        String[] data = { word, pos };

        return data;
    }

    private String substituteUnicodeSequences(String word)
    {

        // German Special Characters
        if (word.contains("&#x00e4;")) {
            word = word.replaceAll("&#x00e4;", "ä");
        }
        if (word.contains("&#x00c4;")) {
            word = word.replaceAll("&#x00c4;", "Ä");
        }

        if (word.contains("&#x00fc;")) {
            word = word.replaceAll("&#x00fc;", "ü");
        }
        if (word.contains("&#x00dc;")) {
            word = word.replaceAll("&#x00dc;", "Ü");
        }

        if (word.contains("&#x00f6;")) {
            word = word.replaceAll("&#x00f6;", "ö");
        }
        if (word.contains("&#x00d6;")) {
            word = word.replaceAll("&#x00d6;", "Ö");
        }

        if (word.contains("&#x00df;")) {
            word = word.replaceAll("&#x00df;", "ß");
        }

        // Special Signs
        if (word.contains("&quot;")) {
            word = word.replaceAll("&quot;", "\"");
        }

        if (word.contains("&#x00a7;")) {
            word = word.replaceAll("&#x00a7;", "§");
        }
        
        if (word.contains("&#x00bb;")) {
            word = word.replaceAll("&#x00bb;", "»");
        }
        
        if (word.contains("&#x00ab;")) {
            word = word.replaceAll("&#x00ab;", "«");
        }
        
        if (word.contains("&#x00e5;")) {
            word = word.replaceAll("&#x00e5;", "å");
        }
        
        if (word.contains("&#x00bf;")) {
            word = word.replaceAll("&#x00bf;", "¿");
        }
        if (word.contains("&#x00b7;")) {
            word = word.replaceAll("&#x00b7;", "·");
        }

        return word;
    }

    private boolean closeReaderOpenNext()
        throws CollectionException, IOException
    {
        try {
            bfs.get(currentReader).close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (currentReader + 1 < bfs.size()) {
            return hasNext();
        }

        return false;
    }

    private BufferedReader getBufferedReader()
    {
        return bfs.get(currentReader);
    }

    public Progress[] getProgress()
    {
        return null;
    }

}
