package com.hwaipy.todo.action

import java.io.File
import java.time.{LocalDateTime, ZoneOffset}
import java.util.TimeZone

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.xml.{Node, XML}
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}

class Action(val actionSet: ActionSet, val id: Int, creationTime: LocalDateTime, ultimate: Boolean = false) {
  val title = StringProperty("")
  val superAction = ultimate match {
    case true => ObjectProperty[Action](null, "Init")
    case false => ObjectProperty[Action](actionSet.ultimateAction)
  }
  var lastModified = ObjectProperty(creationTime)

  def doModify(key: String, value: String, timeStamp: LocalDateTime = LocalDateTime.now) = {
    val lastLastModified = lastModified()
    lastModified() = timeStamp
    val oldValue = key match {
      case "title" => {
        val oldTitle = title()
        title() = value
        oldTitle
      }
      case "superAction" => {
        val oldSuperAction = superAction() match {
          case x if x == actionSet.ultimateAction => "None"
          case x if x == null => "None"
          case x => x.id.toString
        }
        superAction() = value match {
          case "None" => actionSet.ultimateAction
          case s => actionSet.getAction(s.toInt)
        }
        oldSuperAction
      }
      case _ => throw new IllegalArgumentException(s"Key of Action ${key} not recgonized.")
    }
    (oldValue, lastLastModified)
  }

  def doReverse(key: String, oldValue: String, oldTimeStamp: LocalDateTime = LocalDateTime.now) = {
    lastModified() = oldTimeStamp
  }

  override def toString: String = s"Action[$title]"
}

object Action {
  def createEvent(eventTargetID: Int, timeStamp: LocalDateTime = LocalDateTime.now) = new Event {
    def perform(actionSet: ActionSet): Unit = {
      actionSet.doCreateAction(eventTargetID, timeStamp)
    }

    def reverse(actionSet: ActionSet): Unit = actionSet.doRemoveAction(eventTargetID)
  }

  def modifyEvent(eventTargetID: Int, key: String, value: String, timeStamp: LocalDateTime = LocalDateTime.now) = new Event {
    var oldValue: Option[Tuple2[String, LocalDateTime]] = None

    def perform(actionSet: ActionSet): Unit = {
      val action = actionSet.getAction(eventTargetID)
      oldValue = Some(action.doModify(key, value, timeStamp))
    }

    def reverse(actionSet: ActionSet): Unit = {
      oldValue match {
        case Some(t) => {
          val action = actionSet.getAction(eventTargetID)
          action.doReverse(key, t._1, t._2)
        }
        case None => throw new IllegalStateException(s"The Event has not been performed, hence can not be reversed.")
      }
    }
  }
}

class ActionSet {
  val actionMap = mutable.HashMap[Int, Action]()
  var events = new ListBuffer[Event]()
  val ultimateAction = doCreateAction(0)

  def doPerformEvent(event: Event) = {
    events += event
    event.perform(this)
  }

  def doCreateAction(id: Int, timeStamp: LocalDateTime = LocalDateTime.now) = {
    actionMap.contains(id) match {
      case true => throw new IllegalArgumentException(s"Action id ${id} exists.")
      case false => {
        val action = id match {
          case 0 => new Action(this, id, timeStamp, true)
          case _ => new Action(this, id, timeStamp)
        }
        actionMap.put(id, action)
        action
      }
    }
  }

  def eventCreateAction(title: String = "", superAction: Option[Int] = None) = {
    val id = actionMap.keys match {
      case keys if keys.isEmpty => 0
      case keys => keys.max + 1
    }
    val events = new ListBuffer[Event]
    events += Action.createEvent(id, LocalDateTime.now)
    events += Action.modifyEvent(id, "title", title, LocalDateTime.now)
    if (superAction != None) events += Action.modifyEvent(id, "superAction", superAction.get.toString, LocalDateTime.now)
    val event = new AtomicEvent(events)
    doPerformEvent(event)
    actionMap(id)
  }

  def doRemoveAction(id: Int) = {
    actionMap.contains(id) match {
      case true => actionMap.remove(id)
      case false => throw new IllegalArgumentException(s"Action id ${id} does not exist.")
    }
  }

  def getAction(id: Int) = actionMap.get(id) match {
    case Some(x) => x
    case None => throw new IllegalArgumentException(s"Action is ${id} does not exist.")
  }

  def actions = actionMap.values.filter(a => a.id != 0).toList

  def rootActions = actionMap.values.filter(a => a.id != 0).filter(a => a.superAction() == a.actionSet.ultimateAction).toList
}

object ActionSet {
  def loadFromFile(file: File) = Events.loadFromFile(file)
}

object Events {
  def loadFromFile(file: File) = {
    val root = XML.loadFile(file)
    val eventsSeq = root.child.filter(e => e.label == "events")
    val events = eventsSeq.map(loadAtomicEvents)
    val actionSet = new ActionSet
    events.foreach(actionSet.doPerformEvent)
    actionSet
  }

  private def loadAtomicEvents(eventsNode: Node) = {
    val eventSeq = eventsNode.child.filter(e => e.label == "event")
    val timeStamp = (eventsNode.attribute("timeStamp")) match {
      case Some(x) => {
        val t = x.head.text.toLong
        LocalDateTime.ofEpochSecond(t / 1000, (t % 1000).toInt * 1000000, ZoneOffset.ofTotalSeconds(TimeZone.getDefault.getRawOffset / 1000))
      }
      case _ => throw new RuntimeException("attribute \"timeStamp\" is required for <event>.")
    }
    val events = eventSeq.map(loadEvent(_, timeStamp))
    new AtomicEvent(events)
  }

  private def loadEvent(eventNode: Node, timeStamp: LocalDateTime) = {
    val eventTargetID = (eventNode.attribute("id")) match {
      case Some(x) => x.head.text.toInt
      case _ => throw new RuntimeException("attribute \"id\" is required for <event>.")
    }
    val eventType = (eventNode.attribute("type")) match {
      case Some(x) => x.head.text
      case _ => throw new RuntimeException("attribute \"type\" is required for <event>.")
    }
    eventType match {
      case "create" => Action.createEvent(eventTargetID, timeStamp)
      case "modify" => {
        val key = (eventNode.attribute("key")) match {
          case Some(x) => x.head.text
          case _ => throw new RuntimeException("attribute \"key\" is required for modify event.")
        }
        val value = (eventNode.attribute("value")) match {
          case Some(x) => x.head.text
          case _ => throw new RuntimeException("attribute \"value\" is required for modify event.")
        }
        Action.modifyEvent(eventTargetID, key, value, timeStamp)
      }
      case _ => throw new IllegalArgumentException(s"Event type '${eventType}' not recgonized.")
    }
  }
}

class AtomicEvent(events: TraversableOnce[Event]) extends Event {
  private val eventList = events.toList

  override def perform(actionSet: ActionSet) = {
    eventList.foreach(_.perform(actionSet))
  }

  override def reverse(actionSet: ActionSet) = eventList.reverse.foreach(_.reverse(actionSet))
}

trait Event {
  def perform(actionSet: ActionSet): Unit

  def reverse(actionSet: ActionSet): Unit
}