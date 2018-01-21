package com.hwaipy.todo.action

import java.io.File

import org.scalatest._

class ActionTest extends FunSuite with BeforeAndAfter with BeforeAndAfterAll {

  override def beforeAll() {
  }

  override def afterAll() {
  }

  before {
  }

  after {
  }

  test("Test Load Events.") {
    val actionSet = Events.loadFromFile(new File("src/test/res/ActionTest/note.xml"))
    assert(actionSet.actions.map(a => a.title()).toSet == Set("ToDoProject", "Hydra", "Sydra", "ScalaH", "科学应用系统", "星载纠缠源文章", "初稿", "计算Tomography"))
    assert(actionSet.rootActions.map(a => a.title()).toSet == Set("ToDoProject", "Hydra", "科学应用系统", "星载纠缠源文章"))
  }

  test("Test ActionSet store and restore") {
    val actionSet = new ActionSet()
    val actionA = actionSet.eventCreateAction("Project A")
    println(actionA.id)
    println(actionA.title())
  }
}