package com.hwaipy.todo

import java.io.File

import com.hwaipy.todo.action.{Action, ActionSet}

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.value.ObservableValue
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.AnchorPane

object ToDoApp extends JFXApp {
  val actionSet = ActionSet.loadFromFile(new File("../../Google Drive/ToDo.xml"))
  stage = new PrimaryStage {
    title = "ToDo"
    scene = new Scene {
      root = new AnchorPane {
        prefWidth = 1280
        prefHeight = 800
        val mainSplitPane = new SplitPane {
          items += new Button("123")
          items += createProjectView
          items += new Button("789")
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

  private def createProjectView = new ScrollPane {
    content = new AnchorPane {
      val projectTreeTable = new TreeTableView[Action] {
        editable = true
        val projectTitleColumn = new TreeTableColumn[Action, String] {
          text = "Project"
          cellValueFactory = _.value.getValue.title
        }
        val projectNotifyColumn = new TreeTableColumn[Action, String] {
          text = "Notify"
          cellValueFactory = _.value.getValue.title
        }
        columns ++= Seq(projectTitleColumn, projectNotifyColumn)
        val rootItem = new TreeItem(actionSet.getAction(1))
        rootItem.getChildren.addAll(new TreeItem(actionSet.getAction(2)), new TreeItem(actionSet.getAction(3)))
        root() = rootItem
        showRoot = false
      }
      AnchorPane.setTopAnchor(projectTreeTable, 0.0)
      AnchorPane.setLeftAnchor(projectTreeTable, 0.0)
      AnchorPane.setBottomAnchor(projectTreeTable, 0.0)
      AnchorPane.setRightAnchor(projectTreeTable, 0.0)
      children = Seq(projectTreeTable)
    }
  }
}