package csw.subsync.user.service;

import csw.subsync.common.config.security.JwtService;
import csw.subsync.common.exception.DuplicateResourceException;
import csw.subsync.user.dto.AuthenticationRequest;
import csw.subsync.user.dto.AuthenticationResponse;
import csw.subsync.user.dto.RefreshTokenRequest;
import csw.subsync.user.dto.UserRegisterRequest;
import csw.subsync.user.model.User;
import csw.subsync.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Service
@RequiredArgsConstructor
class AuthenticationServiceTest {

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private AuthenticationManager authenticationManager;

    private AuthenticationService authenticationService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtService = Mockito.mock(JwtService.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);
        authenticationService = new AuthenticationService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }


    @Test
    void register_ValidRequest_UserSaved() {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .username("testUser")
                .email("test@example.com")
                .password("password")
                .build();

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        authenticationService.register(request);

        verify(userRepository, times(1)).existsByUsername(request.username());
        verify(userRepository, times(1)).existsByEmail(request.email());
        verify(passwordEncoder, times(1)).encode(request.password());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateUsername_ThrowsDuplicateResourceException() {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .username("existingUser")
                .email("test@example.com")
                .password("password")
                .build();

        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authenticationService.register(request));

        verify(userRepository, times(1)).existsByUsername(request.username());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsDuplicateResourceException() {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .username("testUser")
                .email("existing@example.com")
                .password("password")
                .build();

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authenticationService.register(request));

        verify(userRepository, times(1)).existsByUsername(request.username());
        verify(userRepository, times(1)).existsByEmail(request.email());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_ValidRequest_ReturnsAuthenticationResponse() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("testUser")
                .password("password")
                .build();
        User mockUser = User.builder().username("testUser").password("encodedPassword").build();
        when(authenticationManager.authenticate(any())).thenReturn(Mockito.mock(UsernamePasswordAuthenticationToken.class));
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser)).thenReturn("mockedAccessToken");
        when(jwtService.generateRefreshToken(mockUser)).thenReturn("mockedRefreshToken");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("mockedAccessToken", response.accessToken());
        assertEquals("mockedRefreshToken", response.refreshToken());

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).findByUsername(request.username());
        verify(jwtService, times(1)).generateToken(mockUser);
        verify(jwtService, times(1)).generateRefreshToken(mockUser);
    }

    @Test
    void authenticate_InvalidCredentials_ThrowsException() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("invalidUser")
                .password("wrongPassword")
                .build();

        when(authenticationManager.authenticate(any())).thenThrow(Mockito.mock(AuthenticationException.class));

        assertThrows(Exception.class, () -> authenticationService.authenticate(request)); // Or specific AuthenticationException if you want to test for that

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    void refreshToken_ValidToken_ReturnsNewAuthenticationResponse() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("validRefreshToken")
                .build();
        User mockUser = User.builder().username("testUser").password("encodedPassword").build();

        when(jwtService.extractUsername(request.refreshToken())).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(jwtService.isTokenValid(request.refreshToken(), mockUser)).thenReturn(true);
        when(jwtService.generateToken(mockUser)).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(mockUser)).thenReturn("newRefreshToken");

        AuthenticationResponse response = authenticationService.refreshToken(request);

        assertNotNull(response);
        assertEquals("newAccessToken", response.accessToken());
        assertEquals("newRefreshToken", response.refreshToken());

        verify(jwtService, times(1)).extractUsername(request.refreshToken());
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(jwtService, times(1)).isTokenValid(request.refreshToken(), mockUser);
        verify(jwtService, times(1)).generateToken(mockUser);
        verify(jwtService, times(1)).generateRefreshToken(mockUser);
    }

    @Test
    void refreshToken_InvalidToken_ThrowsRuntimeException() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalidRefreshToken")
                .build();

        when(jwtService.extractUsername(request.refreshToken())).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(User.builder().build())); // User exists
        when(jwtService.isTokenValid(Mockito.eq(request.refreshToken()), Mockito.any())).thenReturn(false); // Token invalid - using Mockito.eq and Mockito.any

        assertThrows(RuntimeException.class, () -> authenticationService.refreshToken(request));

        verify(jwtService, times(1)).extractUsername(request.refreshToken());
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(jwtService, times(1)).isTokenValid(Mockito.eq(request.refreshToken()), Mockito.any());
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    void refreshToken_UserNotFound_ThrowsRuntimeException() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("refreshToken")
                .build();

        when(jwtService.extractUsername(request.refreshToken())).thenReturn("nonExistentUser");
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authenticationService.refreshToken(request));

        verify(jwtService, times(1)).extractUsername(request.refreshToken());
        verify(userRepository, times(1)).findByUsername("nonExistentUser");
        verify(jwtService, never()).isTokenValid(any(), any());
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    void usernameExists_Exists_ReturnsTrue() {
        String username = "existingUser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        boolean exists = authenticationService.usernameExists(username);

        assertTrue(exists);
        verify(userRepository, times(1)).existsByUsername(username);
    }

    @Test
    void usernameExists_NotExists_ReturnsFalse() {
        String username = "nonExistingUser";
        when(userRepository.existsByUsername(username)).thenReturn(false);

        boolean exists = authenticationService.usernameExists(username);

        assertFalse(exists);
        verify(userRepository, times(1)).existsByUsername(username);
    }

    @Test
    void emailExists_Exists_ReturnsTrue() {
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean exists = authenticationService.emailExists(email);

        assertTrue(exists);
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    void emailExists_NotExists_ReturnsFalse() {
        String email = "nonExisting@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        boolean exists = authenticationService.emailExists(email);

        assertFalse(exists);
        verify(userRepository, times(1)).existsByEmail(email);
    }
}