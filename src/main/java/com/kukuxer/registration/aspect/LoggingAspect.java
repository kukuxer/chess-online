package com.kukuxer.registration.aspect;

import com.kukuxer.registration.domain.match.Match;
import com.kukuxer.registration.domain.user.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

@Aspect
@Component
public class LoggingAspect {
    String red = "\u001B[38;5;124m";
    String green = "\u001B[38;5;34m";
    String reset = "\u001B[0m";
    private Logger logger = Logger.getLogger(getClass().getName());

    @Pointcut("execution(* com.kukuxer.registration.controller.*.*(..))")
    private void forControllerPackage() {
    }

    @AfterReturning(
            pointcut = "forControllerPackage()",
            returning = "result"
    )
    public void afterControllerReturning(JoinPoint joinPoint, Object result) {
        String method = joinPoint.getSignature().toShortString();
        logger.info(green + "@After: calling method  " + method + reset);
        logger.info("The result: " + result);
    }

    @Pointcut("execution(* com.kukuxer.registration.service.*.*(..))")
    private void forServicePackage() {
    }

    @AfterReturning(
            pointcut = "forServicePackage()",
            returning = "result"
    )

    public void afterServiceReturning(JoinPoint joinPoint, Object result) {
        String method = joinPoint.getSignature().toShortString();
        if (result instanceof User) {
            result = ((User) result).getUsername();
            logger.info(red + "Match result for user " + result + reset);
        } else {
            logger.info(red + "The result: " + result + reset);
        }
    }
}

