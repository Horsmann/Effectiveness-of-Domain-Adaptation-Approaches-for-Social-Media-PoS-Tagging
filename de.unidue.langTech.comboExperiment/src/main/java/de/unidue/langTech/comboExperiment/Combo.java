package de.unidue.langTech.comboExperiment;

/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit������������������������������������������������������t Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteBatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsUFE;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentTrainTest;
import de.unidue.langTech.features.character.AllCapitalized;
import de.unidue.langTech.features.character.AllDigits;
import de.unidue.langTech.features.character.AllSpecialCharacters;
import de.unidue.langTech.features.character.ContainsHyphen;
import de.unidue.langTech.features.character.ContainsNumber;
import de.unidue.langTech.features.character.ContainsPeriod;
import de.unidue.langTech.features.character.FirstLetterCapitalized;
import de.unidue.langTech.features.character.IsNumber;
import de.unidue.langTech.features.lucene.LuceneCharacterNGramPerUnitUFE;
import de.unidue.langTech.features.resource.BrownClusterFeature;
import de.unidue.langTech.features.resource.DictionaryTagFeature;
import de.unidue.langTech.features.token.CurrentToken;
import de.unidue.langTech.features.token.NextToken;
import de.unidue.langTech.features.token.PreviousToken;
import de.unidue.langTech.misc.ExperimentUtil;
import de.unidue.langTech.provider.FileProvider;
import de.unidue.langTech.reader.Ritter2011TweetCorpusReader;
import de.unidue.langTech.reports.AccuracyOnUnknownWordsReport;
import de.unidue.langTech.reports.CoarseWordClassPerformanceReport;
import de.unidue.langTech.reports.IManualCrossValidation;
import de.unidue.langTech.reports.ManualCrossValidationReport;
import de.unidue.langTech.reports.OwnConfusionMatrix;
import de.unidue.langTech.reports.WordClassPerformanceReport;

/**
 * This a pure Java-based experiment setup of POS tagging as sequence tagging.
 */
public class Combo
    implements Constants, IManualCrossValidation
{
    public static String primaryCorpus = null;
    public static String experimentName = "comboExperiment";
    public static String languageCode = "en";
    public static Boolean useCoarse = false;

    static String homeFolder = null;
    private static String posMapping = null;
    private static String secondaryFolder;

    private static HashMap<String, List<String>> resultFiles = new HashMap<String, List<String>>();
    private static String dictionaryLocation;
    private static String brownCluster;

    public static void main(String[] args)
        throws Exception
    {

        int argNum = 5;
        if (args.length != argNum) {
            System.err
                    .println("Require "
                            + argNum
                            + " arguments:\n"
                            + "\t(1)  primary cross-validation corpus (file path to the corpus)\n"
                            + "\t(2)  secondary folder (folder path - all data in this folder is copied to the train set!)\n"
                            + "\t(3)  brown cluster (file path to the cluster file)\n"
                            + "\t(4)  dictionary location (file path to the dictionary file (json))\n"
                            + "\t(5)  output folder (folder path were all results/working files will be created)");
            return;
        }

        primaryCorpus = args[0];
        secondaryFolder = args[1];
        brownCluster = args[2];
        dictionaryLocation = args[3];
        homeFolder = args[4];

        posMapping = new FileProvider().getFine2CoarsePOSMappingFilePath();
        PropertyConfigurator.configure(new FileProvider().getLog4jPropertyFilePath());

        System.setProperty("DKPRO_HOME", homeFolder);

        List<String[]> cvTrainTestSplits = ExperimentUtil
                .createCrossValidationSetsSplittingOnlyOneCorpus(primaryCorpus, secondaryFolder, 10);

        int runNr = 1;
        for (String[] split : cvTrainTestSplits) {

            ParameterSpace pSpace = getParameterSpace(Constants.FM_SEQUENCE,
                    Constants.LM_SINGLE_LABEL, split[0], split[1]);

            Combo experiment = new Combo();
            experiment.validation(pSpace, runNr++);
        }

        ExperimentUtil.computeAverages(experimentName, resultFiles);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(String featureMode, String learningMode,
            String train, String test)
        throws Exception
    {

        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        Dimension<List<String>> dimFeatureSets = Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] { CurrentToken.class.getName(),
                        NextToken.class.getName(), PreviousToken.class.getName(),

                        AllSpecialCharacters.class.getName(), AllCapitalized.class.getName(),
                        AllDigits.class.getName(), IsNumber.class.getName(),
                        FirstLetterCapitalized.class.getName(), ContainsNumber.class.getName(),
                        ContainsHyphen.class.getName(), ContainsPeriod.class.getName(),

                        NrOfCharsUFE.class.getName(),
                        LuceneCharacterNGramPerUnitUFE.class.getName(),

                        BrownClusterFeature.class.getName(), DictionaryTagFeature.class.getName(),

                }));

        Dimension<List<String>> dimClassificationArgs = Dimension
                .create(DIM_CLASSIFICATION_ARGS,
                        asList(new String[] { CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR }));

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        BrownClusterFeature.PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES, brownCluster,
                        DictionaryTagFeature.PARAM_DICTIONARY_LOCATION, dictionaryLocation,
                        LuceneCharacterNGramPerUnitUFE.PARAM_CHAR_NGRAM_MIN_N, 2,
                        LuceneCharacterNGramPerUnitUFE.PARAM_CHAR_NGRAM_MAX_N, 4,
                        LuceneCharacterNGramPerUnitUFE.PARAM_CHAR_NGRAM_USE_TOP_K, 1000 }));

        dimReaders.put(DIM_READER_TRAIN, Ritter2011TweetCorpusReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS, Arrays.asList(
                Ritter2011TweetCorpusReader.PARAM_LANGUAGE, languageCode,
                Ritter2011TweetCorpusReader.PARAM_SOURCE_LOCATION, train,
                Ritter2011TweetCorpusReader.PARAM_POS_MAPPING_LOCATION, posMapping,
                Ritter2011TweetCorpusReader.PARAM_PATTERNS, "*.data",
                Ritter2011TweetCorpusReader.PARAM_USE_COARSE_GRAINED, useCoarse));
        dimReaders.put(DIM_READER_TEST, Ritter2011TweetCorpusReader.class);
        dimReaders.put(DIM_READER_TEST_PARAMS, Arrays.asList(
                Ritter2011TweetCorpusReader.PARAM_LANGUAGE, languageCode,
                Ritter2011TweetCorpusReader.PARAM_SOURCE_LOCATION, test,
                Ritter2011TweetCorpusReader.PARAM_POS_MAPPING_LOCATION, posMapping,
                Ritter2011TweetCorpusReader.PARAM_PATTERNS, "*.data",
                Ritter2011TweetCorpusReader.PARAM_USE_COARSE_GRAINED, useCoarse));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, learningMode), Dimension.create(
                        DIM_FEATURE_MODE, featureMode), dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

    protected void validation(ParameterSpace pSpace, int aRunNr)
        throws Exception
    {

        ExperimentTrainTest batch = new ExperimentTrainTest(experimentName + "_" + aRunNr,
                CRFSuiteAdapter.class, getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.addReport(CRFSuiteBatchTrainTestReport.class);
        batch.addReport(AccuracyOnUnknownWordsReport.class);
        batch.addInnerReport(OwnConfusionMatrix.class);
        batch.addInnerReport(addInterIntraReport());
        batch.addInnerReport(addCoarseMappingReport());
        batch.addReport(addDataCollectionReport());

        // Run
        Lab.getInstance().run(batch);
    }

    private Class<? extends Report> addDataCollectionReport()
    {
        ManualCrossValidationReport.pointer = this;
        return ManualCrossValidationReport.class;
    }

    private Class<? extends Report> addCoarseMappingReport()
    {
        CoarseWordClassPerformanceReport.language = languageCode;
        CoarseWordClassPerformanceReport.posMappingLocation = posMapping;
        return CoarseWordClassPerformanceReport.class;
    }

    private Class<? extends Report> addInterIntraReport()
    {
        WordClassPerformanceReport.language = languageCode;
        WordClassPerformanceReport.posMappingLocation = posMapping;
        return WordClassPerformanceReport.class;
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(NoOpAnnotator.class);
    }

    @Override
    public void add(String aKey, String aPath)
    {
        List<String> list = resultFiles.get(aKey);
        if (list == null) {
            list = new ArrayList<String>();
        }
        list.add(aPath);
        resultFiles.put(aKey, list);
    }
}
