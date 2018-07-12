package com.hwaipy.todo.gui

import java.awt.Image

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scalafx.scene.layout.{AnchorPane, Region}
import scalafx.scene.control.ToggleButton

class HSideBar(nameAndIcons: List[Tuple2[String, Image]], onSelectionChange: (String) => Unit) extends AnchorPane {
  private val unitSize = 64
  prefWidth = unitSize
  prefHeight = 400

  private val buttonMap = new mutable.LinkedHashMap[String, HSideBarButtonPane]
  private val sideBorderPaneMap = new mutable.HashMap[String, Region]
  private val bottomBorderPaneList = new ListBuffer[Region]

  nameAndIcons.zipWithIndex.foreach(z => {
    val index = z._2
    val buttonPane = new HSideBarButtonPane(z._1._1, z._1._2)
    buttonMap(z._1._1) = buttonPane
    val sideBorderPane = new HSideBarSemiBorderPane(1, unitSize)
    sideBorderPaneMap(z._1._1) = sideBorderPane
    val bottomBorderPane = new HSideBarSemiBorderPane(unitSize, 1)
    bottomBorderPaneList += bottomBorderPane
    AnchorPane.setLeftAnchor(buttonPane, 0)
    AnchorPane.setRightAnchor(buttonPane, 0)
    AnchorPane.setTopAnchor(buttonPane, index * (unitSize + 1))
    AnchorPane.setLeftAnchor(bottomBorderPane, 0)
    AnchorPane.setRightAnchor(bottomBorderPane, 0)
    AnchorPane.setTopAnchor(bottomBorderPane, index * (unitSize + 1) + unitSize)
    AnchorPane.setTopAnchor(sideBorderPane, index * (unitSize + 1))
    AnchorPane.setRightAnchor(sideBorderPane, 0)

    buttonPane.onAction = (a) => {
      onAction(z._1._1)
    }
  })

  private val restSideBorderPane = new HSideBarSemiBorderPane(1, unitSize)
  sideBorderPaneMap("") = restSideBorderPane
  AnchorPane.setTopAnchor(restSideBorderPane, buttonMap.size * (unitSize + 1))
  AnchorPane.setRightAnchor(restSideBorderPane, 0)
  AnchorPane.setBottomAnchor(restSideBorderPane, 0)

  children = buttonMap.values.toList ::: sideBorderPaneMap.values.toList ::: bottomBorderPaneList.toList

  this.getStyleClass.add("hsidebar")

  private def onAction(actionName: String) = {
    val button = buttonMap(actionName)
    val isChanged = button.selected.value == true
    button.selected = true
    if (isChanged) {
      buttonMap.keys.foreach(key => {
        val actionOne = key == actionName
        buttonMap(key).selected = actionOne
        sideBorderPaneMap(key).visible = !actionOne
      })
      onSelectionChange(actionName)
    }
  }

  private class HSideBarButtonPane(name: String, icon: Image) extends ToggleButton(name) {
    minWidth = unitSize
    prefWidth = unitSize
    maxWidth = unitSize
    minHeight = unitSize
    prefHeight = unitSize
    maxHeight = unitSize
    focusTraversable = false

    this.getStyleClass.addAll("hsidebar-togglebutton")
    this.id = s"hsidebar-togglebutton-$name"
  }

  private class HSideBarSemiBorderPane(width: Double, height: Double) extends AnchorPane {
    this.getStyleClass.add("hsidebar-semiborder")
    minWidth = width
    prefWidth = width
    maxWidth = width
    minHeight = height
    prefHeight = height
    maxHeight = height
  }

  def select(name: String) = buttonMap(name).fire
}