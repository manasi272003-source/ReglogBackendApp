package com.reglog.auth;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
@Component
public class JwtFilter extends OncePerRequestFilter {
 private final JwtService jwt;private final SessionRepository sessions;
 public JwtFilter(JwtService jwt,SessionRepository sessions){this.jwt=jwt;this.sessions=sessions;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String header=req.getHeader("Authorization");
  if(header!=null&&header.startsWith("Bearer "))try{var claims=jwt.parse(header.substring(7)).getPayload();var session=sessions.findByIdAndRevokedFalse(claims.getId()).orElse(null);if(session!=null&&session.expiresAt.isAfter(Instant.now())&&session.userId.toString().equals(claims.getSubject())){var auth=new UsernamePasswordAuthenticationToken(session.userId,null,java.util.List.of());auth.setDetails(session.id);SecurityContextHolder.getContext().setAuthentication(auth);}}catch(Exception ignored){SecurityContextHolder.clearContext();}
  chain.doFilter(req,res);
 }
}
