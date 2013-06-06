package test.parser.message

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.parboiled.scala._
import org.parboiled.scala.parserunners.ReportingParseRunner
import org.parboiled.scala.Input

trait ParboiledTestHelper {
  this: WordSpec with ShouldMatchers =>

  def runWithFailure[E <: Throwable](rule: Rule0, input: Input)(f: => Unit)(implicit manifest: Manifest[E]) {
    intercept[E] {
      ReportingParseRunner(rule).run(input)
      f
    }
  }

  def runWithFailure[E <: Throwable](rule: Rule1[_], input: Input)(f: => Unit)(implicit manifest: Manifest[E]) {
    intercept[E] {
      ReportingParseRunner(rule).run(input)
      f
    }
  }

  def run(rule: Rule0, input: Input): Boolean = {
    ReportingParseRunner(rule).run(input).matched
  }

  def run[A](rule: Rule1[A], input: Input)(f: (Option[A]) => Unit) {
    val result = ReportingParseRunner(rule).run(input)
    f(result.result)
  }
}
