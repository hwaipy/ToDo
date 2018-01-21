package com.hwaipy.todo

import java.io.File
import com.hwaipy.todo.action.{Action, ActionSet}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.AnchorPane

object ToDoApp extends JFXApp {
  val actionSet = ActionSet.loadFromFile(new File("D:/Google Drive/ToDo.xml"))
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
        val projectTitleColumn = new TreeTableColumn[Action, String]("Project")
        val projectNotifyColumn = new TreeTableColumn[Action, String]("Notify")
        columns ++= Seq(projectNotifyColumn, projectNotifyColumn)

        

//        // Root Item
//        TreeItem < Employee > itemRoot = new TreeItem < Employee > (empBoss);
//        TreeItem < Employee > itemSmith = new TreeItem < Employee > (empSmith);
//        TreeItem < Employee > itemMcNeil = new TreeItem < Employee > (empMcNeil);
//
//        itemRoot.getChildren().addAll(itemSmith, itemMcNeil);
//        treeTableView.setRoot(itemRoot);

      }
    }
  }
}