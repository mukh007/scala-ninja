//package org.mj.trie
//
//import scala.annotation.tailrec
//import scala.collection.JavaConverters._
//import scala.collection.mutable
//import scala.collection.mutable.ListBuffer
//
//object Trie {
//  def apply() : Trie[String] = new TrieNode[String]()
//}
//
//sealed trait Trie[T] extends Traversable[T] {
//
//  def append(key : T)
//  def findByPrefix(prefix: T): scala.collection.Seq[T]
//  def contains(word: T): Boolean
//  def remove(word : T) : Boolean
//
//}
//
//private[trie] class TrieNode[T](val char : Option[Char] = None, var word: Option[T] = None) extends Trie[T] {
//
//  private[trie] val children: mutable.Map[Char, TrieNode[T]] = new java.util.TreeMap[Char, TrieNode[T]]().asScala
//
//  override def append(key: T) = {
//
//    @tailrec def appendHelper(node: TrieNode[T], currentIndex: Int): Unit = {
//      if (currentIndex == key.length) {
//        node.word = Some(key)
//      } else {
//        val char = key.charAt(currentIndex).toLower
//        val result = node.children.getOrElseUpdate(char, {
//          new TrieNode(Some(char))
//        })
//
//        appendHelper(result, currentIndex + 1)
//      }
//    }
//
//    appendHelper(this, 0)
//  }
//
//  override def foreach[U](f: String => U): Unit = {
//
//    @tailrec def foreachHelper(nodes: TrieNode[T]*): Unit = {
//      if (nodes.size != 0) {
//        nodes.foreach(node => node.word.foreach(f))
//        foreachHelper(nodes.flatMap(node => node.children.values): _*)
//      }
//    }
//
//    foreachHelper(this)
//  }
//
//  override def findByPrefix(prefix: String): scala.collection.Seq[String] = {
//
//    @tailrec def helper(currentIndex: Int, node: TrieNode[T], items: ListBuffer[String]): ListBuffer[T] = {
//      if (currentIndex == prefix.length) {
//        items ++ node
//      } else {
//        node.children.get(prefix.charAt(currentIndex).toLower) match {
//          case Some(child) => helper(currentIndex + 1, child, items)
//          case None => items
//        }
//      }
//    }
//
//    helper(0, this, new ListBuffer[T]())
//  }
//
//  override def contains(word: String): Boolean = {
//
//    @tailrec def helper(currentIndex: Int, node: TrieNode[T]): Boolean = {
//      if (currentIndex == word.length) {
//        node.word.isDefined
//      } else {
//        node.children.get(word.charAt(currentIndex).toLower) match {
//          case Some(child) => helper(currentIndex + 1, child)
//          case None => false
//        }
//      }
//    }
//
//    helper(0, this)
//  }
//
//  override def remove(word : String) : Boolean = {
//
//    pathTo(word) match {
//      case Some(path) => {
//        var index = path.length - 1
//        var continue = true
//
//        path(index).word = None
//
//        while ( index > 0 && continue ) {
//          val current = path(index)
//
//          if (current.word.isDefined) {
//            continue = false
//          } else {
//            val parent = path(index - 1)
//
//            if (current.children.isEmpty) {
//              parent.children.remove(word.charAt(index - 1).toLower)
//            }
//
//            index -= 1
//          }
//        }
//
//        true
//      }
//      case None => false
//    }
//
//  }
//
//  private[trie] def pathTo( word : T ) : Option[ListBuffer[TrieNode[T]]] = {
//
//    def helper(buffer : ListBuffer[TrieNode[T]], currentIndex : Int, node : TrieNode[T]) : Option[ListBuffer[TrieNode[T]]] = {
//      if ( currentIndex == word.length) {
//        node.word.map( word => buffer += node )
//      } else {
//        node.children.get(word.charAt(currentIndex).toLower) match {
//          case Some(found) => {
//            buffer += node
//            helper(buffer, currentIndex + 1, found)
//          }
//          case None => None
//        }
//      }
//    }
//
//    helper(new ListBuffer[TrieNode[T]](), 0, this)
//  }
//
//  override def toString() : String = s"Trie(char=${char},word=${word})"
//
//}
