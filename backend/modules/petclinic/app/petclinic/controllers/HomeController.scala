package petclinic.controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, ControllerComponents, RequestHeader, Results}

class HomeController @Inject() (cc: ControllerComponents) extends AbstractController(cc){

  def index() = cc.actionBuilder.apply {
    implicit req: RequestHeader =>
      Results.Ok(petclinic.views.html.index.render(req))
  }

}
