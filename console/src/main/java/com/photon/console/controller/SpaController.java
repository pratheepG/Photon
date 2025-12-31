package com.photon.console.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping({
            "/server-console",
            "/server-console/",
            "/server-console/{path:[^\\.]*}",
            "/server-console/**/{path:[^\\.]*}"
    })
    public String forward() {
        return "forward:/server-console/index.html";
    }

}