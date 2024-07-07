import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.collection.mutable

object Main {
  def main(args: Array[String]): Unit = {
    val browser = JsoupBrowser()

    // 1.) Fetch and count references from all 19 sections of the JLS, returning a
    // mutable map where each (k,v) pair represents a section number and its reference count
    val sectionsReferenceCountsMap = (1 to 19).map { sectionNum =>
      val sectionUrl = s"https://docs.oracle.com/javase/specs/jls/se22/html/jls-${sectionNum}.html"
      val doc = browser.get(sectionUrl)

      // Extract references ('§') from HTML
      val references = doc >> elementList("a.xref")
      val referencesCount = references.size

      // Return section number and reference count as a tuple
      sectionNum -> referencesCount
    }.toMap

    // 2.) Construct a directed graph based on section reference counts
    val graph = constructGraph(sectionsReferenceCountsMap)

    println("ADJACENCY LIST REPRESENTATION (SECTION -> DEPENDENCIES):")
    graph.foreach { case (section, dependencies) =>
      println(s"$section -> ${dependencies.mkString(", ")}")
    }

    // 3.) Get list of nodes in topological order
    val topologicalOrderedNodesList = performTopologicalSorting(graph)

    println("\nTOPOLOGICAL SORTING ORDER:")
    println(topologicalOrderedNodesList.mkString(" -> "))
  }

  // Function that constructs an adjacency list representation of the graph
  def constructGraph(sectionsReferenceCountsMap: scala.collection.Map[Int, Int]): Map[Int, List[Int]] = {
    val adjList = mutable.Map[Int, List[Int]]().withDefaultValue(List())

    // Iterate over each pair of entries, add edges based on ref counts
    sectionsReferenceCountsMap.foreach { case (sectionA, countA) =>
      sectionsReferenceCountsMap.foreach { case (sectionB, countB) =>
        if (sectionA != sectionB && countA > countB) {
          // Add directed edge sectionB to the adjacency list of sectionA
          adjList(sectionA) = sectionB :: adjList(sectionA)
        }
      }
    }
    adjList.toMap
  }

  // Function that takes a directed graph represented as an adjacency list and
  // performs DFS to return a list of nodes in topological order
  def performTopologicalSorting(adjList: Map[Int, List[Int]]): List[Int] = {
    // Keep track of nodes that have been visited during DFS
    val visited = mutable.Set[Int]()

    val topologicalOrderedNodes = mutable.ListBuffer[Int]()

    def dfs(vertex: Int): Unit = {
      visited.add(vertex)
      // Visit all neighbors (dependencies)
      adjList.getOrElse(vertex, List()).foreach { neighbor =>
        if (!visited.contains(neighbor)) {
          dfs(neighbor)
        }
      }
      // Add vertex/node to the topological ordering
      topologicalOrderedNodes.prepend(vertex)
    }

    adjList.keys.foreach { vertex =>
      if (!visited.contains(vertex)) {
        dfs(vertex)
      }
    }
    topologicalOrderedNodes.toList
  }
}