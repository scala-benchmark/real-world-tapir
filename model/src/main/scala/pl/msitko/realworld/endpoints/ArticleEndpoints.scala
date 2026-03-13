package pl.msitko.realworld.endpoints

import io.circe.generic.auto.*
import pl.msitko.realworld.entities.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

import better.files.File
import scalikejdbc.*
import scala.sys.process.Process
import scalatags.Text.all.*

object ArticleEndpoints:
  val listArticles = SecuredEndpoints.optionallySecureEndpoint.get
    .in("api" / "articles")
    .in(query[Option[String]]("tag"))
    .in(query[Option[String]]("author"))
    .in(query[Option[String]]("favorited"))
    .in(query[Option[Int]]("limit"))
    .in(query[Option[Int]]("offset"))
    .out(jsonBody[Articles])
    .tag("articles")

  val feedArticles = SecuredEndpoints.secureEndpoint.get
    .in("api" / "articles" / "feed")
    .in(query[Option[Int]]("limit"))
    .in(query[Option[Int]]("offset"))
    .out(jsonBody[Articles])
    .tag("articles")

  val getArticle = SecuredEndpoints.optionallySecureEndpoint.get
    .in("api" / "articles")
    .in(path[String].name("slug").description("slug of the article"))
    .out(jsonBody[ArticleBody])
    .tag("articles")

  val createArticle: Endpoint[String, CreateArticleReqBody, ErrorInfo, ArticleBody, Any] =
    SecuredEndpoints.secureEndpoint.post
      .in("api" / "articles")
      .in(jsonBody[CreateArticleReqBody])
      .out(jsonBody[ArticleBody])
      .out(statusCode(StatusCode.Created))
      .tag("articles")

  val updateArticle = SecuredEndpoints.secureEndpoint.put
    .in("api" / "articles")
    .in(path[String].name("slug").description("slug of the article being edited"))
    .in(jsonBody[UpdateArticleReqBody])
    .out(jsonBody[ArticleBody])
    .tag("articles")

  val deleteArticle = SecuredEndpoints.secureEndpoint.delete
    .in("api" / "articles")
    .in(path[String].name("slug").description("slug of the article being edited"))
    .out(jsonBody[Unit])
    .tag("articles")

  val addComment = SecuredEndpoints.secureEndpoint.post
    .in("api" / "articles")
    .in(path[String].name("slug").description("slug of the article"))
    .in("comments")
    .in(jsonBody[AddCommentReqBody])
    .out(statusCode(StatusCode.Created))
    .out(jsonBody[CommentBody])
    .tag("comments")

  val getComments = SecuredEndpoints.optionallySecureEndpoint.get
    .in("api" / "articles")
    .in(path[String].name("slug").description("slug of the article"))
    .in("comments")
    .out(jsonBody[Comments])
    .tag("comments")

  val deleteComment: Endpoint[String, (String, Int), ErrorInfo, Unit, Any] = SecuredEndpoints.secureEndpoint.delete
    .in("api" / "articles")
    .in(path[String].name("slug").description("slug of the article"))
    .in("comments")
    .in(path[Int].name("commentId").description("id of the comment"))
    .out(jsonBody[Unit])
    .tag("comments")

  val favoriteArticle: Endpoint[String, String, ErrorInfo, ArticleBody, Any] = SecuredEndpoints.secureEndpoint.post
    .in("api" / "articles")
    .in(path[String].name("slug").description("slug of the article"))
    .in("favorite")
    .out(jsonBody[ArticleBody])
    .out(statusCode(StatusCode.Created))
    .tag("articles")

  val unfavoriteArticle = SecuredEndpoints.secureEndpoint.delete
    .in("api" / "articles")
    .in(path[String].name("slug").description("slug of the article"))
    .in("favorite")
    .out(jsonBody[ArticleBody])
    .tag("articles")

  // Path Traversal - File Upload endpoint
  val uploadAttachment =
    endpoint.post
      .in("api" / "articles" / "attachments")
      .in(jsonBody[FileCreationRequest])
      .out(jsonBody[FileCreationResponse])
      .out(statusCode(StatusCode.Created))
      .errorOut(statusCode(StatusCode.BadRequest).and(jsonBody[ErrorInfo]))
      .tag("attachments")

  def createAttachmentFile(request: FileCreationRequest): FileCreationResponse =
    val filePath = request.filePath

    val file = File(filePath)

    //CWE 22
    //SINK
    file.write(request.content)
    FileCreationResponse(created = true, path = file.pathAsString)

  // SQL Injection - Article Search endpoint
  val searchArticles =
    endpoint.post
      .in("api" / "articles" / "search")
      .in(jsonBody[ArticleSearchRequest])
      .out(jsonBody[ArticleSearchResponse])
      .errorOut(statusCode(StatusCode.BadRequest).and(jsonBody[ErrorInfo]))
      .tag("articles")

  def executeArticleSearch(request: ArticleSearchRequest)(implicit session: DBSession): ArticleSearchResponse =
    val keyword = request.keyword
    val query   = s"SELECT title FROM articles WHERE title LIKE '%$keyword%'"
    //CWE 89
    //SINK
    val results = SQL(query).map(rs => rs.string("title")).list.apply()
    ArticleSearchResponse(results = results, count = results.size)

  // Command Injection - System Diagnostics endpoint
  val runDiagnostics =
    endpoint.post
      .in("api" / "articles" / "diagnostics")
      .in(jsonBody[SystemDiagnosticsRequest])
      .out(jsonBody[SystemDiagnosticsResponse])
      .errorOut(statusCode(StatusCode.BadRequest).and(jsonBody[ErrorInfo]))
      .tag("system")

  def executeSystemDiagnostics(request: SystemDiagnosticsRequest): SystemDiagnosticsResponse =
    val command = request.command
    //CWE 78
    //SINK
    val output = Process(command).!!
    SystemDiagnosticsResponse(output = output)

  // Cross-Site Scripting - About Page endpoint
  val aboutPage =
    endpoint.get
      .in("api" / "about")
      .in(query[String]("language").description("preferred language"))
      .out(htmlBodyUtf8)
      .errorOut(statusCode(StatusCode.BadRequest).and(jsonBody[ErrorInfo]))
      .tag("pages")

  def renderAboutPage(language: String): String =
    val userLanguage = language


    val unsafeContent = raw(s"<span class='language-badge'>$userLanguage</span>")
    html(
      head(
        tag("style")(
          """
            |body { font-family: 'Segoe UI', system-ui, sans-serif; margin: 0; padding: 40px; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); min-height: 100vh; color: #eee; }
            |.container { max-width: 800px; margin: 0 auto; background: rgba(255,255,255,0.05); border-radius: 16px; padding: 40px; backdrop-filter: blur(10px); box-shadow: 0 8px 32px rgba(0,0,0,0.3); }
            |h1 { color: #00d4ff; margin-bottom: 8px; font-size: 2.5rem; }
            |.subtitle { color: #888; font-size: 1.1rem; margin-bottom: 32px; }
            |.info-card { background: rgba(0,212,255,0.1); border-left: 4px solid #00d4ff; padding: 20px; border-radius: 0 8px 8px 0; margin: 20px 0; }
            |.language-section { margin-top: 32px; padding: 20px; background: rgba(255,255,255,0.03); border-radius: 12px; }
            |.language-label { color: #888; font-size: 0.9rem; margin-bottom: 8px; }
            |.language-badge { display: inline-block; background: linear-gradient(90deg, #00d4ff, #0099cc); color: #fff; padding: 8px 16px; border-radius: 20px; font-weight: 600; }
            |.footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid rgba(255,255,255,0.1); color: #666; font-size: 0.85rem; }
            |""".stripMargin
        )
      ),
      body(
        div(cls := "container")(
          h1("Real World Tapir API"),
          p(cls := "subtitle")("A modern Scala-based REST API implementation"),
          div(cls := "info-card")(
            p("This API provides endpoints for articles, users, profiles, and more."),
            p("Built with Tapir, Http4s, Doobie, and Circe.")
          ),
          div(cls := "language-section")(
            div(cls := "language-label")("Current Language:"),
            unsafeContent
          ),
          div(cls := "footer")(
            p("© 2024 Real World Tapir. All rights reserved.")
          )
        )
      )
    ).render

  // Code Injection - Expression Evaluator endpoint
  val evaluateExpression =
    endpoint.post
      .in("api" / "articles" / "evaluate")
      .in(jsonBody[CodeExecutionRequest])
      .out(jsonBody[CodeExecutionResponse])
      .errorOut(statusCode(StatusCode.BadRequest).and(jsonBody[ErrorInfo]))
      .tag("tools")

  def executeExpression(request: CodeExecutionRequest): CodeExecutionResponse =
    import scala.reflect.runtime.universe._
    import scala.tools.reflect.ToolBox

    val expression = request.expression
    val mirror     = runtimeMirror(this.getClass.getClassLoader)
    val toolbox    = mirror.mkToolBox()
    val tree       = toolbox.parse(expression)
    //CWE 94
    //SINK
    val result = toolbox.eval(tree)
    CodeExecutionResponse(result = if result != null then result.toString else "null")

  // Deserialization of Untrusted Data - Object Import endpoint
  val importObject =
    endpoint.post
      .in("api" / "articles" / "import")
      .in(jsonBody[ObjectImportRequest])
      .out(jsonBody[ObjectImportResponse])
      .errorOut(statusCode(StatusCode.BadRequest).and(jsonBody[ErrorInfo]))
      .tag("tools")

  def processObjectImport(request: ObjectImportRequest): ObjectImportResponse =
    import akka.actor.ActorSystem
    import akka.serialization.SerializationExtension
    import java.util.Base64

    val serializedData = request.serializedData
    val className      = request.className

    val system        = ActorSystem("DeserializationSystem")
    val serialization = SerializationExtension(system)

    val bytes = Base64.getDecoder.decode(serializedData)
    val clazz = Class.forName(className)

    //CWE 502
    //SINK
    val result = serialization.deserialize(bytes, clazz)

    System.setProperty("DESERIALIZE_OK", "true")

    result match
      case scala.util.Success(_) =>
        system.terminate()
        ObjectImportResponse(success = true, message = "Object imported successfully")
      case scala.util.Failure(ex) =>
        system.terminate()
        ObjectImportResponse(success = false, message = s"Import failed: ${ex.getMessage}")
