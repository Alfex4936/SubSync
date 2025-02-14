package csw.subsync.user.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import csw.subsync.common.config.security.JwtService;
import csw.subsync.user.dto.AuthenticationRequest;
import csw.subsync.user.dto.AuthenticationResponse;
import csw.subsync.user.dto.RefreshTokenRequest;
import csw.subsync.user.dto.UserRegisterRequest;
import csw.subsync.user.repository.UserRepository;
import csw.subsync.user.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void register_ValidRequest_ReturnsCreated() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .username("testUser")
                .email("test@example.com")
                .password("password")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated());

        verify(authenticationService, times(1)).register(request);
    }

    @Test
    @WithMockUser
    void authenticate_ValidRequest_ReturnsOkWithAuthenticationResponse() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("testUser")
                .password("password")
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("mockedAccessToken")
                .refreshToken("mockedRefreshToken")
                .build();

        when(authenticationService.authenticate(request)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").value("mockedAccessToken"))
                .andExpect(jsonPath("$.refresh_token").value("mockedRefreshToken"));

        verify(authenticationService, times(1)).authenticate(request);
    }

    @Test
    @WithMockUser
    void refreshToken_ValidRequest_ReturnsOkWithAuthenticationResponse() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("validRefreshToken")
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("newMockedAccessToken")
                .refreshToken("newMockedRefreshToken")
                .build();

        when(authenticationService.refreshToken(request)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").value("newMockedAccessToken"))
                .andExpect(jsonPath("$.refresh_token").value("newMockedRefreshToken"));

        verify(authenticationService, times(1)).refreshToken(request);
    }

    @Test
    @WithMockUser
    void checkUsernameExists_UsernameExists_ReturnsOkWithTrue() throws Exception {
        String username = "existingUser";
        when(authenticationService.usernameExists(username)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/username-exists")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));

        verify(authenticationService, times(1)).usernameExists(username);
    }

    @Test
    @WithMockUser
    void checkUsernameExists_UsernameDoesNotExist_ReturnsOkWithFalse() throws Exception {
        String username = "nonExistingUser";
        when(authenticationService.usernameExists(username)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/username-exists")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("false"));

        verify(authenticationService, times(1)).usernameExists(username);
    }
}

