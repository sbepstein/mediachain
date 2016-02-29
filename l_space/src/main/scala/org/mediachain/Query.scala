package org.mediachain

import org.mediachain.Types._

object Query {
  import gremlin.scala._
  import Traversals.VertexTraversals


  /** Finds a vertex with label "Person" and traits matching `p` in the graph
    * `g`.
    *
    * @param graph The OrientGraph object
    * @param p The person to search for
    * @return Optional person matching criteria
    */
  def findPerson(graph: Graph, p: Person): Option[Person] = {

    Traversals.personWithExactMatch(graph.V, p)
      .toCC[Person]
      .headOption()
  }

  def findPhotoBlob(graph: Graph, p: PhotoBlob): Option[PhotoBlob] = {
    Traversals.photoBlobWithExactMatch(graph.V, p)
      .toCC[PhotoBlob]
      .headOption()
  }

  def rootRevisionVertexForBlob[T <: MetadataBlob](graph: Graph, blob: T): Option[Vertex] = {
    graph.V.flatMap(Traversals.getRootRevision)
      .headOption
  }

  def findCanonicalForBlob(graph: Graph, blobID: ElementID): Option[Canonical] = {
    graph.V(blobID).canonicalOption
  }

  def findCanonicalForBlob[T <: MetadataBlob](graph: Graph, blob: T): Option[Canonical] = {
    blob.getID.flatMap(id => findCanonicalForBlob(graph, id))
  }

  def findCanonicalForBlob(graph: Graph, vertex: Vertex): Option[Canonical] = {
    vertexId(vertex).flatMap(findCanonicalForBlob(graph, _))
  }

  def findAuthorForBlob[T <: MetadataBlob](graph: Graph, blob: T): Option[Canonical] = {
    blob.getID.flatMap { id =>
      graph.V(id)
        .untilWithTraverser { t =>
          t.get().out(AuthoredBy).exists() || t.get().in().notExists()
        }
        .repeat(_.in(ModifiedBy))
        .out(AuthoredBy)
        .headOption()
    }.map(Canonical(_))
  }

  def findWorks(graph: Graph, p: Person): Option[List[Canonical]] = {
    val personCanonical = findCanonicalForBlob(graph, p)
    personCanonical
      .flatMap(p => p.vertex(graph))
      .map { v =>
        v.in(AuthoredBy)
        .map(v => findCanonicalForBlob(graph, v))
        .toList
        .flatten
    }
  }
}

