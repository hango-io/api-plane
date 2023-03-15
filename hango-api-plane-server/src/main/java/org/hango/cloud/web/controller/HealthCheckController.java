package org.hango.cloud.web.controller;

import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "api/health", params = {"Version=2019-07-25"})
public class HealthCheckController extends BaseController {


    @RequestMapping(params = { "Action=Health" }, method = RequestMethod.GET)
    public String heathCheck(HttpServletRequest request, HttpServletResponse response) {
        return apiReturnInSilent(ApiPlaneErrorCode.Success);
    }


}
