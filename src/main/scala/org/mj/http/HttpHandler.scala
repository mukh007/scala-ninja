package org.mj.http

import com.typesafe.scalalogging.LazyLogging
import scalaj.http.BaseHttp
import scalaj.http.HttpConstants
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import org.mj.json.JsonSerDe
import org.mj.file.FunctionsHandler

class HttpHandler() extends LazyLogging {

  def getHttpPostDataResponse(endpointUrl: String, httpPayload: String, httpHeader: Seq[(String, String)] = Seq(), connTimeOutInMin: Double = 0.1, readTimeOutInMin: Double = 0.1): HttpResponse[Array[Byte]] = {
    val baseRequest: HttpRequest = HttpE(endpointUrl)
    val postRequest: HttpRequest = baseRequest
      .postData(httpPayload)
      .headers(httpHeader)
      .timeout((connTimeOutInMin * 60 * 1000).toInt, (readTimeOutInMin * 60 * 1000).toInt)
    postRequest.asBytes
  }

  def getHttpPostFormResponse(endpointUrl: String, httpPayload: Seq[(String, String)] = Seq(), httpHeader: Seq[(String, String)] = Seq(), connTimeOutInMin: Double = 0.1, readTimeOutInMin: Double = 0.1): HttpResponse[Array[Byte]] = {
    val baseRequest: HttpRequest = HttpE(endpointUrl)
    val postRequest: HttpRequest = baseRequest
      .postForm(httpPayload)
      .headers(httpHeader)
      .timeout((connTimeOutInMin * 60 * 1000).toInt, (readTimeOutInMin * 60 * 1000).toInt)
    postRequest.asBytes
  }

  // Extenssion
  private object HttpE extends BaseHttp(
    proxyConfig = None,
    options = HttpConstants.defaultOptions,
    charset = HttpConstants.utf8,
    sendBufferSize = 4096,
    userAgent = "mj",
    compress = true)
}