package com.ecommerce.auth;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  @GetMapping("/health")
  public Map<String,String> health(){ return Map.of("service","iam-service","status","UP"); }
}