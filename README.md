## Gradoop Quickstart

To get started with [Gradoop](http://www.gradoop.com), we will walk through the setup, basic 
operators on a simple grap and importing / exporting data to and from gradoop. You can find the 
whole sourcecode of the quickstart [here](source/main/java/GradoopQuickstart.java).

### Setup

The easiest way to use gradoop in your project is via maven. Add the following repository to your project file:
```
<repositories>
  <repository>
    <id>dbleipzig</id>
    <name>Database Group Leipzig University</name>
    <url>https://wdiserv1.informatik.uni-leipzig.de:443/archiva/repository/dbleipzig/</url>
    <releases>
      <enabled>true</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
   </repository>
</repositories>
```
Now maven can resolve the gradoop dependency:
```
<dependency>
  <groupId>org.gradoop</groupId>
  <artifactId>gradoop-flink</artifactId>
  <version>0.3.0-SNAPSHOT</version>
</dependency>
```
You will also need Apache Flink (TODO explain why,version)
```
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-java</artifactId>
    <version>1.3.1</version>
</dependency>
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-clients_2.11</artifactId>
    <version>1.3.1</version>
</dependency>
```
Now you are able to run your first gradoop application!

### Creating a sample Graph
In the following examples we will work with a rather simple graph, so each operation and its 
results can easily be understood and visualized. Consider the following graph collections that consists of three logical graphs, describing two groups of friends.

As you can see, there are two types of vertices in our graph collection: _Person_ and _Company_. 
There are also two types of edges: _friend_, denoting that a given _Person_ is friends with 
another, and _worksAt_, a relation between a _Person_ and a _Company_. 
In the Extended Property Graph Model (EPGM), these types are called _labels_.

Each _Person_ has two characteristics a name and and an age, while our companys onyle have a
 name as a property. These key-value pairs are the _properties_ of a graph element. Note that 
 there is no schema involved that forces a certain type of graph element to have specific values.
 E.g. there could be a third company vertex which also holds information about the size of 
 the company or a person with no key-value pair age. 
 
 Both groups of friends make up a separate graph, those are called _logical graph_ in EPGM. Graph
  elements can belong to more than a single logical graph Marc and Jacob belong to both groups 
  and since in each group there is at least one person working at each company, they also belong 
  to both logical graphs.  
 
 A set of logical graphs is called a _graph collection_. That's everything we need to know about 
 EPGM for now, if you want to gain a deeper understanding, have a look at [the paper](http://dbs
 .uni-leipzig.de/file/EPGM.pdf).

TODO: insert image

Now lets have a look on how we can create such a graph in gradoop:

// TODO link sourcecode

``` 
// Create the sample Graph
    String graph = "n1[(p1:Person {name: \"Bob\", age: 24})-[:friend]->" +
      "(p2:Person{name: \"Alice\", age: 30})-[:friend]->(p1)" +
      "(p2)-[:friend]->(p3:Person {name: \"Jacob\", age: 27})-[:friend]->(p2) " +
      "(p3)-[:friend]->(p4:Person{name: \"Marc\", age: 40})-[:friend]->(p3) " +
      "(p4)-[:friend]->(p5:Person{name: \"Sara\", age: 33})-[:friend]->(p4) " +
      "(p5)-[:friend]->(p4)-[:friend]->(p5) " + "(c1:company {name: \"Acme Corp\"}) " +
      "(c2:company {name: \"Globex Inc.\"}) " + "(p5)-[:worksAt]->(c1) " +
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
```

To create our example data we use the GDL format, which is rather easy to understand and allows 
to build small graph collections on the fly. You can find detailed information about GDL on 
[github](https://github.com/s1ck/gdl). Almost all real world data won't be available in this 
format, but we will cover data importing further on and use this simple example to familiarize 
with the gradoop operators.

### Basic Graph Operators


