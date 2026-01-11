package com.raf.reservationservicevezbe.secutiry;

import com.raf.reservationservicevezbe.secutiry.service.TokenService;
import io.jsonwebtoken.Claims;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Configuration
public class SecurityAspect {

    private final TokenService tokenService;

    public SecurityAspect(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Around("@annotation(com.raf.reservationservicevezbe.secutiry.CheckSecurity)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. Dohvatanje metode
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        // 2. Traženje tokena u argumentima
        String token = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof String) {
                String strArg = (String) arg;
                if (strArg.startsWith("Bearer ")) {
                    token = strArg.split(" ")[1];
                    break;
                }
            }
        }

        System.out.println("------------------------------------------------");
        System.out.println("POZIV METODE: " + method.getName());
        if (token == null) {
            System.out.println("TOKEN STATUS: Nije pronadjen (Bearer string fali)");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            System.out.println("TOKEN STATUS: Pronadjen.");
            System.out.println("SADRZAJ TOKENA: " + token); // <--- Ovde cemo videti da li klijent salje gluposti
        }
        // ---------------------------------------------

        // 3. Parsiranje tokena (koristimo TokenService)
        Claims claims = tokenService.parseToken(token);

        if (claims == null) {
            System.out.println("GRESKA: TokenService nije uspeo da parsira token (vratio je null).");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 4. Provera Uloge
        CheckSecurity checkSecurity = method.getAnnotation(CheckSecurity.class);
        String role = claims.get("role", String.class);

        System.out.println("ROLE U TOKENU: " + role);
        System.out.println("DOZVOLJENE ROLE: " + Arrays.toString(checkSecurity.roles()));

        if (Arrays.asList(checkSecurity.roles()).contains(role)) {
            System.out.println("PRISTUP: ODOBREN");
            System.out.println("------------------------------------------------");
            return joinPoint.proceed();
        }

        System.out.println("PRISTUP: ZABRANJEN (Nedovoljno prava)");
        System.out.println("------------------------------------------------");
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}