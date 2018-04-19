package org.mj.scala

import org.mj.java.JavaMain

object ScalaMain {

  def call(): Unit = { println("This is a call to " + this.getClass.getSimpleName) }
  
  def main(args: Array[String]): Unit = {
    println("Inside ScalaMain Start")
    JavaMain.call()
    println("Inside ScalaMain End")
  }
}