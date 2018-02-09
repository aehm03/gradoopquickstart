## Gradoop Quickstart

To get started with [Gradoop](http://www.gradoop.com), we will walk through the setup, basic 
operations on a simple grap and importing / exporting data to and from gradoop. You can find the whole sourcecode of the quickstart here.

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
In the following examples we will work with a rather simple graph, so each operation and its results can easily be understood and visualized. Consider the following graph collections that consists of three logical graphs, describing two groups of friends:

TODO: insert image

### Basic Graph Operations


