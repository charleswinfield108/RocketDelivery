package com.rocketFoodDelivery.rocketFood.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home Controller for RocketDelivery Application.
 *
 * Provides entry point to the application.
 *
 * @author RocketDelivery Team
 * @version 1.0
 */
@Controller
@Slf4j
@SuppressWarnings("null")
public class HomeController {

    /**
     * Display home page with link to back office.
     * GET /
     *
     * @return template name: index
     */
    @GetMapping("/")
    public String home() {
        log.info("Loading home page");
        return "index";
    }
}
