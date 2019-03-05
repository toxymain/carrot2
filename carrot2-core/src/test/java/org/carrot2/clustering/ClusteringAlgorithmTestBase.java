package org.carrot2.clustering;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Nightly;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import org.assertj.core.api.Assertions;
import org.carrot2.TestBase;
import org.carrot2.attrs.*;
import org.carrot2.language.EnglishLanguageComponentsFactory;
import org.carrot2.language.LanguageComponents;
import org.carrot2.language.TestsLanguageComponentsFactoryVariant1;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ClusteringAlgorithmTestBase<T extends ClusteringAlgorithm & AcceptingVisitor> extends TestBase {
  protected abstract T algorithm();

  protected LanguageComponents testLanguageModel() {
    return LanguageComponents.get(TestsLanguageComponentsFactoryVariant1.NAME);
  }

  @Test
  public void ensureAllDefaultAttrsHaveRegisteredAliases() {
    // Part 1: default to-from serialization via map.
    Map<String, Object> asMap = Attrs.toMap(algorithm());
    AcceptingVisitor reconstructed = Attrs.fromMap(AcceptingVisitor.class, asMap);
    Assertions.assertThat(reconstructed)
        .isNotNull()
        .isInstanceOf(algorithm().getClass());

    // Part 2: explicit visitor.
    algorithm().accept(new AttrVisitor() {
      ArrayDeque<String> path = new ArrayDeque<>();

      @Override
      public void visit(String key, AttrBoolean attr) {
      }

      @Override
      public void visit(String key, AttrInteger attr) {
      }

      @Override
      public void visit(String key, AttrDouble attr) {
      }

      @Override
      public void visit(String key, AttrEnum<? extends Enum<?>> attr) {
      }

      @Override
      public void visit(String key, AttrString attr) {
      }

      @Override
      public void visit(String key, AttrStringArray attr) {
      }

      @Override
      public void visit(String key, AttrObject<?> attr) {
        AcceptingVisitor o = attr.get();
        if (o != null) {
          Assertions.assertThatCode(() -> AliasMapper.SPI_DEFAULTS.toName(o)).doesNotThrowAnyException();
          Assertions.assertThat(AliasMapper.SPI_DEFAULTS.toName(o)).isNotEmpty();

          path.addLast(key);
          o.accept(this);
          path.removeLast();
        }
      }
    });
  }

  @Test
  public void ensureAttributesHaveDescriptions() {
    ArrayList<String> errors = new ArrayList<>();
    algorithm().accept(new AttrVisitor() {
      ArrayDeque<String> path = new ArrayDeque<>();

      @Override
      public void visit(String key, AttrBoolean attr) {
        hasLabel(key, attr);
      }

      @Override
      public void visit(String key, AttrInteger attr) {
        hasLabel(key, attr);
      }

      @Override
      public void visit(String key, AttrDouble attr) {
        hasLabel(key, attr);
      }

      @Override
      public void visit(String key, AttrEnum<? extends Enum<?>> attr) {
        hasLabel(key, attr);
      }

      @Override
      public void visit(String key, AttrString attr) {
        hasLabel(key, attr);
      }

      @Override
      public void visit(String key, AttrStringArray attr) {
        hasLabel(key, attr);
      }

      @Override
      public void visit(String key, AttrObject<?> attr) {
        hasLabel(key, attr);
        AcceptingVisitor o = attr.get();
        if (o != null) {
          path.addLast(key);
          o.accept(this);
          path.removeLast();
        }
      }

      private void hasLabel(String key, Attr<?> attr) {
        if (attr.getDescription() == null) {
          path.addLast(key);
          errors.add("Attribute has no description: " + String.join(".", path));
          path.removeLast();
        }
      }
    });

    Assertions.assertThat(errors).isEmpty();
  }

  /**
   * A test to check if the algorithm does not fail with no documents.
   */
  @Test
  public void testNoDocuments() {
    assertThat(algorithm().cluster(Stream.empty(), testLanguageModel())).isEmpty();
  }

  @Test
  public void testDocumentsWithoutContent() {
    List<Document> documents = IntStream.range(0, randomIntBetween(1, 100))
        .mapToObj(i -> (Document) fieldConsumer -> {
          // No fields.
        })
        .collect(Collectors.toList());

    final List<Cluster<Document>> clusters = algorithm().cluster(
        documents.stream(),
        testLanguageModel());

    assertThat(clusters).isEmpty();
  }

  @Test
  public void testClusteringSampleDataSet() {
    List<Cluster<Document>> clusters = algorithm().cluster(
        SampleDocumentData.DOCUMENTS_DATA_MINING.stream(),
        LanguageComponents.get(EnglishLanguageComponentsFactory.NAME));

    assertThat(clusters.size())
        .isGreaterThan(0);

    for (Cluster<?> c : clusters) {
      System.out.println(c);
    }
  }

  @Test
  public void testAttrGetAndSet() {
    AcceptingVisitor algorithm = algorithm();
    Map<String, Object> map = Attrs.toMap(algorithm, JvmNameMapper.INSTANCE::toName);
    Attrs.fromMap(AcceptingVisitor.class, map, JvmNameMapper.INSTANCE::fromName);

    System.out.println(Attrs.toPrettyString(algorithm, AliasMapper.SPI_DEFAULTS));
  }

  /**
   * Runs the algorithm concurrently, verifying stability of results.
   */
  @Nightly
  @Test
  @ThreadLeakLingering(linger = 5000)
  public void testResultsStableFromSameOrder() throws Exception {
    final int numberOfThreads = randomIntBetween(1, 8);
    final int queriesPerThread = scaledRandomIntBetween(5, 25);

    System.out.println("Threads: " + numberOfThreads + ", qpt: " + queriesPerThread);

    List<Document> documents = RandomizedTest.randomFrom(Arrays.asList(
        SampleDocumentData.DOCUMENTS_DATA_MINING,
        SampleDocumentData.DOCUMENTS_DAWID));

    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    List<Callable<List<Cluster<Document>>>> callables = new ArrayList<>();
    for (int i = 0; i < numberOfThreads * queriesPerThread; i++) {
      final int dataSetIndex = i;
      callables.add(() -> {
        long s = System.currentTimeMillis();
        try {
          return algorithm().cluster(documents.stream(), testLanguageModel());
        } finally {
          System.out.println("Done. " + (System.currentTimeMillis() - s));
        }
      });
    }

    try {
      List<Cluster<Document>> reference = null;
      for (Future<List<Cluster<Document>>> f : executorService.invokeAll(callables)) {
        List<Cluster<Document>> clusters = f.get();
        if (reference == null) {
          reference = clusters;
        } else {
          assertThat(clusters).containsExactlyElementsOf(reference);
        }
      }
    } finally {
      executorService.shutdown();
    }
  }

  /**
   * Runs the algorithm concurrently, verifying stability of results.
   */
  @Nightly
  @Test
  @ThreadLeakLingering(linger = 5000)
  @Ignore("https://issues.carrot2.org/browse/CARROT-1195") // TODO: CARROT-1195
  public void testResultsStableFromRandomShuffle() throws Exception {
    final int numberOfThreads = randomIntBetween(1, 8);
    final int queriesPerThread = scaledRandomIntBetween(5, 25);

    System.out.println("Threads: " + numberOfThreads + ", qpt: " + queriesPerThread);

    List<Document> documents = RandomizedTest.randomFrom(Arrays.asList(
        SampleDocumentData.DOCUMENTS_DATA_MINING,
        SampleDocumentData.DOCUMENTS_DAWID));

    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    List<Callable<List<Cluster<Document>>>> callables = new ArrayList<>();
    for (int i = 0; i < numberOfThreads * queriesPerThread; i++) {
      final int dataSetIndex = i;
      callables.add(() -> {
        long s = System.currentTimeMillis();
        try {
          ArrayList<Document> cloned = new ArrayList<>(documents);
          Collections.shuffle(cloned);
          return algorithm().cluster(cloned.stream(), testLanguageModel());
        } finally {
          System.out.println("Done. " + (System.currentTimeMillis() - s));
        }
      });
    }

    try {
      List<Cluster<Document>> reference = null;
      for (Future<List<Cluster<Document>>> f : executorService.invokeAll(callables)) {
        List<Cluster<Document>> clusters = f.get();
        // Order documents by their hash code so that equality works.
        orderDocsByHash(clusters);
        if (reference == null) {
          reference = clusters;
        } else {
          assertThat(clusters).containsExactlyElementsOf(reference);
        }
      }
    } finally {
      executorService.shutdown();
    }
  }

  private void orderDocsByHash(List<Cluster<Document>> clusters) {
    clusters.forEach(c -> {
      c.getDocuments().sort(Comparator.comparingInt(Object::hashCode));
      orderDocsByHash(c.getSubclusters());
    });
  }
}
