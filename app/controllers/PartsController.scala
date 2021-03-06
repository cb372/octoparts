package controllers

import com.m3.octoparts.json.format.ConfigModel._
import com.m3.octoparts.json.format.ReqResp._
import com.m3.octoparts.aggregator.service.PartsService
import com.m3.octoparts.model._
import com.m3.octoparts.model.config.HttpPartConfig
import com.m3.octoparts.repository.ConfigsRepository
import com.wordnik.swagger.annotations._
import controllers.support.LoggingSupport
import org.apache.http.client.cache.HeaderConstants
import play.api.libs.concurrent.Promise
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._

@Api(
  value = "/octoparts/2",
  description = "Octoparts' backend endpoints API",
  produces = "application/json",
  consumes = "application/json"
)
class PartsController(
    partsService: PartsService,
    configsRepository: ConfigsRepository,
    requestTimeout: Duration,
    readClientCacheHeaders: Boolean) extends Controller with LoggingSupport {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  @ApiOperation(
    value = "Invoke registered endpoints",
    nickname = "Endpoints invocation",
    notes = "Send an AggregateRequest to invoke backend endpoints. Will respond with an AggregateResponse for you to sort through.",
    response = classOf[AggregateResponse],
    httpMethod = "POST")
  @ApiResponses(Array(new ApiResponse(code = 400, message = "Invalid input")))
  @ApiImplicitParams(Array(new ApiImplicitParam(
    value = "An AggregateRequest consisting of PartRequests that individually invoke a registered backend service once.",
    required = true,
    dataType = "com.m3.octoparts.model.AggregateRequest",
    paramType = "body",
    name = "body"
  )))
  def retrieveParts = Action.async(BodyParsers.parse.json) { implicit request =>
    request.body.validate[AggregateRequest].fold[Future[Result]](
      errors => {
        warnRc("Body" -> request.body.toString, "Errors" -> errors.toString)
        Future.successful(BadRequest("Unrecognized request object"))
      },
      aggregateRequest => {
        val noCache = readClientCacheHeaders && request.headers.get(HeaderConstants.CACHE_CONTROL) == Some(HeaderConstants.CACHE_CONTROL_NO_CACHE)
        logAggregateRequest(aggregateRequest, noCache)

        val fAggregateResponse = partsService.processParts(aggregateRequest, noCache)
        withRequestTimeout(fAggregateResponse)
      }
    )
  }

  @ApiOperation(
    value = "Return a list of all registered endpoints in the system",
    nickname = "Endpoints listing",
    notes = "Returns a list of registered endpoints in the system.",
    response = classOf[HttpPartConfig],
    responseContainer = "List",
    httpMethod = "GET")
  @ApiParam(allowMultiple = true,
    name = "partId",
    value = "Optional filter for the partId"
  )
  def list(partIdParams: List[String] = Nil) = Action.async { implicit request =>
    debugRc
    val fConfigs = partIdParams match {
      case Nil => configsRepository.findAllConfigs()
      case partIds =>
        val fParts = partIds.map(configsRepository.findConfigByPartId)
        Future.sequence(fParts).map(_.flatten)
    }

    fConfigs.map {
      configs => Ok(Json.toJson(configs.map(HttpPartConfig.toJsonModel)))
    }
  }

  private def logAggregateRequest(aggregateRequest: AggregateRequest, noCache: Boolean)(implicit request: RequestHeader): Unit = {
    val logData = Seq(
      "requestId" -> aggregateRequest.requestMeta.id,
      "noCache" -> noCache.toString,
      "timeoutMs" -> aggregateRequest.requestMeta.timeout.fold("default")(_.toMillis.toString),
      "requestUrl" -> aggregateRequest.requestMeta.requestUrl.getOrElse("unknown"),
      "numParts" -> aggregateRequest.requests.size.toString)
    if (underlyingLogger.isDebugEnabled) debugRc(logData: _*) else info(logData: _*)
  }

  private def withRequestTimeout(fResponse: Future[AggregateResponse]): Future[Result] = {
    val fOkResponse = fResponse.map(aggResp => Ok(Json.toJson(aggResp)))
    val fTimeout = Promise.timeout(InternalServerError("Request timed out"), requestTimeout)
    Future.firstCompletedOf(Seq(fOkResponse, fTimeout))
  }

}
