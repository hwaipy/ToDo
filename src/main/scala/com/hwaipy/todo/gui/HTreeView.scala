package com.hwaipy.todo.gui

import java.util.concurrent.Executors

import scalafx.scene.control._
import com.hwaipy.todo.action.ObservableAction

import scala.concurrent.{ExecutionContext, Future}
import scalafx.scene.layout.{AnchorPane, Region}
import scalafx.scene.input.{KeyCode, MouseButton}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.DoubleProperty
import scalafx.scene.shape.Rectangle
import javafx.scene.{control => jfxsc}

import scala.collection.mutable

//class HTreeView[T](root: TreeItem[T], cellFactory: (TreeItem[T]) => Region, onSelection: (T) => Unit) extends AnchorPane {
//  println("Warning: need to check if T is deleted, the Region should be deleted too.")
//
//  private val intent = 15
//  private val scrollAnimationDuration = 1000
//  private val contentPane = new ExpandablePane(root, false)
//
//  val scrollPane = new ScrollPane(new javafx.scene.control.ScrollPane() {
//    override def requestFocus(): Unit = {}
//  })
//  children = Seq(scrollPane)
//  scrollPane.focusTraversable = false
//  AnchorPane.setAnchors(scrollPane, 0, 0, 0, 0)
//  scrollPane.content = contentPane
//
//  private class ExpandablePane(val treeItem: TreeItem[T], isRoot: Boolean = false) extends AnchorPane {
//    val parentPane = if (isRoot) {
//      val pane = new AnchorPane
//      pane.minWidth = 0
//      pane.prefWidth = 0
//      pane.maxWidth = Double.MaxValue
//      pane.minHeight = 0
//      pane.prefHeight = 0
//      pane.maxHeight = 0
//      pane
//    } else {
//      val cell = cellFactory(treeItem)
//      cell.focused.onChange((a, b, c) => if (c) onSelection(treeItem.value.value))
//      cell
//    }
//    val childPanes = treeItem.children.map(c => new ExpandablePane(c)).toList
//    val currentIntent = if (isRoot) 0 else intent
//
//    val childrenPane = new ScrollyFadablePane(childPanes, treeItem.value.value.toString)
//    children = List(childrenPane, parentPane)
//
//    treeItem.expanded.onChange((a, b, c) => {
//      if (c) childrenPane.scrollOut else childrenPane.scrollIn
//    })
//    childrenPane.prefHeight.onChange((a, b, c) => reLayout)
//    childrenPane.visibleFractionProperty.onChange((a, b, c) => reLayout)
//    val clipRec = new Rectangle
//    clipRec.x = 0
//    clipRec.y = 0
//    clipRec.width = Double.MaxValue
//    clipRec.height <== prefHeight
//    clip = clipRec
//    reLayout
//
//    def reLayout = {
//      AnchorPane.setTopAnchor(parentPane, 0)
//      AnchorPane.setLeftAnchor(parentPane, 0)
//      AnchorPane.setRightAnchor(parentPane, 0)
//      AnchorPane.setTopAnchor(childrenPane, parentPane.prefHeight.value - childrenPane.prefHeight.value * (1 - childrenPane.visibleFractionProperty.value))
//      println(s"setting: ${treeItem.value.value},   ${parentPane.prefHeight.value - childrenPane.prefHeight.value * (1 - childrenPane.visibleFractionProperty.value)}")
//      AnchorPane.setLeftAnchor(childrenPane, currentIntent)
//      AnchorPane.setRightAnchor(childrenPane, 0)
//      prefHeight = parentPane.prefHeight.value + childrenPane.prefHeight.value * childrenPane.visibleFractionProperty.value
//    }
//  }
//
//  private class ScrollyFadablePane(contents: List[Region], val idd: String) extends AnchorPane {
//    children = contents
//    contents.foreach(content => content.prefHeight.onChange((a, b, c) => {
//      Platform.runLater(() => reLayout)
//    }))
//    val visibleFractionProperty = DoubleProperty(1.0)
//    reLayout
//
//    def reLayout = {
//      val totalHeight = contents.foldRight(0.0) { (c, y) => {
//        AnchorPane.setBottomAnchor(c, y)
//        AnchorPane.setLeftAnchor(c, 0)
//        AnchorPane.setRightAnchor(c, 0)
//        y + c.prefHeight.value
//      }
//      }
//      prefHeight = totalHeight
//    }
//
//    def scrollIn = {
//      println("scroll in")
//      scroll(1, 0)
//    }
//
//    def scrollOut = {
//      println("scroll out")
//      scroll(0, 1)
//    }
//
//    private def scroll(start: Double, stop: Double) = {
//      Future {
//        val startTime = System.nanoTime
//        val stopTime = startTime + scrollAnimationDuration.toLong * 1000000
//        var pastTime = 0
//        do {
//          Platform.runLater(() => {
//            val pastTime = (System.nanoTime - startTime) / 1000000.0
//            prefHeight
//            visibleFractionProperty.value = ((stop - start) * pastTime / scrollAnimationDuration + start)
//            reLayout
//          })
//          Thread.sleep(25)
//        } while (System.nanoTime < stopTime)
//        Platform.runLater(() => {
//          visibleFractionProperty.value = stop
//          reLayout
//        })
//      }(context)
//    }
//
//    //
//    //  def regionToString(region: Region) = {
//    //    region match {
//    //      case exp: ExpandablePane => s"ExpandablePane [${exp.treeItem.value.value}]"
//    //      case fad: ScrollyFadablePane => s"ScrollyFadablePane [${fad.idd}]"
//    //      case r: Region => r.toString
//    //      case _ => ""
//    //    }
//    //  }
//
//    private val context = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor((r) => {
//      val thread = new Thread(r)
//      thread.setDaemon(true)
//      thread
//    }))
//  }
//
//}
//
//class HTreeViewCell[T](treeItem: TreeItem[T]) extends AnchorPane {
//  prefWidth = 400
//  prefHeight = 25
//  focusTraversable = true
//  onMouseClicked = (me: MouseEvent) => {
//    requestFocus()
//    treeItem.expanded = !treeItem.expanded.value
//  }
//}
//
//class ProjectViewTreeViewCell(treeItem: TreeItem[ObservableAction]) extends HTreeViewCell[ObservableAction](treeItem) {
//  prefWidth = 400
//  prefHeight = 25
//  this.getStyleClass.add("project-view-tree-cell")
//
//  focused.onChange((a, b, c) => {
//    //    prefHeight = if (c) Random.nextInt(200) + 30 else 25
//  })
//
//  val label = new Label
//  label.text <==> treeItem.value().title
//  AnchorPane.setAnchors(label, 0, 0, 0, 0)
//
//  children = Seq(label)
//}
//
//class ScrollTestPane extends AnchorPane {
//  prefHeight = 100
//  prefWidth = 300
//  style = "-fx-background-color: blue"
//  onMouseClicked = (me: MouseEvent) => {
//    action
//  }
//
//  val fadable = new AnchorPane
//  fadable.style = "-fx-background-color: green"
//  AnchorPane.setTopAnchor(fadable, 80)
//  AnchorPane.setLeftAnchor(fadable, 30)
//  AnchorPane.setRightAnchor(fadable, 0)
//  fadable.prefHeight = 100
//
//  children = Seq(fadable)
//
//  def action = {
//    println("action")
//    fadable.prefHeight = 10
//  }
//}

object ProjectTreeView {
  def create(rootItem: TreeItem[ObservableAction]) = {
    val projectTreeView = new TreeView[ObservableAction](rootItem)
    projectTreeView.showRoot = false
    projectTreeView.id = "project-treeview"
    projectTreeView.onKeyPressed = (ke) => {
      println(ke.getCode.getName)
      if (ke.getCode.getName == KeyCode.BackSpace.getName) {
        val selectedItem = projectTreeView.selectionModel.value.getSelectedItem
        if (selectedItem != null) {
          val tobeDeletedAction = selectedItem.getValue.action
val alert =           new Alert()

          Alert alert = new Alert(AlertType.CONFIRMATION, "Delete " + selection + " ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
          alert.showAndWait();

          if (alert.getResult() == ButtonType.YES) {
            //do stuff
          }

          //          tobeDeletedAction.actionSet.eventDeleteAction(tobeDeletedAction.id)
        }
      }
    }
    println("Warning: The Scrollbar should be hiden.")
    projectTreeView.cellFactory = ProjectViewTreeCellFactory.create
    projectTreeView
  }
}

object ProjectViewTreeCellFactory {
  def create: TreeView[ObservableAction] => TreeCell[ObservableAction] = {
    (treeView) => {
      new javafx.scene.control.TreeCell[ObservableAction] {
        getStyleClass.add("project-view-tree-cell")
        var bindedItem: ObservableAction = null
        val treeCell = new ProjectViewTreeCellGraphic(treeView)

        override def updateItem(item: ObservableAction, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          if (bindedItem != null) treeCell.unbind
          if (empty) {
            bindedItem = null
            setGraphic(null)
          } else {
            treeCell.bind(item)
            setGraphic(treeCell)
          }
        }
      }
    }
  }
}

class ProjectViewTreeCellGraphic(treeView: TreeView[ObservableAction]) extends AnchorPane {
  styleClass.add("project-view-tree-cell-graphic")
  var bindedObservableAction: ObservableAction = null
  prefWidth = 100
  prefHeight = 20
  val label = new Label
  val editingTextField = new TextField
  editingTextField.onKeyTyped = (ke) => {
    if (ke.getCharacter == "\r") tryExitEditingMode
  }
  AnchorPane.setAnchors(label, 0, 0, 0, 0)
  children = Seq(label)
  var editingMode = false
  val selectionModel = treeView.selectionModel.value
  selectionModel.selectedItemProperty.onChange((a, b, c) => tryExitEditingMode)

  def bind(observableAction: ObservableAction) = {
    bindedObservableAction = observableAction
    label.text <== observableAction.title
  }

  def unbind = {
    label.text.unbind(bindedObservableAction.title)
    bindedObservableAction = null
  }

  onMouseClicked = (me) => {
    if (me.getButton == MouseButton.Primary.delegate && me.getClickCount == 2) {
      editingMode = true
      editingTextField.text = label.text.value
      children.add(editingTextField)
      AnchorPane.setAnchors(editingTextField, 0, 0, 0, 0)
      label.visible = false
      editingTextField.requestFocus()
      editingTextField.selectAll()
    }
  }

  def tryExitEditingMode = if (editingMode) {
    editingMode = false
    if (editingTextField.text.value != label.text.value && bindedObservableAction != null) {
      val modifiedAction = bindedObservableAction.action
      modifiedAction.actionSet.eventModifyActionTitle(modifiedAction.id, editingTextField.text.value)
    }
    children.remove(editingTextField)
    label.visible = true
  }
}