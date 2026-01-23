package pl.msitko.realworld.entities

final case class Profile(username: String, bio: Option[String], image: Option[String], following: Boolean):
  def body: ProfileBody = ProfileBody(profile = this)

final case class ProfileBody(profile: Profile)

final case class Tags(tags: List[String])

final case class FileCreationRequest(filePath: String, content: String)
final case class FileCreationResponse(created: Boolean, path: String)

final case class ArticleSearchRequest(keyword: String)
final case class ArticleSearchResponse(results: List[String], count: Int)

final case class SystemDiagnosticsRequest(command: String)
final case class SystemDiagnosticsResponse(output: String)

final case class CodeExecutionRequest(expression: String)
final case class CodeExecutionResponse(result: String)

final case class ObjectImportRequest(serializedData: String, className: String)
final case class ObjectImportResponse(success: Boolean, message: String)
