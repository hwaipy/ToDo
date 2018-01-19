package com.hwaipy.todo

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Tab, TabPane}
import scalafx.scene.layout.AnchorPane
import scalafx.scene.paint.Color._

object ToDoApp extends JFXApp {
  private val borderStyle = "" +
    "-fx-background-color: white;" +
    "-fx-border-color: black;" +
    "-fx-border-width: 1;" +
    "-fx-border-radius: 6;" +
    "-fx-padding: 6;"

  stage = new PrimaryStage {
    title = "ToDo"
    width = 1280
    height = 800
    scene = new Scene {
      fill = LightGreen
      content = new AnchorPane {
        style = borderStyle
        val mainTabPane = new TabPane {
          id = "source-tabs"
//          style = borderStyle
          tabs = Seq(
            new Tab() {
              text = "Demo"
              closable = false
              content = new AnchorPane {
                children = Seq(new Button("123"))
              }
            },
            new Tab() {
              text = "Source"
              closable = false
              content = new AnchorPane {
                children = Seq(new Button("456"))
              }
            }
          )
        }
        //        AnchorPane.setTopAnchor(mainTabPane, 0.0)
        //        AnchorPane.setLeftAnchor(mainTabPane, 0.0)
        //        AnchorPane.setBottomAnchor(mainTabPane, 0.0)
        //        AnchorPane.setRightAnchor(mainTabPane, 0.0)

        //        AnchorPane.setTopAnchor(mainTabPane, 0.0)
        //        AnchorPane.setLeftAnchor(mainTabPane, 0.0)
        //        AnchorPane.setBottomAnchor(mainTabPane, 0.0)
        //        AnchorPane.setRightAnchor(mainTabPane, 0.0)
        children = Seq(mainTabPane)
      }
    }
  }
}