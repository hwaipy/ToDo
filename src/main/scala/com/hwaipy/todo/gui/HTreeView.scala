package com.hwaipy.todo.gui

import scala.collection.mutable
import scalafx.scene.control.{ScrollPane, TreeItem}
import javafx.scene.{control => jfxsc}

import com.hwaipy.todo.action.ObservableAction

import scalafx.scene.layout.{AnchorPane, Region, VBox}
import scala.collection.JavaConverters._

class HTreeView[T](root: TreeItem[T], cellFactory: (TreeItem[T]) => Region) extends AnchorPane {
  private val cellMap = new mutable.HashMap[jfxsc.TreeItem[T], Region]()
  println("Warning: need to check if T is deleted, the Region should be deleted too.")

  createCells(root.delegate)

  private def createCells(treeItem: jfxsc.TreeItem[T]): Unit = {
    cellMap(treeItem) = cellFactory(new TreeItem[T](treeItem))
    treeItem.getChildren().asScala.foreach(createCells)
  }

  val scrollPane = new ScrollPane()
  children = Seq(scrollPane)
  AnchorPane.setAnchors(scrollPane, 0, 0, 0, 0)

  val vbox = new VBox(0)
  scrollPane.content = vbox

  vbox.children = cellMap.values.toSeq
}

class ProjectViewTreeCell(treeItem: jfxsc.TreeItem[ObservableAction]) extends Region {
  def this(item: TreeItem[ObservableAction]) = this(item.delegate)

  prefWidth = 400
  prefHeight = 25
  this.getStyleClass.add("project-view-tree-cell")
}