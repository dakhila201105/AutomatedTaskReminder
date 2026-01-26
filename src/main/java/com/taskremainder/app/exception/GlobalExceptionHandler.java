package com.taskremainder.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

//@ControllerAdvice
public class GlobalExceptionHandler {

    // Ignore favicon and static resources
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFound() {
        // Do nothing
    }

    // Handle real application errors
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(HttpServletRequest req, Exception ex) {
        System.err.println("🔥 GlobalExceptionHandler caught exception at " + req.getRequestURL());
        ex.printStackTrace();

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("exception", ex);
        mav.addObject("url", req.getRequestURL());
        return mav;
    }
}
