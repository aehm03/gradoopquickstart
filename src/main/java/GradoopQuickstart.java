import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.algorithms.gelly.connectedcomponents.WeaklyConnectedComponentsAsCollection;
import org.gradoop.flink.io.api.DataSink;
import org.gradoop.flink.io.impl.dot.DOTDataSink;
import org.gradoop.flink.model.impl.epgm.GraphCollection;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.util.FlinkAsciiGraphLoader;
import org.gradoop.flink.util.GradoopFlinkConfig;

public class GradoopQuickstart {

  /**
   * Simple Gradoop Example that walks through the process of loading data, doing a simple graph
   * transformation and storing the results
   *
   * @param args
   */
  public static void main(String[] args) throws Exception {

    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    GradoopFlinkConfig cfg = GradoopFlinkConfig.createConfig(env);

    // Create the sample Graph
    String graph = "g1:graph[" +
      "(p1:Person {name: \"Bob\", age: 24})-[:friendsWith]->" +
      "(p2:Person{name: \"Alice\", age: 30})-[:friendsWith]->(p1)" +
      "(p2)-[:friendsWith]->(p3:Person {name: \"Jacob\", age: 27})-[:friendsWith]->(p2) " +
      "(p3)-[:friendsWith]->(p4:Person{name: \"Marc\", age: 40})-[:friendsWith]->(p3) " +
      "(p4)-[:friendsWith]->(p5:Person{name: \"Sara\", age: 33})-[:friendsWith]->(p4) " +
      "(c1:Company {name: \"Acme Corp\"}) " +
      "(c2:Company {name: \"Globex Inc.\"}) " +
      "(p2)-[:worksAt]->(c1) " +
      "(p4)-[:worksAt]->(c1) " +
      "(p5)-[:worksAt]->(c1) " +
      "(p1)-[:worksAt]->(c2) " +
      "(p3)-[:worksAt]->(c2) " + "] " +
      "g2:graph[" +
      "(p4)-[:friendsWith]->(p6:Person {name: \"Paul\", age: 37})-[:friendsWith]->(p4) " +
      "(p6)-[:friendsWith]->(p7:Person {name: \"Mike\", age: 23})-[:friendsWith]->(p6) " +
      "(p8:Person {name: \"Jil\", age: 32})-[:friendsWith]->(p7)-[:friendsWith]->(p8) " +
      "(p6)-[:worksAt]->(c2) " +
      "(p7)-[:worksAt]->(c2) " +
      "(p8)-[:worksAt]->(c1) " + "]";

    FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
    loader.initDatabaseFromString(graph);

    GraphCollection c1 = loader.getGraphCollectionByVariables("g1", "g2");
    DataSink sink1 = new DOTDataSink("out/input.dot", true);
    c1.writeTo(sink1);


    // OVERLAP
    LogicalGraph n1 = loader.getLogicalGraphByVariable("g1");
    LogicalGraph n2 = loader.getLogicalGraphByVariable("g2");
    LogicalGraph overlap = n2.overlap(n1);
    DataSink sink2 = new DOTDataSink("out/overlap.dot", true);
    overlap.writeTo(sink2);

    // WORKS AT
    LogicalGraph workGraph = n1.combine(n2)
      .subgraph(
        v -> true,
        e -> e.getLabel().equals("worksAt"));

    WeaklyConnectedComponentsAsCollection weaklyConnectedComponents = new WeaklyConnectedComponentsAsCollection (10);
    GraphCollection components = weaklyConnectedComponents.execute(workGraph);

    DataSink sink3 = new DOTDataSink("out/workspace.dot", true);
    components.writeTo(sink3);

    env.execute();
  }

}
