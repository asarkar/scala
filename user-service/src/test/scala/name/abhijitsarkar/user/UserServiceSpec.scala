package name.abhijitsarkar.user

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import akka.event.NoLogging
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.StatusCodes.Conflict
import akka.http.scaladsl.testkit.ScalatestRouteTest
import name.abhijitsarkar.user.domain.User
import UserJsonSupport._
import akka.http.scaladsl.model.ContentTypes.`application/json`

class UserServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with UserService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  override val userRepository = new MockUserRepository

  "User service" should "find a single user with first name John" in {
    Get(s"/user?firstName=John") ~> route ~> check {
      status shouldBe OK
      contentType shouldBe(`application/json`)
      val users = responseAs[Seq[User]]

      verifySingleUser(users, "John", "")
    }
  }

  private def verifySingleUser(users: Seq[User], expectedFirstName: String, expectedLastName: String) {
    users.size shouldBe (1)

    val user = users.head

    user.firstName shouldBe (expectedFirstName)
    user.lastName shouldBe (expectedLastName)
  }

  "User service" should "not find any users with first name Johnny" in {
    Get(s"/user?firstName=Johnny") ~> route ~> check {
      status shouldBe NotFound
      contentType shouldBe(`application/json`)
      println(responseAs[String])
    }
  }

  "User service" should "find a single user with last name Doe" in {
    Get(s"/user?lastName=Doe") ~> route ~> check {
      status shouldBe OK
      contentType shouldBe(`application/json`)
      val users = responseAs[Seq[User]]

      verifySingleUser(users, "", "Doe")
    }
  }
  
  "User service" should "not find any users with last name Appleseed" in {
    Get(s"/user?lastName=Appleseed") ~> route ~> check {
      status shouldBe NotFound
      contentType shouldBe(`application/json`)
      println(responseAs[String])
    }
  }

  "User service" should "find a single user with first name John and last name Doe" in {
    Get(s"/user?firstName=John&lastName=Doe") ~> route ~> check {
      status shouldBe OK
      contentType shouldBe(`application/json`)
      val users = responseAs[Seq[User]]

      verifySingleUser(users, "John", "Doe")
    }
  }
  
  "User service" should "not find any users with first name Johnny and last name Appleseed" in {
    Get(s"/user?firstName=Johnny&lastName=Appleseed") ~> route ~> check {
      status shouldBe NotFound
      contentType shouldBe(`application/json`)
      println(responseAs[String])
    }
  }

  "User service" should "throw exception if neither first nor last name is present" in {
    Get(s"/user") ~> route ~> check {
      status shouldBe BadRequest
      contentType shouldBe(`application/json`)
      println(responseAs[String])
    }
  }

  "User service" should "create new user and return the resource URI" in {
    val user = User("1", "John", "Doe", "555-555-5555", None)

    Put(s"/user", user) ~> route ~> check {
      status shouldBe Created
      contentType shouldBe(`application/json`)
      responseAs[String] should endWith("/user/1")
      println(responseAs[String])
    }
  }
  
  "User service" should "not create new user" in {
    val user = User("junk", "John", "Doe", "555-555-5555", None)

    Put(s"/user", user) ~> route ~> check {
      status shouldBe Conflict
      contentType shouldBe(`application/json`)
      println(responseAs[String])
    }
  }

  "User service" should "update existing user and return the user id" in {
    val user = User("1", "John", "Doe", "555-555-5555", None)

    Post(s"/user", user) ~> route ~> check {
      status shouldBe OK
      contentType shouldBe(`application/json`)
      responseAs[String] shouldBe "1"
      println(responseAs[String])
    }
  }
  
  "User service" should "not update non existing user" in {
    val user = User("junk", "John", "Doe", "555-555-5555", None)

    Post(s"/user", user) ~> route ~> check {
      status shouldBe NotFound
      contentType shouldBe(`application/json`)
      println(responseAs[String])
    }
  }

  "User service" should "delete existing user and return the user id" in {
    Delete(s"/user/1") ~> route ~> check {
      status shouldBe OK
      contentType shouldBe(`application/json`)
      responseAs[String] shouldBe "1"
      println(responseAs[String])
    }
  }
  
  "User service" should "not delete non existing user" in {
    Delete(s"/user/junk") ~> route ~> check {
      status shouldBe NotFound
      contentType shouldBe(`application/json`)
      println(responseAs[String])
    }
  }
}