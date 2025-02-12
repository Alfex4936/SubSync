package csw.subsync.subscription.doc;

import csw.subsync.subscription.dto.SubscriptionCreateRequest;
import csw.subsync.subscription.dto.SubscriptionGroupDto;
import csw.subsync.subscription.dto.SubscriptionJoinRequest;
import csw.subsync.subscription.model.PredefinedSubscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Interface-based documentation for SubscriptionController
@Tag(name = "Subscription", description = "Subscription API")
public interface SubscriptionControllerDoc {

    /**
     * 구독 그룹 생성 API
     *
     * @param request 구독 그룹 생성 요청 DTO
     * @return ResponseEntity<SubscriptionGroupDto> 생성된 구독 그룹 정보 DTO와 201 Created 응답
     */
    @Operation(
            summary = "구독 그룹 생성",
            description = "새로운 구독 그룹을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "구독 그룹 생성 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = SubscriptionGroupDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 유효하지 않은 입력값)",
                            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Invalid input\"}"))),
                    @ApiResponse(responseCode = "401", description = "인증 실패",
                            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Unauthorized\"}")))
            }
    )
    @PostMapping("/create")
    ResponseEntity<SubscriptionGroupDto> create(@RequestBody @Valid SubscriptionCreateRequest request);


    /**
     * 구독 그룹 참여 API
     *
     * @param request 구독 그룹 참여 요청 DTO (groupId)
     * @return ResponseEntity<SubscriptionGroupDto> 참여된 구독 그룹 정보 DTO와 200 OK 응답
     */
    @Operation(
            summary = "구독 그룹 참여",
            description = "기존 구독 그룹에 참여합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "구독 그룹 참여 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = SubscriptionGroupDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 그룹 ID 누락)",
                            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Invalid input\"}"))),
                    @ApiResponse(responseCode = "404", description = "구독 그룹을 찾을 수 없음",
                            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Group not found\"}"))),
                    @ApiResponse(responseCode = "409", description = "그룹 참여 충돌 (예: 그룹이 꽉 참, 이미 참여)",
                            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Join conflict\"}"))),
                    @ApiResponse(responseCode = "401", description = "인증 실패",
                            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Unauthorized\"}")))
            }
    )
    @PostMapping("/join")
    ResponseEntity<SubscriptionGroupDto> join(@RequestBody @Valid SubscriptionJoinRequest request);


    /**
     * 구독 그룹 삭제 API
     *
     * @param groupId 삭제할 구독 그룹 ID (쿼리 파라미터)
     * @return ResponseEntity<Void> 성공 시 204 No Content 응답
     */
    @Operation(
            summary = "구독 그룹 삭제",
            description = "구독 그룹을 삭제합니다. 그룹 소유자 또는 관리자만 삭제할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "구독 그룹 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (그룹 소유자 또는 관리자만 삭제 가능)"),
                    @ApiResponse(responseCode = "404", description = "구독 그룹을 찾을 수 없음",
                            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Group not found\"}")))
            }
    )
    @DeleteMapping("/remove")
    ResponseEntity<Void> remove(@Parameter(description = "삭제할 구독 그룹 ID") @RequestParam Long groupId);


    /**
     * 멤버 요금 청구 API
     *
     * @param groupId 요금을 청구할 구독 그룹 ID (쿼리 파라미터)
     * @return ResponseEntity<Void> 성공 시 200 OK 응답
     */
    @Operation(
            summary = "멤버 요금 청구",
            description = "구독 그룹의 모든 멤버에게 요금을 청구합니다. 그룹 소유자 또는 관리자만 실행할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "멤버 요금 청구 성공"),
                    @ApiResponse(responseCode = "404", description = "구독 그룹을 찾을 수 없음",
                            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Group not found\"}"))),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (그룹 소유자 또는 관리자만 실행 가능)"),
                    @ApiResponse(responseCode = "500", description = "요금 청구 실패 (서버 에러)",
                            content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"error\": \"Payment processing error\"}")))
            }
    )
    @PostMapping("/charge")
    ResponseEntity<Void> charge(@Parameter(description = "요금을 청구할 구독 그룹 ID") @RequestParam Long groupId);


    /**
     * 미리 정의된 구독 목록 조회 API
     *
     * @return ResponseEntity<List < PredefinedSubscription>> 미리 정의된 구독 목록과 200 OK 응답
     */
    @Operation(
            summary = "미리 정의된 구독 목록 조회",
            description = "미리 정의된 구독 상품 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "미리 정의된 구독 목록 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = List.class, subTypes = {PredefinedSubscription.class})))
            }
    )
    @GetMapping("/list")
    ResponseEntity<List<PredefinedSubscription>> getPredefinedSubscriptionList();
}