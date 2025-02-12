package csw.subsync.user.controller;

import csw.subsync.user.doc.AuthControllerDoc;
import csw.subsync.user.dto.AuthenticationRequest;
import csw.subsync.user.dto.AuthenticationResponse;
import csw.subsync.user.dto.RefreshTokenRequest;
import csw.subsync.user.dto.UserRegisterRequest;
import csw.subsync.user.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// NOT VERSIONING
@RestController
@RequestMapping("/auth")
public class AuthController implements AuthControllerDoc {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody UserRegisterRequest request
    ) {
        authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @Override
    @GetMapping("/username-exists")
    public ResponseEntity<Boolean> checkUsernameExists(@RequestParam String username) {
        boolean exists = authenticationService.usernameExists(username);
        return ResponseEntity.ok(exists);
    }
}