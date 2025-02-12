package csw.subsync.user.doc;

import csw.subsync.common.dto.ApiErrorResponse;
import csw.subsync.user.dto.AuthenticationRequest;
import csw.subsync.user.dto.AuthenticationResponse;
import csw.subsync.user.dto.RefreshTokenRequest;
import csw.subsync.user.dto.UserRegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

// Interface-based documentation for the AuthController
@Tag(name = "Authorization", description = "Authorization API")
public interface AuthControllerDoc {

    @Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "등록 성공", content = @Content),
            @ApiResponse(responseCode = "409", description = "사용자 이름 또는 이메일 중복", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/register")
    ResponseEntity<Void> register(@RequestBody UserRegisterRequest request);

    @Operation(summary = "사용자 인증", description = "사용자 이름과 비밀번호로 인증하고 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/authenticate")
    ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request);

    @Operation(summary = "토큰 갱신", description = "만료된 액세스 토큰을 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/refresh")
    ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request);

    @Operation(summary = "사용자 이름 존재 확인", description = "사용자 이름이 이미 존재하는지 확인합니다.")
    @GetMapping("/username-exists")
    ResponseEntity<Boolean> checkUsernameExists(@RequestParam String username);
}
