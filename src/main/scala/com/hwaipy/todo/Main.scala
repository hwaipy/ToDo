package com.hwaipy.todo

import java.io.{File, FileOutputStream, PrintStream}
import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDateTime}
import java.util.TimerTask
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.control

import scala.language.reflectiveCalls
import com.hwaipy.todo.action.{Action, ActionSet, Events}
import com.hwaipy.todo.input.Input

import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.{IntegerProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.input.{KeyEvent, MouseEvent}
import scalafx.scene.layout.{AnchorPane, GridPane}
import java.nio.file.{Files, StandardCopyOption}

object ToDoApp extends JFXApp {
  val DEBUG = true
  val storageFile = new File("ToDo.xml")

  if (!DEBUG) {
    System.setOut(new PrintStream(new FileOutputStream(s"StdOut-${LocalDateTime.now}.txt".replaceAll(":", "-")), true))
    System.setErr(new PrintStream(new FileOutputStream(s"StdErr-${LocalDateTime.now}.txt".replaceAll(":", "-")), true))
    Files.copy(storageFile.toPath, new File(storageFile.getAbsolutePath.reverse.replaceFirst("lmx.", s"lmx.${LocalDateTime.now.toString.replaceAll(":", "-").reverse}").reverse).toPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING)
  }

  val actionSet = ActionSet.loadFromFile(storageFile)
  val mainSplitPane = new SplitPane {
    items += featureView
    items += projectView
    items += actionView
    items += tempView
  }
  stage = new PrimaryStage {
    title = "ToDo"
    scene = new Scene {
      root = new AnchorPane {
        prefWidth = 1680
        prefHeight = 800
        mainSplitPane.setDividerPositions(0.1, 0.3, 0.9)
        AnchorPane.setTopAnchor(mainSplitPane, 0.0)
        AnchorPane.setLeftAnchor(mainSplitPane, 0.0)
        AnchorPane.setBottomAnchor(mainSplitPane, 0.0)
        AnchorPane.setRightAnchor(mainSplitPane, 0.0)
        children = Seq(mainSplitPane)
      }
    }
  }

  lazy val projectView = new ScrollPane {
    private val aPane = new AnchorPane {
      val projectTreeTable = new TreeTableView[ObservableAction] {
        editable = true
        prefWidth = 300
        prefHeight = 800
        val projectTitleColumn = new TreeTableColumn[ObservableAction, String] {
          text = "Project"
          cellValueFactory = _.value.getValue.title
          prefWidth = 200
        }
        val projectDueColumn = new TreeTableColumn[ObservableAction, Int] {
          text = ""
          cellValueFactory = _.value.getValue.dueCount
          cellFactory = createTreeTableDueCountCellFactory
          style = "-fx-text-fill: red"
          prefWidth = 30
        }
        val projectAlmostDueColumn = new TreeTableColumn[ObservableAction, Int] {
          text = ""
          cellValueFactory = _.value.getValue.almostDueCount
          cellFactory = createTreeTableDueCountCellFactory
          style = "-fx-text-fill: darkorange"
          prefWidth = 30
        }
        columns ++= Seq(projectTitleColumn, projectDueColumn, projectAlmostDueColumn)
        val actionView = new ActionView(actionSet)
        actionView.applyFilter((action) => action.getIsProject)
        val rootItem = actionView.getTreeItem(0)
        root() = rootItem
        showRoot = false
        expandNode(root())
      }
      AnchorPane.setTopAnchor(projectTreeTable, 0.0)
      AnchorPane.setLeftAnchor(projectTreeTable, 0.0)
      AnchorPane.setBottomAnchor(projectTreeTable, 0.0)
      AnchorPane.setRightAnchor(projectTreeTable, 0.0)

      val newProjectButton = new Button("New") {
        onAction = () => {
          projectTreeTable.getSelectionModel.getSelectedItem match {
            case null => {
              showActionInformationDialog(new ActionInfo("", "", "", "None", "Normal", true)).foreach(info => {
                actionSet.eventCreateAction(info.title, Input.stringToDateTime(info.begin), Input.stringToDateTime(info.due), info.context, info.priority, info.isProject, 0)
              })
            }
            case oa => {
              showActionInformationDialog(new ActionInfo("", "", "", "None", "Normal", true)).foreach(info => {
                actionSet.eventCreateAction(info.title, Input.stringToDateTime(info.begin), Input.stringToDateTime(info.due), info.context, info.priority, info.isProject, oa.value().action.id)
              })
            }
          }
        }
      }
      AnchorPane.setLeftAnchor(newProjectButton, 0.0)
      AnchorPane.setBottomAnchor(newProjectButton, 0.0)
      children = Seq(projectTreeTable, newProjectButton)

      projectTreeTable.onKeyTyped = (event: KeyEvent) => {
        if (event.character == "\u001B") {
          projectTreeTable.getSelectionModel.clearSelection()
        }
        if (event.character == "\r") openChangeDialog
      }
      projectTreeTable.onMouseClicked = (event: MouseEvent) => {
        if (event.clickCount == 2) openChangeDialog
      }

      def openChangeDialog = {
        projectTreeTable.getSelectionModel.getSelectedItem match {
          case null =>
          case item => {
            val action = item.getValue.action
            showActionInformationDialog(new ActionInfo(action.getTitle, dateTimeToEditableString(action.getBegin), dateTimeToEditableString(action.getDue), action.getContext, action.getPriority, true)).foreach(info => {
              actionSet.eventModifyAction(action.id, info.title, Input.stringToDateTime(info.begin), Input.stringToDateTime(info.due), info.context, info.priority, action.getIsDone, action.getSuperActionId)
            })
          }
        }
      }

      projectTreeTable.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[control.TreeItem[ObservableAction]] {
        override def changed(observable: ObservableValue[_ <: control.TreeItem[ObservableAction]], oldValue: control.TreeItem[ObservableAction], newValue: control.TreeItem[ObservableAction]) = {
          onProjectSelectionChange(newValue match {
            case null => -1
            case _ => newValue.getValue().action.id
          })
        }
      })
    }
    content = aPane
    var onProjectSelectionChange = (id: Int) => {}

    def selectProject(projectID: Int) = {
      val actionView = aPane.projectTreeTable.actionView
      val treeItem = actionView.getTreeItem(projectID)
      aPane.projectTreeTable.getSelectionModel.select(treeItem)
    }
  }

  lazy val actionView = new ScrollPane {
    val aPane = new AnchorPane {
      val actionTreeTable = new TreeTableView[ObservableAction] {
        editable = true
        prefWidth = 960
        prefHeight = 800
        val actionTitleColumn = new TreeTableColumn[ObservableAction, String] {
          text = "Action"
          cellValueFactory = _.value.getValue.title
          prefWidth = 500
        }
        val beginColumn = new TreeTableColumn[ObservableAction, LocalDateTime] {
          text = "Begin"
          cellValueFactory = _.value.getValue.begin
          cellFactory = createTreeTableLocalDateTimeCellFactory
          prefWidth = 100
        }
        val dueColumn = new TreeTableColumn[ObservableAction, LocalDateTime] {
          text = "Due"
          cellValueFactory = _.value.getValue.due
          cellFactory = createTreeTableLocalDateTimeCellFactory
          prefWidth = 100
        }
        val contextColumn = new TreeTableColumn[ObservableAction, String] {
          text = "Context"
          cellValueFactory = _.value.getValue.context
          prefWidth = 100
        }
        val priorityColumn = new TreeTableColumn[ObservableAction, String] {
          text = "Priority"
          cellValueFactory = _.value.getValue.priority
          prefWidth = 80
        }
        val isDoneColumn = new TreeTableColumn[ObservableAction, Boolean] {
          text = "done"
          cellValueFactory = _.value.getValue.isDone
          prefWidth = 60
        }
        columns ++= Seq(actionTitleColumn, beginColumn, dueColumn, contextColumn, priorityColumn, isDoneColumn)
        showRoot = false
      }
      AnchorPane.setTopAnchor(actionTreeTable, 0.0)
      AnchorPane.setLeftAnchor(actionTreeTable, 0.0)
      AnchorPane.setBottomAnchor(actionTreeTable, 0.0)
      AnchorPane.setRightAnchor(actionTreeTable, 0.0)

      val newActionButton = new Button("New") {
        onAction = () => {
          actionTreeTable.getSelectionModel.getSelectedItem match {
            case null => {
              showActionInformationDialog(new ActionInfo("", "", "", "None", "Normal", false)).foreach(info => {
                actionSet.eventCreateAction(info.title, Input.stringToDateTime(info.begin), Input.stringToDateTime(info.due), info.context, info.priority, info.isProject, selectedProjectIDProperty.value)
              })
            }
            case oa => {
              showActionInformationDialog(new ActionInfo("", "", "", "None", "Normal", false)).foreach(info => {
                actionSet.eventCreateAction(info.title, Input.stringToDateTime(info.begin), Input.stringToDateTime(info.due), info.context, info.priority, info.isProject, oa.getValue.action.id)
              })
            }
          }
        }
        disable = true
      }
      AnchorPane.setLeftAnchor(newActionButton, 0.0)
      AnchorPane.setBottomAnchor(newActionButton, 0.0)

      val viewCheckBox = new CheckBox("View All") {
        onAction = () => {
          actionView.refresh
        }
      }
      AnchorPane.setRightAnchor(viewCheckBox, 0.0)
      AnchorPane.setBottomAnchor(viewCheckBox, 0.0)

      children = Seq(actionTreeTable, newActionButton, viewCheckBox)

      actionTreeTable.onKeyTyped = (event: KeyEvent) => {
        if (event.character == "\u001B") {
          actionTreeTable.getSelectionModel.clearSelection()
        }
        if (event.character == "\r") openChangeDialog
        if (event.character == " ") setActionDone
        if (event.character == "\u007F") deleteSelectedAction
      }
      actionTreeTable.onMouseClicked = (event: MouseEvent) => {
        if (event.clickCount == 2) openChangeDialog
      }

      def openChangeDialog = {
        actionTreeTable.getSelectionModel.getSelectedItem match {
          case null =>
          case item => {
            val action = item.getValue.action
            showActionInformationDialog(new ActionInfo(action.getTitle, dateTimeToEditableString(action.getBegin), dateTimeToEditableString(action.getDue), action.getContext, action.getPriority, false)).foreach(info => {
              actionSet.eventModifyAction(action.id, info.title, Input.stringToDateTime(info.begin), Input.stringToDateTime(info.due), info.context, info.priority, action.getIsDone, action.getSuperActionId)
            })
          }
        }
      }

      def setActionDone = {
        actionTreeTable.getSelectionModel.getSelectedItem match {
          case null =>
          case item => {
            val action = item.getValue.action
            actionSet.eventModifyAction(action.id, action.getTitle, action.getBegin, action.getDue, action.getContext, action.getPriority, !action.getIsDone, action.getSuperActionId)
          }
        }
      }

      def deleteSelectedAction = {
        actionTreeTable.getSelectionModel.getSelectedItem match {
          case null =>
          case item => {
            val action = item.getValue.action
            actionSet.eventDeleteAction(action.id)
          }
        }
      }

      val actionViewFilter = (action: Action) => (viewCheckBox.isSelected || !action.getIsDone)

      val actionView = new ActionView(actionSet)
      actionView.applyFilter(actionViewFilter)
      val rootItem = actionView.getTreeItem(0)

      val selectedProjectIDProperty = IntegerProperty(-1)
      projectView.onProjectSelectionChange = (id: Int) => {
        selectedProjectIDProperty.value = id
        newActionButton.disable = id == -1
        id match {
          case -1 => actionTreeTable.root() = actionView.getTreeItem(0)
          case i => actionTreeTable.root() = actionView.getTreeItem(i)
        }
        expandNode(actionTreeTable.root())
      }

      val timer = new java.util.Timer(true)
      timer.schedule(new TimerTask {
        override def run(): Unit = {
          actionTreeTable.refresh()
        }
      }, 30000, 30000)
    }
    content = aPane

    def selectAction(actionID: Int) = {
      val view = aPane.actionView
      val treeItem = view.getTreeItem(actionID)
      aPane.actionTreeTable.getSelectionModel.select(treeItem)
    }
  }

  private def expandNode(item: TreeItem[ObservableAction]): Unit = {
    if (item != null && !item.isLeaf) {
      item.setExpanded(true)
      item.getChildren.foreach(i => expandNode(i))
    }
  }

  lazy val featureView: ScrollPane = new ScrollPane {
    content = new AnchorPane {
      prefWidth = 50
      prefHeight = 50
      val projectViewButton = new Button("Project") {
        onAction = () => {
          mainSplitPane.items.set(2, actionView)
          mainSplitPane.setDividerPositions(0.1, 0.3, 0.9)
        }
      }
      val emergencyViewButton = new Button("Nexts") {
        onAction = () => {
          nextsView.update
          mainSplitPane.items.set(2, nextsView)
          mainSplitPane.setDividerPositions(0.1, 0.3, 0.9)
        }
      }
      AnchorPane.setLeftAnchor(projectViewButton, 0.0)
      AnchorPane.setTopAnchor(projectViewButton, 0.0)
      AnchorPane.setBottomAnchor(emergencyViewButton, 0.0)
      AnchorPane.setLeftAnchor(emergencyViewButton, 0.0)
      children = Seq(projectViewButton, emergencyViewButton)
    }
  }


  lazy val nextsView = new ScrollPane {
    val rootObservableActionItem = new TreeItem[ObservableAction](new ObservableAction(actionSet.rootAction))
    val aPane = new AnchorPane {
      val actionTreeTable = new TreeTableView[ObservableAction] {
        editable = false
        prefWidth = 960
        prefHeight = 800
        val actionTitleColumn = new TreeTableColumn[ObservableAction, String] {
          text = "Action"
          cellValueFactory = _.value.getValue.title
          prefWidth = 400
        }
        val projectTitleColumn = new TreeTableColumn[ObservableAction, String] {
          text = "Project"
          cellValueFactory = (a) => new StringProperty(actionSet.getAction(a.value.getValue.action.projectID).getTitle)
          prefWidth = 100
        }
        val beginColumn = new TreeTableColumn[ObservableAction, LocalDateTime] {
          text = "Begin"
          cellValueFactory = _.value.getValue.begin
          cellFactory = createTreeTableLocalDateTimeCellFactory
          prefWidth = 100
        }
        val dueColumn = new TreeTableColumn[ObservableAction, LocalDateTime] {
          text = "Due"
          cellValueFactory = _.value.getValue.due
          cellFactory = createTreeTableLocalDateTimeCellFactory
          prefWidth = 100
        }
        val contextColumn = new TreeTableColumn[ObservableAction, String] {
          text = "Context"
          cellValueFactory = _.value.getValue.context
          prefWidth = 100
        }
        val priorityColumn = new TreeTableColumn[ObservableAction, String] {
          text = "Priority"
          cellValueFactory = _.value.getValue.priority
          prefWidth = 80
        }
        val isDoneColumn = new TreeTableColumn[ObservableAction, Boolean] {
          text = "done"
          cellValueFactory = _.value.getValue.isDone
          prefWidth = 60
        }
        columns ++= Seq(actionTitleColumn, projectTitleColumn, beginColumn, dueColumn, contextColumn, priorityColumn, isDoneColumn)
        showRoot = false
        root = rootObservableActionItem
      }
      AnchorPane.setTopAnchor(actionTreeTable, 0.0)
      AnchorPane.setLeftAnchor(actionTreeTable, 0.0)
      AnchorPane.setBottomAnchor(actionTreeTable, 0.0)
      AnchorPane.setRightAnchor(actionTreeTable, 0.0)

      children = Seq(actionTreeTable)

      actionTreeTable.onKeyTyped = (event: KeyEvent) => {
        if (event.character == "\u001B") {
          actionTreeTable.getSelectionModel.clearSelection()
        }
        if (event.character == "\r") openChangeDialog
        if (event.character == " ") setActionDone
        if (event.character == "t") targetToProjectActionView
      }
      actionTreeTable.onMouseClicked = (event: MouseEvent) => {
        if (event.clickCount == 2) openChangeDialog
      }

      def openChangeDialog = {
        actionTreeTable.getSelectionModel.getSelectedItem match {
          case null =>
          case item => {
            val action = item.getValue.action
            showActionInformationDialog(new ActionInfo(action.getTitle, dateTimeToEditableString(action.getBegin), dateTimeToEditableString(action.getDue), action.getContext, action.getPriority, false)).foreach(info => {
              actionSet.eventModifyAction(action.id, info.title, Input.stringToDateTime(info.begin), Input.stringToDateTime(info.due), info.context, info.priority, action.getIsDone, action.getSuperActionId)
            })
          }
        }
      }

      def setActionDone = {
        actionTreeTable.getSelectionModel.getSelectedItem match {
          case null =>
          case item => {
            val action = item.getValue.action
            actionSet.eventModifyAction(action.id, action.getTitle, action.getBegin, action.getDue, action.getContext, action.getPriority, !action.getIsDone, action.getSuperActionId)
          }
        }
      }

      def targetToProjectActionView = {
        actionTreeTable.getSelectionModel.getSelectedItem match {
          case null =>
          case item => {
            val action = item.getValue.action
            mainSplitPane.items.set(2, actionView)
            mainSplitPane.setDividerPositions(0.1, 0.3, 0.9)
            val projectID = action.projectID
            projectView.selectProject(projectID)
            actionView.selectAction(action.id)
          }
        }
      }
    }
    content = aPane

    def update = {
      val nexts = actionSet.getNexts
      rootObservableActionItem.getChildren.clear
      nexts.foreach(n => rootObservableActionItem.getChildren.addAll(new TreeItem[ObservableAction](new ObservableAction(n))))
    }
  }

  lazy val tempView: ScrollPane = new ScrollPane {
    content = new AnchorPane {
      val nextButton = new Button("Next") {
        onAction = () => actionSet.getNexts.headOption foreach (action => {
          mainSplitPane.items.set(2, actionView)
          mainSplitPane.setDividerPositions(0.1, 0.3, 0.9)
          val projectID = action.projectID
          projectView.selectProject(projectID)
          actionView.selectAction(action.id)
        })
      }
      AnchorPane.setBottomAnchor(nextButton, 0.0)
      AnchorPane.setRightAnchor(nextButton, 0.0)
      children = Seq(nextButton)
    }
  }


  case class ActionInfo(title: String, begin: String, due: String, context: String, priority: String, isProject: Boolean)

  def showActionInformationDialog(info: ActionInfo) = {
    val dialog = new Dialog[ActionInfo]() {
      initOwner(stage)
      title = "Edit Action Information"
    }
    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    val title = new TextField() {
      promptText = "Username"
    }
    val begin = new TextField() {
      promptText = "Begin"
    }
    val due = new TextField() {
      promptText = "Due"
    }
    val context = new ComboBox[String] {
      items = ObservableBuffer("Lab", "Office", "Waiting", "People", "None")
    }
    val priority = new ComboBox[String] {
      items = ObservableBuffer("Immediate", "Normal", "Opportunity")
    }
    title.text = info.title
    begin.text = info.begin
    due.text = info.due
    context.getSelectionModel.select(info.context)
    priority.getSelectionModel.select(info.priority)
    val grid = new GridPane() {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)
      add(new Label("Title:"), 0, 0)
      add(title, 1, 0)
      add(new Label("Begin:"), 0, 1)
      add(begin, 1, 1)
      add(new Label("Due:"), 0, 2)
      add(due, 1, 2)
      add(new Label("Context:"), 0, 3)
      add(context, 1, 3)
      add(new Label("Proirity:"), 0, 4)
      add(priority, 1, 4)
    }

    val okButton = dialog.dialogPane().lookupButton(ButtonType.OK)
    dialog.dialogPane().content = grid
    Platform.runLater(title.requestFocus())
    dialog.showAndWait() match {
      case Some(ButtonType.OK) => Some(new ActionInfo(title.text(), begin.text(), due.text(), context.getSelectionModel.getSelectedItem, priority.getSelectionModel.getSelectedItem, info.isProject))
      case _ => None
    }
  }

  private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  private def dateTimeToEditableString(localDateTime: LocalDateTime) =
    if (localDateTime == Events.INVALID_TIME_STAMP) ""
    else DATETIME_FORMATTER.format(localDateTime)

  private def createTreeTableLocalDateTimeCellFactory: TreeTableColumn[ObservableAction, LocalDateTime] => TreeTableCell[ObservableAction, LocalDateTime] = { _: TreeTableColumn[ObservableAction, LocalDateTime] =>
    new TreeTableCell[ObservableAction, LocalDateTime] {
      item.onChange { (_, _, time) => {
        time match {
          case null => {
            style = "-fx-text-fill: black"
            text = ""
          }
          case time if time == Events.INVALID_TIME_STAMP => {
            style = "-fx-text-fill: black"
            text = ""
          }
          case time => {
            val now = LocalDateTime.now
            val delta = Duration.between(now, time).getSeconds
            delta match {
              case d if d >= 3600 * 24 => {
                style = "-fx-text-fill: black"
                text = DATE_FORMATTER.format(time)
              }
              case d if d >= 3600 * 3 => {
                style = "-fx-text-fill: darkorange"
                text = s"${delta / 3600}h"
              }
              case d if d >= 3600 => {
                style = "-fx-text-fill: darkorange"
                text = s"${delta / 3600}h ${(delta % 3600) / 60}min"
              }
              case d if d >= 0 => {
                style = "-fx-text-fill: darkorange"
                text = s"${delta / 60}min"
              }
              case d if d >= -3600 => {
                style = "-fx-text-fill: red"
                text = s"${-delta / 60}min ago"
              }
              case d if d >= -3600 * 24 => {
                style = "-fx-text-fill: red"
                text = s"${-delta / 3600}h ago"
              }
              case _ => {
                style = "-fx-text-fill: red"
                text = DATE_FORMATTER.format(time)
              }
            }
          }
        }
      }
      }
    }
  }

  private def createTreeTableDueCountCellFactory: TreeTableColumn[ObservableAction, Int] => TreeTableCell[ObservableAction, Int] = { _: TreeTableColumn[ObservableAction, Int] =>
    new TreeTableCell[ObservableAction, Int] {
      item.onChange { (_, _, count) =>
        count match {
          case 0 => text = ""
          case c => text = c.toString
        }
      }
    }
  }
}