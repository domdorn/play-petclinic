import javax.inject.Inject

import play.api.http.{DefaultHttpRequestHandler, _}
import play.api.mvc.{Handler, RequestHeader}
import play.api.routing.Router

// see https://www.playframework.com/documentation/2.6.x/ScalaHttpRequestHandlers
class RequestHandler @Inject()(errorHandler: HttpErrorHandler,
                               configuration: HttpConfiguration, filters: HttpFilters,
                               authRouter: auth.Routes,
                               billingRouter: billing.Routes,
                               petClinicRouter: petclinic.Routes,
                               defaultHttpRequestHandler: DefaultHttpRequestHandler
                              ) extends DefaultHttpRequestHandler(Router.empty, errorHandler, configuration, filters) {

  override def routeRequest(request: RequestHeader): Option[Handler] = {
    request.host.split('.').headOption match {
      case Some("auth") => authRouter.routes.lift(request)
      case Some("billing") => billingRouter.routes.lift(request)
      case Some("petclinic") => petClinicRouter.routes.lift(request)
      case _ => super.routeRequest(request)
    }
  }

}