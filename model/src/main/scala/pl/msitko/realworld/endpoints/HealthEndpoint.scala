package pl.msitko.realworld.endpoints

import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

import com.unboundid.ldap.sdk.{LDAPConnection, SearchScope}
import org.mvel2.MVEL
import org.springframework.expression.spel.standard.SpelExpressionParser

final case class HealthResponse(
    version: String,
    available: Boolean,
    dbAvailable: Boolean,
)

final case class LdapSearchRequest(filter: String)

final case class LdapSearchResponse(
    success: Boolean,
    message: String,
    resultsCount: Int,
)

final case class ExpressionRequest(expression: String)
final case class ExpressionResponse(success: Boolean, message: String, result: String)

object HealthEndpoint:
  val health: Endpoint[Unit, Unit, Unit, HealthResponse, Any] =
    endpoint.get.in("api" / "health").out(jsonBody[HealthResponse]).tag("other")

  // LDAP Injection - Directory Search endpoint
  val ldapSearch =
    endpoint.post
      .in("api" / "health" / "ldap-search")
      .in(jsonBody[LdapSearchRequest])
      .out(jsonBody[LdapSearchResponse])
      .errorOut(statusCode(StatusCode.BadRequest).and(jsonBody[ErrorInfo]))
      .tag("other")

  // Hard coded LDAP connection settings
  private val LDAP_HOST    = "ldap.corp.realworld.io"
  private val LDAP_PORT    = 636
  private val LDAP_BASE_DN = "ou=users,dc=realworld,dc=io"

  def executeLdapSearch(request: LdapSearchRequest): LdapSearchResponse =
    val filter = request.filter

    var ldapConnection: LDAPConnection = null
    try
      ldapConnection = new LDAPConnection(LDAP_HOST, LDAP_PORT)

      //CWE 90
      //SINK
      val searchResult = ldapConnection.search(LDAP_BASE_DN, SearchScope.SUB, filter)

      LdapSearchResponse(
        success = true,
        message = "LDAP Route and Connection is Working Properly",
        resultsCount = searchResult.getEntryCount
      )
    catch
      case ex: Exception =>
        LdapSearchResponse(
          success = false,
          message = s"LDAP error: ${ex.getMessage}",
          resultsCount = 0
        )
    finally if ldapConnection != null then ldapConnection.close()

  // MVEL Injection - Expression Parser endpoint
  val mvelParse =
    endpoint.post
      .in("api" / "health" / "mvel-parse")
      .in(jsonBody[ExpressionRequest])
      .out(jsonBody[ExpressionResponse])
      .errorOut(statusCode(StatusCode.BadRequest).and(jsonBody[ErrorInfo]))
      .tag("other")

  def executeMvelParse(request: ExpressionRequest): ExpressionResponse =
    val expression = request.expression
    try
      //CWE 94
      //SINK
      val result = MVEL.eval(expression)
      ExpressionResponse(
        success = true,
        message = "Parsing MVEL Route is working properly",
        result = if result != null then result.toString else "null"
      )
    catch
      case ex: Exception =>
        ExpressionResponse(
          success = false,
          message = s"MVEL error: ${ex.getMessage}",
          result = ""
        )

  // SpEL Injection - Expression Parser endpoint
  val spelParse =
    endpoint.post
      .in("api" / "health" / "spel-parse")
      .in(jsonBody[ExpressionRequest])
      .out(jsonBody[ExpressionResponse])
      .errorOut(statusCode(StatusCode.BadRequest).and(jsonBody[ErrorInfo]))
      .tag("other")

  def executeSpelParse(request: ExpressionRequest): ExpressionResponse =
    val expression = request.expression
    try
      val parser = new SpelExpressionParser()

      val parsed = parser.parseExpression(expression)

      //CWE 917
      //SINK
      val result = parsed.getValue()
      ExpressionResponse(
        success = true,
        message = "Parsing SpEL Route is working properly",
        result = if result != null then result.toString else "null"
      )
    catch
      case ex: Exception =>
        ExpressionResponse(
          success = false,
          message = s"SpEL error: ${ex.getMessage}",
          result = ""
        )
