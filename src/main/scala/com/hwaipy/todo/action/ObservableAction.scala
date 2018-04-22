package com.hwaipy.todo

import java.beans.{PropertyChangeEvent, PropertyChangeListener}

import com.hwaipy.todo.action.{Action, ActionSet}

import scala.collection.mutable
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.scene.control.TreeItem


class ObservableAction(val action: Action) {
  private val listener = new PropertyChangeListener {
    override def propertyChange(evt: PropertyChangeEvent) {

    }
  }
  action.addPropertyChangeListener(listener)

  val title = StringProperty(action.getTitle)
  val lastModified = ObjectProperty(action.getLastModified)
  val begin = ObjectProperty(action.getBegin)
  val due = ObjectProperty(action.getDue)
  val context = ObjectProperty(action.getContext)
  val priority = ObjectProperty(action.getPriority)
  val isProject = BooleanProperty(action.getIsProject)
}

object ObservableAction {
  def projectItems(actionSet: ActionSet) = {
    val itemMap = new mutable.HashMap[Int, TreeItem[ObservableAction]]()
    itemMap.put(0, new TreeItem[ObservableAction](new ObservableAction(actionSet.ultimateAction)))
    actionSet.actions.filter(_.id > 0).filter(_.getIsProject).foreach(action => itemMap.put(action.id, new TreeItem[ObservableAction](new ObservableAction(action))))
    itemMap.values.filter(_.value().action.id > 0).foreach(item => {
      val superID = item.value().action.getSuperActionId
      val superItem = itemMap(superID)
      superItem.getChildren.addAll(item)
    })
    actionSet.addHierarchyChangeListener((id: Int, oldSuperAction: Int, newSuperAction: Int) => {
      val item = itemMap.get(id) match {
        case Some(i) => i
        case None => {
          val i = new TreeItem[ObservableAction](new ObservableAction(actionSet.getAction(id)))
          itemMap.put(id, i)
          i
        }
      }
      val oldSuper = itemMap(oldSuperAction)
      val newSuper = itemMap(newSuperAction)
      oldSuper.getChildren.removeAll(item)
      newSuper.getChildren.addAll(item)
    })
    itemMap(0)
  }
}