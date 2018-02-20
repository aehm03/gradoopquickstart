import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.algorithms.gelly.connectedcomponents.WeaklyConnectedComponents;
import org.gradoop.flink.io.api.DataSink;
import org.gradoop.flink.io.impl.dot.DOTDataSink;
import org.gradoop.flink.model.api.epgm.GraphCollection;
import org.gradoop.flink.model.api.epgm.LogicalGraph;
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
    // TODO validate
    String graph = "g1:graph[(p1:Person {name: \"Bob\", age: 24})-[:friendsWith]->" +
      "(p2:Person{name: \"Alice\", age: 30})-[:friendsWith]->(p1)" +
      "(p2)-[:friendsWith]->(p3:Person {name: \"Jacob\", age: 27})-[:friendsWith]->(p2) " +
      "(p3)-[:friendsWith]->(p4:Person{name: \"Marc\", age: 40})-[:friendsWith]->(p3) " +
      "(p4)-[:friendsWith]->(p5:Person{name: \"Sara\", age: 33})-[:friendsWith]->(p4) " +
      "(p5)-[:friendsWith]->(p4)-[:friendsWith]->(p5) " +
      "(c1:Company {name: \"Acme Corp\"}) " +
      "(c2:Company {name: \"Globex Inc.\"}) " + "(p5)-[:worksAt]->(c1) " +
      "(p3)-[:worksAt]->(c1) " +
      "(p2)-[:worksAt]->(c1) " +
      "(p1)-[:worksAt]->(c2) " +
      "(p4)-[:worksAt]->(c2) " + "] " +
      "g2:graph[(p4)-[:friendsWith]->(p6:Person {name: \"Paul\", age: 37})-[:friendsWith]->(p4) " +
      "(p3)-[:friendsWith]->(p7:Person {name: \"Mike\", age: 23})-[:friendsWith]->(p3) " +
      "(p6)-[:friendsWith]->(p7)-[:friendsWith]->(p6) " +
      "(p8:Person {name: \"Jil\", age: 32})-[:friendsWith]->(p7)-[:friendsWith]->(p8) " +
      "(p6)-[:worksAt]->(c2) " +
      "(p7)-[:worksAt]->(c2) " +
      "(p8)-[:worksAt]->(c1) " + "]";

    FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
    loader.initDatabaseFromString(graph);

    GraphCollection c1 = loader.getGraphCollectionByVariables("g1", "g2");

    // OVERLAP

    LogicalGraph n1 = loader.getLogicalGraphByVariable("g1");
    LogicalGraph n2 = loader.getLogicalGraphByVariable("g2");
    LogicalGraph overlap = n2.overlap(n1);

    DataSink sink = new DOTDataSink("out/n1", true);
    //overlap.writeTo(sink);
    c1.writeTo(sink);
    // Why are there no edges in the overlap?
    // Because both nodes are in the overlap but the relations are actually only in one

    // show how to overlap vertices and get all edges?

    LogicalGraph wholeGraph = n1.combine(n2);
    LogicalGraph workGraph = wholeGraph.subgraph(
      v -> true,
      e -> e.getLabel().equals("worksAt"));



    WeaklyConnectedComponents weaklyConnectedComponents = new WeaklyConnectedComponents(10);
    GraphCollection res = weaklyConnectedComponents.execute(workGraph);

    //writeAsDotGraph("out/matches", matches);
    DataSink sink2 = new DOTDataSink("out/matches", true);
    res.writeTo(sink2);


    env.execute();
  }

}
