import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.io.api.DataSink;
import org.gradoop.flink.io.impl.dot.DOTDataSink;
import org.gradoop.flink.model.api.epgm.GraphCollection;
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
    String graph = "n1[(p1:Person {name: \"Bob\", age: 24})-[:friend]->" +
      "(p2:Person{name: \"Alice\", age: 30})-[:friend]->(p1)" +
      "(p2)-[:friend]->(p3:Person {name: \"Jacob\", age: 27})-[:friend]->(p2) " +
      "(p3)-[:friend]->(p4:Person{name: \"Marc\", age: 40})-[:friend]->(p3) " +
      "(p4)-[:friend]->(p5:Person{name: \"Sara\", age: 33})-[:friend]->(p4) " +
      "(p5)-[:friend]->(p4)-[:friend]->(p5) " + "(c1:Corporation {name: \"Acme Corp\"}) " +
      "(c2:Corporation {name: \"Globex Inc.\"}) " + "(p5)-[:worksAt]->(c1) " +
      "(p3)-[:worksAt]->(c1) " + "(p2)-[:worksAt]->(c1) " + "(p1)-[:worksAt]->(c2) " +
      "(p4)-[:worksAt]->(c2) " + "] " +
      "n2[(p4)-[:friend]->(p6:Person {name: \"Paul\", age: 37})-[:friend]->(p4) " +
      "(p3)-[:friend]->(p7:Person {name: \"Mike\", age: 23})-[:friend]->(p3) " +
      "(p6)-[:friend]->(p7)-[:friend]->(p6) " +
      "(p8:Person {name: \"Jil\", age: 22})-[:friend]->(p7)-[:friend]->(p8) " +
      "(p6)-[:worksAt]->(c2) " + "(p7)-[:worksAt]->(c2) " + "(p8)-[:worksAt]->(c1) " + "]";

    FlinkAsciiGraphLoader loader = new FlinkAsciiGraphLoader(cfg);
    loader.initDatabaseFromString(graph);

    GraphCollection c1 = loader.getGraphCollectionByVariables("n1", "n2");
    DataSink sink = new DOTDataSink("out/n1", true);
    c1.writeTo(sink);

    env.execute();
  }
}
