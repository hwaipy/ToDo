package com.hwaipy.todo

import java.io.File
import java.time.{LocalDateTime, ZoneOffset}
import java.util.TimeZone
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.control
import scala.language.reflectiveCalls
import com.hwaipy.todo.action.{ActionSet, Events}
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.input.KeyEvent
import scalafx.scene.layout.{AnchorPane, GridPane}

object ToDoApp extends JFXApp {
  //  val actionSet = ActionSet.loadFromFile(new File("../../Google Drive/ToDo.xml"))
  val storageFile = new File("ToDo.xml")
  val actionSet = ActionSet.loadFromFile(storageFile)
  stage = new PrimaryStage {
    title = "ToDo"
    scene = new Scene {
      root = new AnchorPane {
        prefWidth = 1280
        prefHeight = 800
        val mainSplitPane = new SplitPane {
          items += new Button("123")
          items += projectView
          items += actionView
          items += new Button("000")
        }
        AnchorPane.setTopAnchor(mainSplitPane, 0.0)
        AnchorPane.setLeftAnchor(mainSplitPane, 0.0)
        AnchorPane.setBottomAnchor(mainSplitPane, 0.0)
        AnchorPane.setRightAnchor(mainSplitPane, 0.0)
        children = Seq(mainSplitPane)
      }
    }
  }

  lazy val projectView = new ScrollPane {
    content = new AnchorPane {
      val projectTreeTable = new TreeTableView[ObservableAction] {
        editable = true
        val projectTitleColumn = new TreeTableColumn[ObservableAction, String] {
          text = "Project"
          cellValueFactory = _.value.getValue.title
        }
        val projectNotifyColumn = new TreeTableColumn[ObservableAction, String] {
          text = "Notify"
          //          cellValueFactory = _.value.getValue.title
        }
        columns ++= Seq(projectTitleColumn, projectNotifyColumn)
        val rootItem = ObservableAction.projectItems(actionSet)
        root() = rootItem
        showRoot = false
      }
      AnchorPane.setTopAnchor(projectTreeTable, 0.0)
      AnchorPane.setLeftAnchor(projectTreeTable, 0.0)
      AnchorPane.setBottomAnchor(projectTreeTable, 0.0)
      AnchorPane.setRightAnchor(projectTreeTable, 0.0)

      val newProjectButton = new Button("New") {
        onAction = () => {
          projectTreeTable.getSelectionModel.getSelectedItem match {
            case null => {
              showActionInformationDialog(new ActionInfo("", "", "", "", "", true)).foreach(info => {
                actionSet.eventCreateAction(info.title, stringToDateTime(info.begin), stringToDateTime(info.due), info.context, info.priority, info.isProject, 0)
                actionSet.saveToFile(storageFile)
              })
            }
            case oa => {
              showActionInformationDialog(new ActionInfo("", "", "", "", "", true)).foreach(info => {
                actionSet.eventCreateAction(info.title, stringToDateTime(info.begin), stringToDateTime(info.due), info.context, info.priority, info.isProject, oa.value().action.id)
                actionSet.saveToFile(storageFile)
              })
            }
          }
        }
      }
      AnchorPane.setLeftAnchor(newProjectButton, 0.0)
      AnchorPane.setBottomAnchor(newProjectButton, 0.0)

      children = Seq(projectTreeTable, newProjectButton)

      //      val contextMenu = new ContextMenu() {
      //        val newProjectMenuItem = new MenuItem("New Project")
      //        items.addAll(newProjectMenuItem)
      //      }
      projectTreeTable.onKeyTyped = (event: KeyEvent) => {
        if (event.character == "\u001B") {
          projectTreeTable.getSelectionModel.clearSelection()
        }
      }
      projectTreeTable.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[control.TreeItem[ObservableAction]] {
        override def changed(observable: ObservableValue[_ <: control.TreeItem[ObservableAction]], oldValue: control.TreeItem[ObservableAction], newValue: control.TreeItem[ObservableAction]) = onProjectSelectionChange(newValue.getValue().action.id)
      })
    }
    var onProjectSelectionChange = (id: Int) => {}
  }

  lazy val actionView = new ScrollPane {
    content = new AnchorPane {
      val actionTreeTable = new TreeTableView[ObservableAction] {
        editable = true
        val actionTitleColumn = new TreeTableColumn[ObservableAction, String] {
          text = "Action"
          cellValueFactory = _.value.getValue.title
        }
        //        val projectNotifyColumn = new TreeTableColumn[ObservableAction, String] {
        //          text = "Notify"
        //        }
        columns ++= Seq(actionTitleColumn)
        //        val rootItem = ObservableAction.projectItems(actionSet)
        //        root() = rootItem
        showRoot = false
      }
      AnchorPane.setTopAnchor(actionTreeTable, 0.0)
      AnchorPane.setLeftAnchor(actionTreeTable, 0.0)
      AnchorPane.setBottomAnchor(actionTreeTable, 0.0)
      AnchorPane.setRightAnchor(actionTreeTable, 0.0)

      val newProjectButton = new Button("New") {
        //        onAction = () => {
        //          projectTreeTable.getSelectionModel.getSelectedItem match {
        //            case null => {
        //              showActionInformationDialog(new ActionInfo("", "", "", "", "", true)).foreach(info => {
        //                actionSet.eventCreateAction(info.title, stringToDateTime(info.begin), stringToDateTime(info.due), info.context, info.priority, info.isProject, 0)
        //                actionSet.saveToFile(storageFile)
        //              })
        //            }
        //            case oa => {
        //              showActionInformationDialog(new ActionInfo("", "", "", "", "", true)).foreach(info => {
        //                actionSet.eventCreateAction(info.title, stringToDateTime(info.begin), stringToDateTime(info.due), info.context, info.priority, info.isProject, oa.value().action.id)
        //                actionSet.saveToFile(storageFile)
        //              })
        //            }
        //          }
        //        }
      }
      AnchorPane.setLeftAnchor(newProjectButton, 0.0)
      AnchorPane.setBottomAnchor(newProjectButton, 0.0)

      children = Seq(actionTreeTable)
      //
      //      projectTreeTable.onKeyTyped = (event: KeyEvent) => {
      //        if (event.character == "\u001B") {
      //          projectTreeTable.getSelectionModel.clearSelection()
      //        }
      //      }
      projectView.onProjectSelectionChange = (id: Int) => {
        println("View:" + id)
      }
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
      items = ObservableBuffer("Lab", "Office", "None")
    }
    context.getSelectionModel.select(2)
    val priority = new ComboBox[String] {
      items = ObservableBuffer("High", "Mid", "Low")
    }
    priority.getSelectionModel.select(2)
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
    okButton.disable = true
    title.text.onChange { (_, _, newValue) =>
      okButton.disable = newValue.trim().isEmpty
    }
    dialog.dialogPane().content = grid
    Platform.runLater(title.requestFocus())
    dialog.showAndWait() match {
      case Some(ButtonType.OK) => Some(new ActionInfo(title.text(), begin.text(), due.text(), context.getSelectionModel.getSelectedItem, priority.getSelectionModel.getSelectedItem, info.isProject))
      case _ => None
    }
  }

  def stringToDateTime(str: String) = {
    str match {
      case "" => Events.INVALID_TIME_STAMP
      case _ => Events.INVALID_TIME_STAMP
    }
  }
}