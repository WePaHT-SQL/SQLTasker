/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wepaht.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Kake
 */
@Controller

public class DefaultController {
    
    @ResponseBody
    @RequestMapping(value="*", method=RequestMethod.GET)
    public String hello(){
        return "Hello World!";
    }
}
