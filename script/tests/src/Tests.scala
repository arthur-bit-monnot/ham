package ham.script

import java.nio.file.{Files, Path, Paths}

import cats.implicits._

import ham.errors._
import ham.expr.ModuleID
import ham.interpreter.Interpreter
import ham.parsing.Decl
import ham.prelude.Prelude

import minitest._

object Tests extends SimpleTestSuite {

  val parser: String => Attempt[List[Decl]] =
    ham.parsing.modules.Parser.default.declarations(_).leftMap(ParseError).toAttempt

  test("valid") {
    //println("\n======= Valid tests =======")
    forEachHamFilesIn("/tests/positive", checkValid)
  }

  test("invalid") {
    //println("\n======= Invalid tests =======")
    forEachHamFilesIn("/tests/negative", checkInvalid)
  }

  def forEachHamFilesIn(dir: String, run: Path => Unit): Unit = {
    val clazz = this.getClass
    val res   = clazz.getResource(dir)
    val uri   = res.toURI
    val path  = Paths.get(uri)
    Files
      .list(path)
      .forEach(p => run(p))
  }

  def checkValid(p: Path): Unit = {
//    println
    val id      = p.getFileName.toString.replaceAll(".ham", "")
    val content = Files.readAllLines(p).toArray().mkString("\n")
    val loader  = Prelude.getLoader()
    loader.loadFromSource(ModuleID(id), content, parser) match {
      case Succ(typed) =>
        //println(s"Successfully typed $id")
        //typed.types.foreach { case (k, v) => println(s"$k : $v") }

        typed.mod.mainFunction match {
          case None =>
          // println("No main")
          // no main to try running
          case Some(mainId) =>
            loader.definitionOf(mainId) match {
              case Succ(mainExpr) =>
                Interpreter.eval(mainExpr, loader.definitionOf) match {
                  case Succ(res) =>
                  //println(s"main = $res")
                  case Fail(err) =>
                    System.err.println(s"Error while evaluating main: ${err.msg}")
                    throw err
                }
              case Fail(err) =>
                sys.error("Could not find definition of main method")
                System.exit(1)
            }

        }
      case Fail(err) =>
        System.err.println(s"Failed to type check: $p")
        System.err.println(content)
        System.err.println(err)
        throw err
    }
  }

  def checkInvalid(p: Path): Unit = {
//    println
    val id      = p.getFileName.toString.replaceAll(".ham", "")
    val content = Files.readAllLines(p).toArray().mkString("\n")
    val loader  = Prelude.getLoader()
    loader.loadFromSource(ModuleID(id), content, parser) match {
      case Succ(typed) =>
        System.err.println(s"Error: successfully typed $p")
        System.err.println(content)
        typed.types.foreach { case (k, v) => System.err.println(s"$k : $v") }
        System.exit(1)
      case Fail(err) =>
      //println(s"OK: failed to type check: $id")
      //println(err)
    }
  }

}
