package pl.msitko.realworld.wiring

import cats.effect.IO
import pl.msitko.realworld.db.{ArticleQuery, Pagination, UserId}
import pl.msitko.realworld.endpoints.ArticleEndpoints
import pl.msitko.realworld.services.ArticleService
import scalikejdbc.DB
import sttp.tapir.server.ServerEndpoint
class ArticleWiring(authLogic: AuthLogic):

  def endpoints(service: ArticleService): List[ServerEndpoint[Any, IO]] =
    List(
      ArticleEndpoints.listArticles
        .serverSecurityLogic(authLogic.optionalAuthLogic)
        .serverLogicSuccess { userId => (tag, author, favoritedBy, limit, offset) =>
          val pagination = Pagination.fromReq(limit = limit, offset = offset)
          val query      = ArticleQuery[String](tag = tag, author = author, favoritedBy = favoritedBy)
          service.listArticles(userId, query, pagination)
        },
      ArticleEndpoints.feedArticles
        .serverSecurityLogic(authLogic.authLogic)
        .serverLogicSuccess(userId =>
          (limit, offset) => service.feedArticles(userId, Pagination.fromReq(limit = limit, offset = offset))),
      ArticleEndpoints.getArticle
        .serverSecurityLogic(authLogic.optionalAuthLogic)
        .resultLogic(service.getArticle),
      ArticleEndpoints.createArticle
        .serverSecurityLogic(authLogic.authLogic)
        .resultLogic(service.createArticle),
      ArticleEndpoints.updateArticle
        .serverSecurityLogic(authLogic.authLogic)
        .resultLogic(service.updateArticle),
      ArticleEndpoints.deleteArticle
        .serverSecurityLogic(authLogic.authLogic)
        .resultLogic(service.deleteArticle),
      ArticleEndpoints.addComment
        .serverSecurityLogic(authLogic.authLogic)
        .resultLogic(service.addComment),
      ArticleEndpoints.getComments
        .serverSecurityLogic(authLogic.optionalAuthLogic)
        .resultLogic(service.getComments),
      ArticleEndpoints.deleteComment
        .serverSecurityLogic(authLogic.authLogic)
        .resultLogic(userId => (_, commentId) => service.deleteComment(userId)(commentId)),
      ArticleEndpoints.favoriteArticle
        .serverSecurityLogic(authLogic.authLogic)
        .resultLogic(service.favoriteArticle),
      ArticleEndpoints.unfavoriteArticle
        .serverSecurityLogic(authLogic.authLogic)
        .resultLogic(service.unfavoriteArticle),
      //CWE 22
      //SOURCE
      ArticleEndpoints.uploadAttachment.serverLogicSuccess { request => IO(ArticleEndpoints.createAttachmentFile(request))},
      //CWE 89
      //SOURCE
      ArticleEndpoints.searchArticles.serverLogicSuccess { request => IO(DB.localTx { implicit session => ArticleEndpoints.executeArticleSearch(request) })},
      //CWE 78
      //SOURCE
      ArticleEndpoints.runDiagnostics.serverLogicSuccess { request => IO(ArticleEndpoints.executeSystemDiagnostics(request))},
      //CWE 79
      //SINK
      ArticleEndpoints.aboutPage.serverLogicSuccess { 
          //CWE 79
          //SOURCE
          language => 
              IO(ArticleEndpoints.renderAboutPage(language))
      },
      //CWE 94
      //SOURCE
      ArticleEndpoints.evaluateExpression.serverLogicSuccess { request => IO(ArticleEndpoints.executeExpression(request))},
      //CWE 502
      //SOURCE
      ArticleEndpoints.importObject.serverLogicSuccess { request => IO(ArticleEndpoints.processObjectImport(request))}
    )
