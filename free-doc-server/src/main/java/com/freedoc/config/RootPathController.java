package com.freedoc.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootPathController {

    @GetMapping({"", "/"})
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/{path:[^\\.]*}")
    public String forwardSingle() {
        return "forward:/index.html";
    }

    @GetMapping("/{segment1}/{path:[^\\.]*}")
    public String forwardTwoLevel() {
        return "forward:/index.html";
    }

    @GetMapping("/{segment1}/{segment2}/{path:[^\\.]*}")
    public String forwardThreeLevel() {
        return "forward:/index.html";
    }

    @GetMapping("/{segment1}/{segment2}/{segment3}/{path:[^\\.]*}")
    public String forwardFourLevel() {
        return "forward:/index.html";
    }
}
