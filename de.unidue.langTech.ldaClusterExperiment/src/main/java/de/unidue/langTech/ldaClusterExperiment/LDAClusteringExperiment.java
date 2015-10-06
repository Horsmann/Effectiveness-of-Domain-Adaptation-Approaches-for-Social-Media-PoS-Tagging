package de.unidue.langTech.ldaClusterExperiment;


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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosTagger;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.reporting.Report;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteAdapter;
import de.tudarmstadt.ukp.dkpro.tc.crfsuite.CRFSuiteBatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsUFE;
import de.tudarmstadt.ukp.dkpro.tc.ml.ExperimentCrossValidation;
import de.unidue.langTech.features.character.AllCapitalized;
import de.unidue.langTech.features.character.AllDigits;
import de.unidue.langTech.features.character.AllSpecialCharacters;
import de.unidue.langTech.features.character.ContainsHyphen;
import de.unidue.langTech.features.character.ContainsNumber;
import de.unidue.langTech.features.character.ContainsPeriod;
import de.unidue.langTech.features.character.FirstLetterCapitalized;
import de.unidue.langTech.features.character.IsNumber;
import de.unidue.langTech.features.lucene.LuceneCharacterNGramPerUnitUFE;
import de.unidue.langTech.features.resource.LDAClusterFeature;
import de.unidue.langTech.features.token.CurrentToken;
import de.unidue.langTech.features.token.NextToken;
import de.unidue.langTech.features.token.PreviousToken;
import de.unidue.langTech.provider.FileProvider;
import de.unidue.langTech.reader.Ritter2011TweetCorpusReader;
import de.unidue.langTech.reports.CoarseWordClassPerformanceReport;
import de.unidue.langTech.reports.WordClassPerformanceReport;
import de.unidue.langTech.reports.average.AverageCoarsePerformanceReport;
import de.unidue.langTech.reports.average.AverageItemAccuracyReport;
import de.unidue.langTech.reports.average.AverageWordClassPerformancePercentReport;

/**
 * This a pure Java-based experiment setup of POS tagging as sequence tagging.
 */
public class LDAClusteringExperiment
    implements Constants
{
    public static String corpus = null;
    public static String experimentName = "ldaClusteringExperiment";
    public static String languageCode = "en";

    private int NUM_FOLDS = 10;

    static String homeFolder = null;
    private static String posMapping = null;
    private static String ldaCluster = null;

    public static void main(String[] args)
        throws Exception
    {

        int argNum = 3;
        if (args.length != argNum) {
            System.err
                    .println("Require "
                            + argNum
                            + " arguments:\n"
                            + "\t(1)  corpus (file path)\n"
                            + "\t(2)  cluster location (file path)\n"
                            + "\t(3)  output folder (folder path were all results/working files will be created)");
            return;
        }

        corpus = args[0];
        ldaCluster = args[1];
        homeFolder = args[2];

        posMapping = new FileProvider().getFine2CoarsePOSMappingFilePath();
        System.setProperty("DKPRO_HOME", homeFolder);
        PropertyConfigurator.configure(new FileProvider().getLog4jPropertyFilePath());

        ParameterSpace pSpace = getParameterSpace(Constants.FM_SEQUENCE, Constants.LM_SINGLE_LABEL);

        LDAClusteringExperiment experiment = new LDAClusteringExperiment();
        experiment.validation(pSpace);
        
        LogFactory.getLog(LDAClusteringExperiment.class.getName()).info(
                "Averaged results are located into the experiment folder: "
                        + ExperimentCrossValidation.class.getSimpleName() + "$1" + experimentName);
        
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(String featureMode, String learningMode)
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

                        LDAClusterFeature.class.getName() }));

        Dimension<List<String>> dimClassificationArgs = Dimension
                .create(DIM_CLASSIFICATION_ARGS,
                        asList(new String[] { CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR }));

        Dimension<List<Object>> dimPipelineParameters = Dimension.create(
                DIM_PIPELINE_PARAMS,
                Arrays.asList(new Object[] {
                        LDAClusterFeature.PARAM_LDA_CLUSTER_CLASS_PROPABILITIES, ldaCluster,
                        LDAClusterFeature.PARAM_USE_LEMMAS, true,
                        LuceneCharacterNGramPerUnitUFE.PARAM_CHAR_NGRAM_MIN_N, 2,
                        LuceneCharacterNGramPerUnitUFE.PARAM_CHAR_NGRAM_MAX_N, 4,
                        LuceneCharacterNGramPerUnitUFE.PARAM_CHAR_NGRAM_USE_TOP_K, 1000 }));

        dimReaders.put(DIM_READER_TRAIN, Ritter2011TweetCorpusReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS, Arrays.asList(
                Ritter2011TweetCorpusReader.PARAM_LANGUAGE, languageCode,
                Ritter2011TweetCorpusReader.PARAM_POS_MAPPING_LOCATION, posMapping,
                Ritter2011TweetCorpusReader.PARAM_SOURCE_LOCATION, corpus,
                Ritter2011TweetCorpusReader.PARAM_USE_COARSE_GRAINED, false));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, learningMode), Dimension.create(
                        DIM_FEATURE_MODE, featureMode), dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

    protected void validation(ParameterSpace pSpace)
        throws Exception
    {

        ExperimentCrossValidation batch = new ExperimentCrossValidation(experimentName,
                CRFSuiteAdapter.class, getPreprocessing(), NUM_FOLDS);
        batch.setParameterSpace(pSpace);
        batch.addReport(CRFSuiteBatchCrossValidationReport.class);
        batch.addInnerReport(addInterIntraReport());
        batch.addInnerReport(addCoarseMappingReport());
        batch.addReport(AverageItemAccuracyReport.class);
        batch.addReport(AverageWordClassPerformancePercentReport.class);
        batch.addReport(AverageCoarsePerformanceReport.class);

        // Run
        Lab.getInstance().run(batch);
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
        return createEngineDescription(TreeTaggerPosTagger.class,
                TreeTaggerPosTagger.PARAM_WRITE_POS, false);
    }
}
