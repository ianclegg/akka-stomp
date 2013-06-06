package test.parser.message

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.parboiled.errors.ParserRuntimeException
import com.github.nrf110.stomp.parser.StompParser
import com.github.nrf110.stomp.message.{ ContentType, MediaTypes, StompHeaders, StompCharsets }
import StompHeaders._
import StompCharsets._

class ContentTypeHeaderSpec
  extends WordSpec
  with ShouldMatchers
  with ParboiledTestHelper {

  "*Content-Type" should {
    "correctly parse application/json" in {
      run(StompParser.`*content-type`, "application/json") { result =>
        result.get should be (`content-type`(ContentType(MediaTypes.`application/json`)))
      }
    }

    "correctly parse text/plain; charset=utf8" in {
      run(StompParser.`*content-type`, "text/plain; charset=utf8") { result =>
        result.get should be (`content-type`(ContentType(MediaTypes.`text/plain`, `UTF-8`)))
      }
    }

    "correctly parse multipart/mixed; boundary=ABC123" in {
      run(StompParser.`*content-type`, "multipart/mixed; boundary=ABC123") { result =>
        result.get should be (`content-type`(ContentType(new MediaTypes.`multipart/mixed`(Some("ABC123")))))
      }
    }

    "should fail on invalid charset" in {
      runWithFailure[ParserRuntimeException](StompParser.`*content-type`, "text/plain; charset=fancy-pants") {
      }
    }
  }
}
