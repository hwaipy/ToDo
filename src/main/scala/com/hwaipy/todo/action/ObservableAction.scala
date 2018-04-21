//package com.hwaipy.todo
//
//import java.io.File
//import java.time.LocalDateTime
//
//import com.hwaipy.todo.action.{Action, AtomicEvent, Event, Events}
//
//import scala.collection.mutable
//import scala.collection.mutable.ListBuffer
//
//object ObservableAction {
//  def loadFromFile(file: File) = {
//    val events = Events.loadFromFile(file)
//  }
//
//
//}
//
//
//
////class ActionSet {
////  val actionMap = mutable.HashMap[Int, Action]()
////  var events = new ListBuffer[Event]()
////  val ultimateAction = doCreateAction(0)
////
////  def doPerformEvent(event: Event) = {
////    events += event
////    event.perform(this)
////  }
////
////  def doCreateAction(id: Int, timeStamp: LocalDateTime = LocalDateTime.now) = {
////    actionMap.contains(id) match {
////      case true => throw new IllegalArgumentException(s"Action id ${id} exists.")
////      case false => {
////        val action = id match {
////          case 0 => new Action(this, id, timeStamp, true)
////          case _ => new Action(this, id, timeStamp)
////        }
////        actionMap.put(id, action)
////        action
////      }
////    }
////  }
////
////  def eventCreateAction(title: String = "", superAction: Option[Int] = None) = {
////    val id = actionMap.keys match {
////      case keys if keys.isEmpty => 0
////      case keys => keys.max + 1
////    }
////    val events = new ListBuffer[Event]
////    events += Action.createEvent(id, LocalDateTime.now)
////    events += Action.modifyEvent(id, "title", title, LocalDateTime.now)
////    if (superAction != None) events += Action.modifyEvent(id, "superAction", superAction.get.toString, LocalDateTime.now)
////    val event = new AtomicEvent(events)
////    doPerformEvent(event)
////    actionMap(id)
////  }
////
////  def doRemoveAction(id: Int) = {
////    actionMap.contains(id) match {
////      case true => actionMap.remove(id)
////      case false => throw new IllegalArgumentException(s"Action id ${id} does not exist.")
////    }
////  }
////
////  def getAction(id: Int) = actionMap.get(id) match {
////    case Some(x) => x
////    case None => throw new IllegalArgumentException(s"Action is ${id} does not exist.")
////  }
////
////  def actions = actionMap.values.filter(a => a.id != 0).toList
////
////  def rootActions = actionMap.values.filter(a => a.id != 0).filter(a => a.superAction() == a.actionSet.ultimateAction).toList
////}