package com.hwaipy.todo.action

import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import java.io.{File, PrintWriter}
import java.time.{LocalDateTime, ZoneOffset}
import java.util.{EventListener, TimeZone}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.xml.{Node, XML}

class Action(val actionSet: ActionSet, val id: Int, creationTime: LocalDateTime, ultimate: Boolean = false) {
  private var title = ""
  private var superAction = 0
  private var lastModified = creationTime
  private var begin = creationTime
  private var due = creationTime
  private var context = ""
  private var priority = ""
  private var isProject = false

  private val listeners = new ArrayBuffer[PropertyChangeListener]

  def addPropertyChangeListener(listener: PropertyChangeListener) = listeners += listener

  def removePropertyChangeListener(listener: PropertyChangeListener) = listeners.remove(listeners.indexOf(listener))

  private def firePropertyChangeEvent(propertyName: String, oldValue: Any, newValue: Any) {
    val event = new PropertyChangeEvent(this, propertyName, oldValue, newValue)
    listeners.foreach(_.propertyChange(event))
  }


  def modifyProperty(key: String, value: String, timeStamp: LocalDateTime = LocalDateTime.now) = {
    val lastLastModified = lastModified
    lastModified = timeStamp
    val oldValue = key match {
      case "title" => modifyTitle(value)
      case "superAction" => modifySuperAction(value)
      case "begin" => modifyBegin(value)
      case "due" => modifyDue(value)
      case "context" => modifyContext(value)
      case "priority" => modifyPriority(value)
      case "isProject" => modifiyIsProject(value)
      case _ => throw new IllegalArgumentException(s"Key of Action ${key} not recgonized.")
    }
    (oldValue, lastLastModified)
  }

  private def modifyTitle(newTitle: String) = {
    val oldTitle = title
    title = newTitle
    firePropertyChangeEvent("title", oldTitle, newTitle)
    oldTitle
  }

  private def modifySuperAction(newSuperAction: String) = {
    val oldSuperAction = superAction
    superAction = newSuperAction.toInt
    actionSet.fireHierarchyChangeEvent(id, oldSuperAction, superAction)
    oldSuperAction.toString
  }

  private def modifyBegin(newBegin: String) = {
    val oldBegin = begin
    begin = Events.stringToTime(newBegin)
    firePropertyChangeEvent("begin", oldBegin, begin)
    Events.timeToString(oldBegin)
  }

  private def modifyDue(newDue: String) = {
    val oldDue = due
    due = Events.stringToTime(newDue)
    firePropertyChangeEvent("due", oldDue, due)
    Events.timeToString(oldDue)
  }

  private def modifyContext(newContext: String) = {
    val oldContext = context
    context = newContext
    firePropertyChangeEvent("context", oldContext, context)
    oldContext
  }

  private def modifyPriority(newPriority: String) = {
    val oldPriority = priority
    priority = newPriority
    firePropertyChangeEvent("priority", oldPriority, priority)
    oldPriority
  }

  private def modifiyIsProject(newBool: String) = {
    val oldBool = isProject
    isProject = newBool.toBoolean
    firePropertyChangeEvent("isProject", oldBool, isProject)
    oldBool.toString
  }

  def getTitle = title

  def getSuperActionId = superAction

  def getLastModified = lastModified

  def getBegin = begin

  def getDue = due

  def getContext = context

  def getPriority = priority

  def getIsProject = isProject

  override def toString: String = s"Action[$title]"
}

class ActionSet {
  val actionMap = mutable.HashMap[Int, Action]()
  var events = new ListBuffer[AtomicEvent]()
  val ultimateAction = createAction(0)

  def performEvent(event: AtomicEvent) = {
    events += event
    event.perform(this)
  }

  def createAction(id: Int, timeStamp: LocalDateTime = LocalDateTime.now) = {
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

  def removeAction(id: Int) = {
    actionMap.contains(id) match {
      case true => actionMap.remove(id)
      case false => throw new IllegalArgumentException(s"Action id ${id} does not exist.")
    }
  }

  def eventCreateAction(title: String, begin: LocalDateTime, due: LocalDateTime, context: String, priority: String, isProject: Boolean, superAction: Int = 0) = {
    val id = actionMap.keys match {
      case keys if keys.isEmpty => 0
      case keys => keys.max + 1
    }
    val events = new ListBuffer[Event]
    val timeStamp = LocalDateTime.now
    events += Events.newCreateEvent(id, timeStamp)
    events += Events.newModifyEvent(id, "title", title, timeStamp)
    events += Events.newModifyEvent(id, "begin", Events.timeToString(begin), timeStamp)
    events += Events.newModifyEvent(id, "due", Events.timeToString(due), timeStamp)
    events += Events.newModifyEvent(id, "context", context, timeStamp)
    events += Events.newModifyEvent(id, "priority", priority, timeStamp)
    events += Events.newModifyEvent(id, "isProject", isProject.toString, timeStamp)
    events += Events.newModifyEvent(id, "superAction", superAction.toString, timeStamp)
    val event = new AtomicEvent(events)
    performEvent(event)
  }

  def saveToFile(file: File) = Events.saveToFile(events, file)

  def getAction(id: Int) = actionMap.get(id) match {
    case Some(x) => x
    case None => throw new IllegalArgumentException(s"Action is ${id} does not exist.")
  }

  def actions = actionMap.values.filter(a => a.id != 0).toList

  private val listeners = new ArrayBuffer[HierarchyChangeListener]

  def addHierarchyChangeListener(listener: HierarchyChangeListener) = listeners += listener

  def removeHierarchyChangeListener(listener: HierarchyChangeListener) = listeners.remove(listeners.indexOf(listener))

  def fireHierarchyChangeEvent(id: Int, oldSuperAction: Int, newSuperAction: Int) = {
    listeners.foreach(_.hierarchyChanged(id, oldSuperAction, newSuperAction))
  }

  trait HierarchyChangeListener extends EventListener {
    def hierarchyChanged(id: Int, oldSuperAction: Int, newSuperAction: Int)
  }

}

object ActionSet {
  def loadFromFile(file: File) = {
    val events = Events.loadFromFile(file)
    val actionSet = new ActionSet
    events.foreach(actionSet.performEvent)
    actionSet
  }
}

object Events {
  val INVALID_TIME_STAMP = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofTotalSeconds(TimeZone.getDefault.getRawOffset / 1000))

  def loadFromFile(file: File) = {
    val root = XML.loadFile(file)
    val eventsSeq = root.child.filter(e => e.label == "events")
    val events = eventsSeq.map(loadAtomicEvents)
    events
  }

  def saveToFile(events: TraversableOnce[AtomicEvent], file: File) = {
    val document = <todo>
      {for (atomicEvent <- events) yield atomicEvent.toXMLNode}
    </todo>
    val pp = new scala.xml.PrettyPrinter(80, 4)
    val output = pp.format(document)
    val pw = new PrintWriter(file)
    pw.println(output)
    pw.close
  }

  def timeToString(time: LocalDateTime) = (time.toEpochSecond(ZoneOffset.ofTotalSeconds(TimeZone.getDefault.getRawOffset / 1000)) * 1000 + time.getNano / 1000000).toString

  def stringToTime(timeString: String) = {
    val t = timeString.toLong
    LocalDateTime.ofEpochSecond(t / 1000, (t % 1000).toInt * 1000000, ZoneOffset.ofTotalSeconds(TimeZone.getDefault.getRawOffset / 1000))
  }

  private def loadAtomicEvents(eventsNode: Node) = {
    val eventSeq = eventsNode.child.filter(e => e.label == "event")
    val timeStamp = (eventsNode.attribute("timeStamp")) match {
      case Some(x) => stringToTime(x.head.text)
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
      case "create" => Events.newCreateEvent(eventTargetID, timeStamp)
      case "modify" => {
        val key = (eventNode.attribute("key")) match {
          case Some(x) => x.head.text
          case _ => throw new RuntimeException("attribute \"key\" is required for modify event.")
        }
        val value = (eventNode.attribute("value")) match {
          case Some(x) => x.head.text
          case _ => throw new RuntimeException("attribute \"value\" is required for modify event.")
        }
        Events.newModifyEvent(eventTargetID, key, value, timeStamp)
      }
      case _ => throw new IllegalArgumentException(s"Event type '${eventType}' not recgonized.")
    }
  }

  private def eventXMLNode(event: Event) = {}

  def newCreateEvent(eventTargetID: Int, timeStamp: LocalDateTime = LocalDateTime.now) = new Event {
    def perform(actionSet: ActionSet): Unit = {
      actionSet.createAction(eventTargetID, timeStamp)
    }

    def reverse(actionSet: ActionSet): Unit = actionSet.removeAction(eventTargetID)

    def getTimeStamp = timeStamp

    override def toXMLNode = <event type="create" id={eventTargetID.toString}></event>
  }

  def newModifyEvent(eventTargetID: Int, key: String, value: String, timeStamp: LocalDateTime = LocalDateTime.now) = new Event {
    var oldValue: Option[Tuple2[String, LocalDateTime]] = None

    def perform(actionSet: ActionSet): Unit = {
      val action = actionSet.getAction(eventTargetID)
      oldValue = Some(action.modifyProperty(key, value, timeStamp))
    }

    def reverse(actionSet: ActionSet): Unit = {
      oldValue match {
        case Some(t) => {
          val action = actionSet.getAction(eventTargetID)
          action.modifyProperty(key, t._1, t._2)
        }
        case None => throw new IllegalStateException(s"The Event has not been performed, hence can not be reversed.")
      }
    }

    override def getTimeStamp = timeStamp

    override def toXMLNode = <event type="modify" id={eventTargetID.toString} key={key} value={value}></event>
  }
}

class AtomicEvent(events: TraversableOnce[Event]) extends Event {
  private val eventList = events.toList

  override def perform(actionSet: ActionSet) = {
    eventList.foreach(_.perform(actionSet))
  }

  override def reverse(actionSet: ActionSet) = eventList.reverse.foreach(_.reverse(actionSet))

  override def getTimeStamp = eventList.head.getTimeStamp

  override def toXMLNode = {
    val node: Node = <events timeStamp={Events.timeToString(getTimeStamp)}>
      {for (event <- eventList) yield event.toXMLNode}
    </events>
    node
  }
}

trait Event {
  def perform(actionSet: ActionSet): Unit

  def reverse(actionSet: ActionSet): Unit

  def toXMLNode: Node

  def getTimeStamp: LocalDateTime
}