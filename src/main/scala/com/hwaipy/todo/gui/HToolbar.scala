package com.hwaipy.todo.gui

import java.awt.Image

import scala.collection.mutable.ListBuffer
import scalafx.geometry.{Orientation, Pos}
import scalafx.scene.layout.{AnchorPane, Region}
import scalafx.scene.control.{Button, Label, ToggleButton}

class HToolBar(orientation: Orientation = Orientation.Horizontal) extends AnchorPane {
  orientation match {
    case Orientation.Horizontal => {
      prefWidth = 400
      prefHeight = 56
    }
    case Orientation.Vertical => {
      prefWidth = 56
      prefHeight = 400
    }
    case _ => throw new IllegalArgumentException
  }

  object Alignment extends Enumeration {
    type Alignment = Value
    val Leading, Tailing = Value
  }

  import Alignment._

  private val childrenListLeading = new ListBuffer[Region]
  private val childrenListTailing = new ListBuffer[Region]

  def addButton(name: String, icon: Image, action: () => Unit, alignment: Alignment = Leading) = {
    val buttonPane = new HToolBarButtonPanel(name)
    (alignment match {
      case Leading => childrenListLeading
      case Tailing => childrenListTailing
    }) += buttonPane
    updateLayouts
  }

  def addRegion(region: Region, alignment: Alignment = Leading) = {
    (alignment match {
      case Leading => childrenListLeading
      case Tailing => childrenListTailing
    }) += region
    updateLayouts
  }

  private def updateLayouts = {
    children = childrenListLeading.toList ::: childrenListTailing.toList
    orientation match {
      case Orientation.Horizontal => {
        childrenListLeading.map(region => (region.prefWidth.value, region)).foldLeft(0.0) { (a, b) => {
          val region = b._2
          val width = b._1
          AnchorPane.clearConstraints(region)
          AnchorPane.setTopAnchor(region, 0)
          AnchorPane.setBottomAnchor(region, 0)
          AnchorPane.setLeftAnchor(region, a)
          a + width
        }
        }
        childrenListTailing.map(region => (region.prefWidth.value, region)).foldLeft(0.0) { (a, b) => {
          val region = b._2
          val width = b._1
          AnchorPane.clearConstraints(region)
          AnchorPane.setTopAnchor(region, 0)
          AnchorPane.setBottomAnchor(region, 0)
          AnchorPane.setRightAnchor(region, a)
          a + width
        }
        }
      }
      case Orientation.Vertical => {
        childrenListLeading.map(region => (region.prefHeight.value, region)).foldLeft(0.0) { (a, b) => {
          println(s"ori ${b._2}")
          val region = b._2
          val height = b._1
          AnchorPane.clearConstraints(region)
          AnchorPane.setLeftAnchor(region, 0)
          AnchorPane.setRightAnchor(region, 0)
          AnchorPane.setTopAnchor(region, a)
          a + height
        }
        }
        childrenListTailing.map(region => (region.prefHeight.value, region)).foldLeft(0.0) { (a, b) => {
          val region = b._2
          val height = b._1
          AnchorPane.clearConstraints(region)
          AnchorPane.setLeftAnchor(region, 0)
          AnchorPane.setRightAnchor(region, 0)
          AnchorPane.setBottomAnchor(region, a)
          a + height
        }
        }
      }
      case _ => throw new IllegalArgumentException
    }
  }

  this.getStyleClass.add("htoolbar")
}

class HToolBarButtonPanel(name: String) extends AnchorPane {
  val button = new Button()

  prefWidth = 60
  prefHeight = 60
  button.prefWidth = 42
  button.prefHeight = 24
  button.minHeight = 24
  AnchorPane.setLeftAnchor(button, (prefWidth.value - button.prefWidth.value) / 2)
  AnchorPane.setTopAnchor(button, 9)
  button.focusTraversable = false
  val label = new Label(name)
  label.alignment = Pos.Center
  AnchorPane.setLeftAnchor(label, 2)
  AnchorPane.setRightAnchor(label, 2)
  AnchorPane.setBottomAnchor(label, 4)
  children = Seq(button, label)

  button.getStyleClass.add("htoolbar-button")
  label.getStyleClass.add("htoolbar-label")
}