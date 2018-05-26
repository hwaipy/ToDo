package com.hwaipy.todo

import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import com.hwaipy.todo.action.{Action, ActionSet}
import scala.collection.mutable
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.scene.control.TreeItem

class ObservableAction(val action: Action) {
  private val listener = new PropertyChangeListener {
    override def propertyChange(evt: PropertyChangeEvent) {
      evt.getPropertyName match {
        case "title" => title.value = getTitleMethodTempWithEmergency
        case "begin" => begin.value = action.getBegin
        case "due" => due.value = action.getDue
        case "context" => context.value = action.getContext
        case "priority" => priority.value = action.getPriority
        case "isDone" => isDone.value = action.getIsDone
        case "dueCount" => dueCount.value = action.getDueCount
        case "almostDueCount" => almostDueCount.value = action.getAlmostDueCount
      }
      lastModified.value = action.getLastModified
    }
  }
  action.addPropertyChangeListener(listener)

  val title = StringProperty(getTitleMethodTempWithEmergency)
  val lastModified = ObjectProperty(action.getLastModified)
  val begin = ObjectProperty(action.getBegin)
  val due = ObjectProperty(action.getDue)
  val context = StringProperty(action.getContext)
  val priority = StringProperty(action.getPriority)
  val isProject = ObjectProperty(action.getIsProject)
  val isDone = ObjectProperty(action.getIsDone)
  val dueCount = ObjectProperty(action.getDueCount)
  val almostDueCount = ObjectProperty(action.getAlmostDueCount)

  def getTitleMethodTempWithEmergency = {
    val title = action.getTitle
    action.getPriority match {
      case "Emergency" => "***" + title + "***"
      case _ => title
    }
  }
}

class ActionView(actionSet: ActionSet) {
  val itemMap = new mutable.HashMap[Int, TreeItem[ObservableAction]]()
  private var filter = (action: Action) => true
  refresh
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
    if (filter(item.value().action)) newSuper.getChildren.addAll(item)
  })

  def getTreeItem(id: Int) = itemMap(id)

  def applyFilter(filter: (Action) => Boolean) {
    this.filter = filter
    refresh
  }

  def refresh = {
    itemMap.values.foreach(_.children.clear)
    if (!itemMap.contains(0)) itemMap.put(0, new TreeItem[ObservableAction](new ObservableAction(actionSet.rootAction)))
    if (!itemMap.contains(-1)) itemMap.put(-1, new TreeItem[ObservableAction](new ObservableAction(actionSet.deletedRootAction)))
    actionSet.actions.filter(_.id > 0).filter(action => !itemMap.contains(action.id)).foreach(action => itemMap.put(action.id, new TreeItem[ObservableAction](new ObservableAction(action))))
    itemMap.values.toList.filter(_.value().action.id > 0).filter(item => filter(item.value().action)).sortWith((ti1, ti2) => ti1.value().action.id < ti2.value().action.id).foreach(item => {
      val superID = item.value().action.getSuperActionId
      val superItem = itemMap(superID)
      superItem.getChildren.addAll(item)
    })
  }
}
