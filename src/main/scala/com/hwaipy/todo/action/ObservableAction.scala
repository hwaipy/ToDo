package com.hwaipy.todo.action

import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import java.time.{LocalDateTime, LocalDate}
import scala.collection.mutable
import scalafx.beans.property.{BooleanProperty, IntegerProperty, ObjectProperty, StringProperty}
import scalafx.scene.control.TreeItem

class ObservableAction(val action: Action) {
  private val listener = new PropertyChangeListener {
    override def propertyChange(evt: PropertyChangeEvent) {
      evt.getPropertyName match {
        case "title" => title.value = action.getTitle
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

  val title = StringProperty(action.getTitle)
  val lastModified = ObjectProperty(action.getLastModified)
  val begin = ObjectProperty(action.getBegin)
  val due = ObjectProperty(action.getDue)
  val context = StringProperty(action.getContext)
  val priority = StringProperty(action.getPriority)
  val isProject = ObjectProperty(action.getIsProject)
  val isDone = BooleanProperty(action.getIsDone)
  val dueCount = IntegerProperty(action.getDueCount)
  val almostDueCount = IntegerProperty(action.getAlmostDueCount)

  override def toString: String = s"ObservableAction[${action.getTitle}]"
}

class ActionView(actionSet: ActionSet, expandAll: Boolean = true) {
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

  if (expandAll) itemMap.values.foreach(item => item.expanded = true)
}

class NextsActionView(actionSet: ActionSet) {
  val root = new TreeItem[ObservableAction](new ObservableAction(actionSet.rootAction))
  refresh

  def refresh = {
    root.children.clear
    val availableActions = actionSet.actions.filter(action => (!action.getIsProject) && (!action.getIsDone) && (action.getDue != Events.INVALID_TIME_STAMP))
    val now = LocalDateTime.now
    val dueEmergencyActions = availableActions.filter(action => action.getPriority == "Emergency" && action.getDue.isBefore(now)).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val todayEmergencyActions = availableActions.filter(action => action.getPriority == "Emergency" && action.getDue.isAfter(now) && action.getDue.isBefore(LocalDate.from(now.plusDays(1)).atStartOfDay())).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val dueImmediateActions = availableActions.filter(action => action.getPriority == "Immediate" && action.getDue.isBefore(now)).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val dueNormalActions = availableActions.filter(action => action.getPriority == "Normal" && action.getDue.isBefore(now)).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val dueOpportunityActions = availableActions.filter(action => action.getPriority == "Opportunity" && action.getDue.isBefore(now)).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val otherEmergencyActions = availableActions.filter(action => action.getPriority == "Emergency" && action.getDue.isAfter(LocalDate.from(now.plusDays(1)).atStartOfDay())).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val almostDueImmediateActions = availableActions.filter(action => action.getPriority == "Immediate" && action.getDue.isAfter(now) && action.getDue.isBefore(now.plusHours(24))).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val almostDueNormalActions = availableActions.filter(action => action.getPriority == "Normal" && action.getDue.isAfter(now) && action.getDue.isBefore(now.plusHours(24))).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val almostDueOpportunityActions = availableActions.filter(action => action.getPriority == "Opportunity" && action.getDue.isAfter(now) && action.getDue.isBefore(now.plusHours(24))).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val futureImmediateActions = availableActions.filter(action => action.getPriority == "Immediate" && action.getDue.isAfter(now.plusHours(24))).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val futureNormalActions = availableActions.filter(action => action.getPriority == "Normal" && action.getDue.isAfter(now.plusHours(24))).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val futureOpportunityActions = availableActions.filter(action => action.getPriority == "Opportunity" && action.getDue.isAfter(now.plusHours(24))).sortWith((a1, a2) => a1.getDue.isBefore(a2.getDue))
    val list = dueEmergencyActions ::: todayEmergencyActions ::: dueImmediateActions ::: dueNormalActions ::: almostDueImmediateActions ::: otherEmergencyActions ::: almostDueNormalActions ::: dueOpportunityActions ::: almostDueOpportunityActions ::: futureImmediateActions ::: futureNormalActions ::: futureOpportunityActions
    list.foreach(action => root.children += new TreeItem[ObservableAction](new ObservableAction(action)))
  }
}