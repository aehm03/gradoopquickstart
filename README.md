## Gradoop Quickstart

To get started with [Gradoop](http://www.gradoop.com), we will walk through the setup, and 
uss some basic operators on an example graph You can find the whole sourcecode of the quickstart [here](src/main/java/GradoopQuickstart.java).

### Setup

The easiest way to use gradoop in your project is via maven. Add the following repository to your project file:

```
<dependency>
    <groupId>org.gradoop</groupId>
    <artifactId>gradoop-flink</artifactId>
    <version>0.3.2</version>
</dependency>
```
You also need Apache Flink (version 1.3.1.):
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
Now you are able to run your gradoop application.

### Creating an example graph
In the following examples we will work with a rather simple graph, so each operation and its 
results can easily be understood and visualized. Consider the following graph collections that consists of three logical graphs, describing two groups of friends.

As you can see, there are two types of vertices in our graph collection: _Person_ and _Company_. 
There are also two types of edges: _friend_, denoting that a given _Person_ is friends with 
another, and _worksAt_, a relation between a _Person_ and a _Company_. 
In the Extended Property Graph Model (EPGM), these types are called _labels_.

![example graph](images/example-graph.png)

Each _Person_ has two characteristics a name and and an age, while our companys onyle have a
 name as a property. These key-value pairs are the _properties_ of a graph element. Note that 
 there is no schema involved that forces a certain type of graph element to have specific values.
 E.g. there could be a third company vertex which also holds information about the size of 
 the company or a person with no key-value pair age. 
 
 Both groups of friends make up a separate graph, those are called _logical graph_ in EPGM. Graph
  elements can belong to more than one logical graph. The vertices Marc and Jacob belong to both 
  groups  and since in each group there is at least one person working at each company, they also belong 
  to both logical graphs.  
 
 A set of logical graphs is called a _graph collection_. That's everything we need to know about 
 EPGM for now, if you want to gain a deeper understanding, have a look at the [paper](http://dbs.uni-leipzig.de/file/EPGM.pdf).    


To create the example data we use the GDL format, which is easy to understand and allows 
to build small graph collections from strings. Basically a vertex is given by a pair of 
parentheses `()`and edges are represented with arrow-like symbols `-[]->`. All 
graph elements can be referred to by a variable name, given a label and key-value pairs. Let's have a look at the source code:

```java 
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
```
Note that the edges of Type friend in our example are undirected, to model this property we 
have to use two mirrored directed edges.  
You can find detailed information about GDL on [github](https://github.com/s1ck/gdl).
Obviously real world data won't be available in this format. We will cover data importing 
later on and use this example to familiarize  with the gradoop operators.

### Graph Operators
With the overlap operator we can find all elements that are contained in both our logical 
graphs:
```java
LogicalGraph n1 = loader.getLogicalGraphByVariable("g1");
LogicalGraph n2 = loader.getLogicalGraphByVariable("g2");
LogicalGraph overlap = n2.overlap(n1);
DataSink sink2 = new DOTDataSink("out/overlap.dot", true);
overlap.writeTo(sink2);
```
To review and validate the results we use the `DOTDataSink` to write the result graphs in 
the dot format which can be compiled into an image with [Graphviz](https://www.graphviz.org/). 

![overlap graph](images/overlap.png)

The overlap graphs contains all elements that are present in both logical graphs. The fact that 
even Marcs work relation with Acme Corp. is not part of the overlap may come as a surprise since 
both vertices are present. However, notice the respective edge was only defined in g1.
The remaining set operators combination, exclusion and equality are straightforward to
 understand from here. You can try them out and observe the results with the method given above. 

### Creating a company based graph 

Let's try a slightly more complicated example. How can we transform our current input graphs to 
achieve two graphs that show the members of each company? At first we need to combine the existing 
graphs to a single graph and use the `subgraph` operator to filter out all non work-related edges:
```java
LogicalGraph workGraph = n1.combine(n2)
      .subgraph(
        v -> true,
        e -> e.getLabel().equals("worksAt"));
```

Now we want to find all parts of our graph that form a connected component. To achieve this we 
can use the `WeaklyConnectedComponents` algorithm from the [Flink Gelly library](https://flink.apache.org/news/2015/08/24/introducing-flink-gelly.html) which is available in gradoop.  
```java
WeaklyConnectedComponents weaklyConnectedComponents = new WeaklyConnectedComponents(10);
GraphCollection components = weaklyConnectedComponents.execute(workGraph);

DataSink sink3 = new DOTDataSink("out/workspace.dot", true);
components.writeTo(sink3);
```

Using the datasource again, we can validate our results.

![workspace graph](images/workspace.png)


Congratulations, you solved your first graph processing problem with gradoop. 


### Next Steps 

* Review the [examples](https://github.com/dbs-leipzig/gradoop/tree/master/gradoop-examples)
* Learn about advanced methods e.g. Cypher Queries, ...
* How to import data from different sources