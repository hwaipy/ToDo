package com.hwaipy.todo.gui

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDateTime}
import java.util.{Timer, TimerTask}
import javafx.beans.value.{ChangeListener, ObservableValue}

import scalafx.scene.control._
import com.hwaipy.todo.action.ObservableAction

import scalafx.scene.layout.AnchorPane
import scalafx.scene.input.{KeyCode, MouseButton}
import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, IntegerProperty, ObjectProperty, StringProperty}
import javafx.scene.{control => jfxsc}

import com.hwaipy.todo.input.Input

import scala.collection.mutable.ArrayBuffer
import scalafx.application.Platform

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
      if (ke.getCode.getName == KeyCode.BackSpace.getName) {
        val selectedItem = projectTreeView.selectionModel.value.getSelectedItem
        if (selectedItem != null) {
          val tobeDeletedAction = selectedItem.getValue.action
          val alert = new Alert(Alert.AlertType.Confirmation, s"Project [${tobeDeletedAction.getTitle}] and all sub projects and actions will be deleted. Continue?", ButtonType.Yes, ButtonType.No)
          alert.showAndWait
          if (alert.getResult() == ButtonType.Yes.delegate) {
            println("Warning: Delete parent project will lead an exception on sub project at next time startup.")
            tobeDeletedAction.actionSet.eventDeleteAction(tobeDeletedAction.id)
          }
        }
      }
    }
    DueUpdateRequest.addChangeListener(() => rootItem.value.value.action.actionSet.updateDueCounts)
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

  val textField = new TextField
  val dueLabel = new Label
  val almostDueLabel = new Label
  textField.mouseTransparent = true
  dueLabel.mouseTransparent = true
  almostDueLabel.mouseTransparent = true
  val titleValueBuffer = new StringProperty
  val dueCountBuffer = new IntegerProperty
  val almostDueCountBuffer = new IntegerProperty
  titleValueBuffer.onChange((a, b, c) => textField.text.value = c)
  textField.editable = false
  textField.getStyleClass.add("project-view-tree-cell-graphic-title-text-field")
  dueLabel.getStyleClass.add("project-view-tree-cell-graphic-due-label")
  almostDueLabel.getStyleClass.add("project-view-tree-cell-graphic-almost-due-label")

  textField.onKeyTyped = (ke) => {
    if (ke.getCharacter == "\r") tryExitEditingMode
  }

  children = Seq(textField, dueLabel, almostDueLabel)
  reLayout

  def reLayout = if (bindedObservableAction != null) {
    dueLabel.text = s"${dueCountBuffer.value}"
    almostDueLabel.text = s"${almostDueCountBuffer.value}"
    dueLabel.visible = dueCountBuffer.value > 0
    almostDueLabel.visible = almostDueCountBuffer.value > 0
    AnchorPane.setTopAnchor(almostDueLabel, 0)
    AnchorPane.setBottomAnchor(almostDueLabel, 0)
    AnchorPane.setTopAnchor(dueLabel, 0)
    AnchorPane.setBottomAnchor(dueLabel, 0)
    dueLabel.prefWidth = 20
    almostDueLabel.prefWidth = 20
    AnchorPane.setRightAnchor(dueLabel, 2)
    AnchorPane.setRightAnchor(almostDueLabel, 24)
    AnchorPane.setAnchors(textField, 0, 46, 0, 0)
  }

  var editingMode = false
  val selectionModel = treeView.selectionModel.value
  selectionModel.selectedItemProperty.onChange((a, b, c) => {
    textField.mouseTransparent = c.getValue != bindedObservableAction
    tryExitEditingMode
  })

  def bind(observableAction: ObservableAction) = {
    bindedObservableAction = observableAction
    titleValueBuffer <== observableAction.title
    dueCountBuffer <== observableAction.dueCount
    almostDueCountBuffer <== observableAction.almostDueCount
    reLayout
  }

  def unbind = {
    titleValueBuffer.unbind(bindedObservableAction.title)
    dueCountBuffer.unbind(bindedObservableAction.dueCount)
    almostDueCountBuffer.unbind(bindedObservableAction.almostDueCount)
    bindedObservableAction = null
  }

  textField.onMouseClicked = (me) => {
    if (me.getButton == MouseButton.Primary.delegate) {
      editingMode = true
      textField.editable = true
      textField.requestFocus()
    }
  }

  def tryExitEditingMode = if (editingMode) {
    editingMode = false
    textField.editable = false
    if (bindedObservableAction != null && textField.text.value != bindedObservableAction.title.value) {
      val modifiedAction = bindedObservableAction.action
      modifiedAction.actionSet.eventModifyActionTitle(modifiedAction.id, textField.text.value)
    }
  }

  dueCountBuffer.onChange((a, b, c) => reLayout)
  almostDueCountBuffer.onChange((a, b, c) => reLayout)
}

object ProjectActionTreeView {
  def create(rootItem: TreeItem[ObservableAction]) = {
    val treeView = new TreeView[ObservableAction](rootItem)
    treeView.showRoot = false
    treeView.id = "project-action-treeview"
    treeView.onKeyPressed = (ke) => {
      //      if (ke.getCode.getName == KeyCode.BackSpace.getName) {
      //        val selectedItem = treeView.selectionModel.value.getSelectedItem
      //        if (selectedItem != null) {
      //          val tobeDeletedAction = selectedItem.getValue.action
      //          val alert = new Alert(Alert.AlertType.Confirmation, s"Project [${tobeDeletedAction.getTitle}] and all sub projects and actions will be deleted. Continue?", ButtonType.Yes, ButtonType.No)
      //          alert.showAndWait
      //          if (alert.getResult() == ButtonType.Yes.delegate) {
      //            println("Warning: Delete parent project will lead an exception on sub project at next time startup.")
      //            tobeDeletedAction.actionSet.eventDeleteAction(tobeDeletedAction.id)
      //          }
      //        }
      //      }
    }
    println("Warning: The Scrollbar should be hiden.")
    treeView.cellFactory = ProjectActionViewTreeCellFactory.create
    treeView
  }
}

object ProjectActionViewTreeCellFactory {
  def create: TreeView[ObservableAction] => TreeCell[ObservableAction] = {
    (treeView) => {
      new javafx.scene.control.TreeCell[ObservableAction] {
        getStyleClass.add("project-action-view-tree-cell")
        var bindedItem: ObservableAction = null
        val treeCell = new ProjectActionViewTreeCellGraphic(treeView)

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


class ProjectActionViewTreeCellGraphic(treeView: TreeView[ObservableAction]) extends AnchorPane {
  styleClass.add("project-action-view-tree-cell-graphic")
  var bindedObservableAction: ObservableAction = null
  prefWidth = 100
  prefHeight = 46

  val titleTextField = new TextField
  val contextTextField = new ComboBox[String](Seq("Lab", "Office", "Waiting", "People", "None"))
  val priorityTextField = new ComboBox[String](Seq("Emergency", "Immediate", "Normal", "Opportunity"))
  val dueTextField = new TextField
  val doneCheckBox = new ToggleButton
  doneCheckBox.focusTraversable = false
  val splitLine = new AnchorPane
  titleTextField.mouseTransparent = true
  contextTextField.mouseTransparent = true
  priorityTextField.mouseTransparent = true
  dueTextField.mouseTransparent = true
  priorityTextField.editable = false
  val titleValueBuffer = new StringProperty
  val contextValueBuffer = new StringProperty
  val priorityValueBuffer = new StringProperty
  val dueValueBuffer = new ObjectProperty[LocalDateTime]
  val doneValueBuffer = new BooleanProperty
  titleValueBuffer.onChange((a, b, c) => titleTextField.text.value = c)
  contextValueBuffer.onChange((a, b, c) => contextTextField.value.value = c)
  priorityValueBuffer.onChange((a, b, c) => priorityTextField.selectionModel.value.select(c))
  dueValueBuffer.onChange((a, b, c) => updateDueTextField)
  DueUpdateRequest.addChangeListener(() => updateDueTextField)
  doneValueBuffer.onChange((a, b, c) => doneCheckBox.selected = c)
  titleTextField.getStyleClass.add("project-action-view-tree-cell-graphic-title-text-field")
  contextTextField.getStyleClass.add("project-action-view-tree-cell-graphic-context-text-field")
  priorityTextField.getStyleClass.add("project-action-view-tree-cell-graphic-priority-text-field")
  dueTextField.getStyleClass.add("project-action-view-tree-cell-graphic-due-text-field")
  splitLine.getStyleClass.add("project-action-view-tree-cell-graphic-split-line")
  doneCheckBox.getStyleClass.add("project-action-view-tree-cell-graphic-done-checkbox")

  titleTextField.onKeyTyped = (ke) => if (ke.getCharacter == "\r") tryExitEditingMode
  contextTextField.onKeyTyped = (ke) => if (ke.getCharacter == "\r") tryExitEditingMode
  priorityTextField.onKeyTyped = (ke) => if (ke.getCharacter == "\r") tryExitEditingMode
  dueTextField.onKeyTyped = (ke) => if (ke.getCharacter == "\r") tryExitDueEditingMode
  titleTextField.focused.onChange((a, b, c) => if (!c) tryExitEditingMode)
  contextTextField.focused.onChange((a, b, c) => if (!c) tryExitEditingMode)
  priorityTextField.focused.onChange((a, b, c) => if (!c) tryExitEditingMode)
  dueTextField.focused.onChange((a, b, c) => if (!c) tryExitDueEditingMode)

  titleTextField.prefHeight = 30
  contextTextField.prefHeight = 15
  contextTextField.prefWidth = 100
  priorityTextField.prefHeight = 15
  priorityTextField.prefWidth = 150
  dueTextField.prefHeight = 15
  dueTextField.prefWidth = 150
  splitLine.prefHeight = 1
  doneCheckBox.prefWidth = 32
  doneCheckBox.prefHeight = 32
  AnchorPane.setTopAnchor(titleTextField, 0)
  AnchorPane.setLeftAnchor(titleTextField, 0)
  AnchorPane.setRightAnchor(titleTextField, 50)
  AnchorPane.setTopAnchor(contextTextField, 30)
  AnchorPane.setLeftAnchor(contextTextField, 0)
  AnchorPane.setTopAnchor(priorityTextField, 30)
  AnchorPane.setLeftAnchor(priorityTextField, 100)
  AnchorPane.setTopAnchor(dueTextField, 30)
  AnchorPane.setRightAnchor(dueTextField, 40)
  AnchorPane.setBottomAnchor(splitLine, 0)
  AnchorPane.setLeftAnchor(splitLine, 50)
  AnchorPane.setRightAnchor(splitLine, 50)
  AnchorPane.setTopAnchor(doneCheckBox, 7)
  AnchorPane.setRightAnchor(doneCheckBox, 7)
  children = Seq(titleTextField, contextTextField, priorityTextField, dueTextField, splitLine, doneCheckBox)

  var editingMode = false
  val selectionModel = treeView.selectionModel.value
  selectionModel.selectedItemProperty.onChange((a, b, c) => if (c != null) {
    titleTextField.mouseTransparent = c.getValue != bindedObservableAction
    contextTextField.mouseTransparent = c.getValue != bindedObservableAction
    priorityTextField.mouseTransparent = c.getValue != bindedObservableAction
    dueTextField.mouseTransparent = c.getValue != bindedObservableAction
    tryExitEditingMode
  })

  def bind(observableAction: ObservableAction) = {
    bindedObservableAction = observableAction
    titleValueBuffer <== observableAction.title
    contextValueBuffer <== observableAction.context
    priorityValueBuffer <== observableAction.priority
    dueValueBuffer <== observableAction.due
    doneValueBuffer <== observableAction.isDone
  }

  def unbind = {
    titleValueBuffer.unbind(bindedObservableAction.title)
    contextValueBuffer.unbind(bindedObservableAction.context)
    priorityValueBuffer.unbind(bindedObservableAction.priority)
    dueValueBuffer.unbind(bindedObservableAction.due)
    doneValueBuffer.unbind(bindedObservableAction.isDone)
    bindedObservableAction = null
  }

  titleTextField.onMouseClicked = (me) => {
    if (me.getButton == MouseButton.Primary.delegate) {
      editingMode = true
    }
  }
  contextTextField.onMouseClicked = (me) => {
    if (me.getButton == MouseButton.Primary.delegate) {
      editingMode = true
    }
  }
  priorityTextField.onMouseClicked = (me) => {
    if (me.getButton == MouseButton.Primary.delegate) {
      editingMode = true
    }
  }
  dueTextField.onMouseClicked = (me) => {
    if (me.getButton == MouseButton.Primary.delegate) {
      editingMode = true
      dueTextField.editable = true
    }
  }
  dueTextField.focused.onChange((a, b, c) => if (c) {
    editingMode = true
    dueTextField.editable = true
  })
  doneCheckBox.selected.onChange((a, b, c) => if (bindedObservableAction != null) {
    val modifiedAction = bindedObservableAction.action
    modifiedAction.actionSet.eventModifyActionDone(modifiedAction.id, c)
  })


  def tryExitEditingMode = if (editingMode) {
    editingMode = false
    if (bindedObservableAction != null && titleTextField.text.value != bindedObservableAction.title.value) {
      val modifiedAction = bindedObservableAction.action
      modifiedAction.actionSet.eventModifyActionTitle(modifiedAction.id, titleTextField.text.value)
    }
    if (bindedObservableAction != null && contextTextField.value.value != bindedObservableAction.context.value) {
      val modifiedAction = bindedObservableAction.action
      modifiedAction.actionSet.eventModifyActionContext(modifiedAction.id, contextTextField.value.value)
    }
    if (bindedObservableAction != null && priorityTextField.value.value != bindedObservableAction.priority.value) {
      val modifiedAction = bindedObservableAction.action
      modifiedAction.actionSet.eventModifyActionPriority(modifiedAction.id, priorityTextField.value.value)
    }
  }

  def tryExitDueEditingMode = if (editingMode) {
    editingMode = false
    if (bindedObservableAction != null) {
      val modifiedAction = bindedObservableAction.action
      val newDueString = dueTextField.text.value
      val newDue = Input.stringToDateTime(newDueString)
      modifiedAction.actionSet.eventModifyActionDue(modifiedAction.id, newDue)
    }
    dueTextField.editable = false
  }

  private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def updateDueTextField = if (bindedObservableAction != null) {
    val due = bindedObservableAction.due.value
    val now = LocalDateTime.now
    val delta = Duration.between(now, due).getSeconds
    val displayString = delta match {
      case d if d >= 3600 * 24 => DATE_FORMATTER.format(due)
      case d if d >= 3600 * 3 => s"${delta / 3600}h"
      case d if d >= 3600 => s"${delta / 3600}h ${(delta % 3600) / 60}min"
      case d if d >= 0 => s"${delta / 60}min"
      case d if d >= -3600 => s"${-delta / 60}min ago"
      case d if d >= -3600 * 24 => s"${-delta / 3600}h ago"
      case d if d <= -3600 * 24 * 365 * 10 => s""
      case _ => DATE_FORMATTER.format(due)
    }
    val dueStatus = delta match {
      case d if d > 3600 * 24 => "far-away"
      case d if d > 0 => "nearly"
      case d if d < 0 => "already"
    }
    dueTextField.text = displayString
    dueTextField.getStyleClass.removeAll("due-status-far-away", "due-status-nearly", "due-status-already")
    dueTextField.getStyleClass.add("due-status-" + dueStatus)
  }
}

object DueUpdateRequest {
  private val listeners = new ArrayBuffer[() => Unit]
  private val timer = new Timer(true)
  timer.schedule(new TimerTask {
    override def run(): Unit = Platform.runLater(() => listeners.foreach(_ ()))
  }, 10000, 10000)

  def addChangeListener(listener: () => Unit) = {
    listeners += listener
  }

}