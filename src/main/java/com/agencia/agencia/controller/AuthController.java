package com.agencia.agencia.controller;

import com.agencia.agencia.dto.JwtAuthResponse;
import com.agencia.agencia.dto.RegisterRequest;
import com.agencia.agencia.dto.UserProfileDto;
import com.agencia.agencia.model.Usuario;
import com.agencia.agencia.security.JwtTokenProvider;
import com.agencia.agencia.service.UsuarioService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UsuarioService usuarioService;
    private final JwtTokenProvider jwtProvider;

    public AuthController(AuthenticationManager authManager,
                          UsuarioService usuarioService,
                          JwtTokenProvider jwtProvider) {
        this.authManager = authManager;
        this.usuarioService = usuarioService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> apiRegister(@RequestBody RegisterRequest req, HttpServletResponse response) {
        System.out.println("✅ Recibida solicitud de registro para: " + req.getCorreo());

        Usuario u = new Usuario();
        u.setNombre(req.getNombre());
        u.setCorreo(req.getCorreo());
        u.setContrasena(req.getContrasena());
        u.setTelefono(req.getTelefono());
        u.setTipo_usuario(2);
        u.setRuta_imagen_usuario("/assets/img/FotoPerfil/usuario2.jpg");

        try {
            usuarioService.registrarUsuario(u);
            System.out.println("✅ Usuario registrado exitosamente en la base de datos.");
        } catch (Exception e) {
            System.out.println("❌ Error al registrar usuario: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }

        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getCorreo(), req.getContrasena())
            );
            System.out.println("🔐 Autenticación exitosa para: " + req.getCorreo());

            String token = jwtProvider.generarToken(auth);
            System.out.println("🔑 Token generado correctamente");

            // Establecer la cookie JWT_TOKEN
            Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
            jwtCookie.setHttpOnly(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600);
            jwtCookie.setSecure(false);
            jwtCookie.setAttribute("SameSite", "Lax");
            response.addCookie(jwtCookie);

            return ResponseEntity.ok(new JwtAuthResponse(token));

        } catch (Exception e) {
            System.out.println("❌ Error durante autenticación: " + e.getMessage());
            return ResponseEntity.status(403).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> apiLogin(@RequestBody com.agencia.agencia.dto.LoginRequest req, HttpServletResponse response) {
        System.out.println("📥 Intento de login con: " + req.getCorreo());

        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getCorreo(), req.getContrasena())
            );
            String token = jwtProvider.generarToken(auth);
            System.out.println("🔑 Token generado correctamente para login");

            // Establecer la cookie JWT_TOKEN
            Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
            jwtCookie.setHttpOnly(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600);
            jwtCookie.setSecure(false);
            jwtCookie.setAttribute("SameSite", "Lax");
            response.addCookie(jwtCookie);

            return ResponseEntity.ok(new JwtAuthResponse(token));
        } catch (Exception e) {
            System.out.println("❌ Error durante login: " + e.getMessage());
            return ResponseEntity.status(403).build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
    Usuario usuario = usuarioService.obtenerUsuarioDesdeAutenticacion(authentication);

    if (usuario == null) {
        return ResponseEntity.status(401).build();
    }

    UserProfileDto profile = new UserProfileDto(
            usuario.getId_usuario(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getRuta_imagen_usuario(),
            usuario.getTipo_usuario()
    );

    return ResponseEntity.ok(profile);
}

@PostMapping("/logout")
public ResponseEntity<?> logout(HttpServletResponse response) {
    // Crear cookie vacía para eliminar la JWT
    Cookie cookie = new Cookie("JWT_TOKEN", "");
    cookie.setMaxAge(0); // Expira inmediatamente
    cookie.setPath("/"); 
    cookie.setHttpOnly(false); // (igual que cuando la creaste)
    cookie.setSecure(false);
    cookie.setAttribute("SameSite", "Lax");

    response.addCookie(cookie);

    return ResponseEntity.ok().build();
}


}