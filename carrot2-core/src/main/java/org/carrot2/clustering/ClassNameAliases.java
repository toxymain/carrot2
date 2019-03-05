package org.carrot2.clustering;

import org.carrot2.attrs.AliasMapper;
import org.carrot2.attrs.AliasMappingFactory;
import org.carrot2.clustering.kmeans.BisectingKMeansClusteringAlgorithm;
import org.carrot2.clustering.lingo.ClusterBuilder;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.clustering.lingo.SimpleLabelAssigner;
import org.carrot2.clustering.lingo.UniqueLabelAssigner;
import org.carrot2.clustering.stc.STCClusteringAlgorithm;
import org.carrot2.math.matrix.*;
import org.carrot2.text.preprocessing.BasicPreprocessingPipeline;
import org.carrot2.text.preprocessing.CompletePreprocessingPipeline;
import org.carrot2.text.preprocessing.DocumentAssigner;
import org.carrot2.text.preprocessing.LabelFilterProcessor;
import org.carrot2.text.preprocessing.filter.*;
import org.carrot2.text.vsm.*;

public class ClassNameAliases implements AliasMappingFactory {
  @Override
  public AliasMapper mapper() {
    return new AliasMapper()
        .alias("Lingo", LingoClusteringAlgorithm.class, LingoClusteringAlgorithm::new)
        .alias("BisectingKMeans", BisectingKMeansClusteringAlgorithm.class, BisectingKMeansClusteringAlgorithm::new)
        .alias("STC", STCClusteringAlgorithm.class, STCClusteringAlgorithm::new)

        .alias("SimpleLabelAssigner", SimpleLabelAssigner.class, SimpleLabelAssigner::new)
        .alias("UniqueLabelAssigner", UniqueLabelAssigner.class, UniqueLabelAssigner::new)

        .alias("BasicPreprocessingPipeline", BasicPreprocessingPipeline.class, BasicPreprocessingPipeline::new)
        .alias("CompletePreprocessingPipeline", CompletePreprocessingPipeline.class, CompletePreprocessingPipeline::new)

        .alias("ClusterBuilder", ClusterBuilder.class, ClusterBuilder::new)
        .alias("DocumentAssigner", DocumentAssigner.class, DocumentAssigner::new)

        .alias("CompleteLabelFilter", CompleteLabelFilter.class, CompleteLabelFilter::new)
        .alias("LabelFilterProcessor", LabelFilterProcessor.class, LabelFilterProcessor::new)
        .alias("GenitiveLabelFilter", GenitiveLabelFilter.class, GenitiveLabelFilter::new)
        .alias("MinLengthLabelFilter", MinLengthLabelFilter.class, MinLengthLabelFilter::new)
        .alias("NumericLabelFilter", NumericLabelFilter.class, NumericLabelFilter::new)
        .alias("QueryLabelFilter", QueryLabelFilter.class, QueryLabelFilter::new)
        .alias("StopLabelFilter", StopLabelFilter.class, StopLabelFilter::new)
        .alias("StopWordLabelFilter", StopWordLabelFilter.class, StopWordLabelFilter::new)

        .alias("TermDocumentMatrixBuilder", TermDocumentMatrixBuilder.class, TermDocumentMatrixBuilder::new)
        .alias("TermDocumentMatrixReducer", TermDocumentMatrixReducer.class, TermDocumentMatrixReducer::new)

        .alias("TfTermWeighting", TfTermWeighting.class, TfTermWeighting::new)
        .alias("LinearTfIdfTermWeighting", LinearTfIdfTermWeighting.class, LinearTfIdfTermWeighting::new)
        .alias("LogTfIdfTermWeighting", LogTfIdfTermWeighting.class, LogTfIdfTermWeighting::new)

        .alias("KMeansMatrixFactorizationFactory", KMeansMatrixFactorizationFactory.class, KMeansMatrixFactorizationFactory::new)
        .alias("LocalNonnegativeMatrixFactorizationFactory", LocalNonnegativeMatrixFactorizationFactory.class, LocalNonnegativeMatrixFactorizationFactory::new)
        .alias("NonnegativeMatrixFactorizationEDFactory", NonnegativeMatrixFactorizationEDFactory.class, NonnegativeMatrixFactorizationEDFactory::new)
        .alias("NonnegativeMatrixFactorizationKLFactory", NonnegativeMatrixFactorizationKLFactory.class, NonnegativeMatrixFactorizationKLFactory::new)
        .alias("PartialSingularValueDecompositionFactory", PartialSingularValueDecompositionFactory.class, PartialSingularValueDecompositionFactory::new);
  }

  @Override
  public String name() {
    return "Core classes";
  }
}
