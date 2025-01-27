package csw.subsync.subscription.controller;

import csw.subsync.common.annotation.ApiV1;
import csw.subsync.common.exception.ResourceNotFoundException;
import csw.subsync.subscription.dto.SubscriptionCreateRequest;
import csw.subsync.subscription.dto.SubscriptionGroupDto;
import csw.subsync.subscription.dto.SubscriptionJoinRequest;
import csw.subsync.subscription.model.SubscriptionGroup;
import csw.subsync.subscription.service.SubscriptionService;
import csw.subsync.user.model.User;
import csw.subsync.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@ApiV1
@RequiredArgsConstructor
@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    // Group 생성
    @Operation(summary = "구독 그룹 생성", description = "새로운 구독 그룹을 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<SubscriptionGroupDto> create(@RequestBody @Valid SubscriptionCreateRequest request) {
        User owner = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        var group = subscriptionService.createGroup(
                owner,
                request.getTitle(),
                request.getMaxMembers(),
                request.getDurationDays(),
                request.getPricingModel(),
                request.getPriceAmount(),
                request.getPriceCurrency()
        );

        // 201 Created와 함께 DTO 반환
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDto(group));
    }

    // Group 참여
    @Operation(summary = "구독 그룹 참여", description = "기존 구독 그룹에 참여합니다.")
    @PostMapping("/join")
    public ResponseEntity<SubscriptionGroupDto> join(@RequestBody @Valid SubscriptionJoinRequest request) {
        // 사용자 확인
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        var group = subscriptionService.joinGroup(request.getGroupId(), user);

        // 200 OK
        return ResponseEntity.ok(toDto(group));
    }

    // Group 삭제
    // TODO: creator or admin만 가능하도록 권한 제어
    @Operation(summary = "구독 그룹 삭제", description = "구독 그룹을 삭제합니다.")
    @DeleteMapping("/remove")
    public ResponseEntity<Void> remove(@RequestParam Long groupId,
                                       @RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 삭제 실행
        subscriptionService.removeGroup(groupId, user);

        // 성공 시 204 No Content
        return ResponseEntity.noContent().build();
    }

    // 멤버 요금 청구
    @Operation(summary = "멤버 요금 청구", description = "구독 그룹의 모든 멤버에게 요금을 청구합니다.")
    @PostMapping("/charge")
    public ResponseEntity<Void> charge(@RequestParam Long groupId) {
        subscriptionService.chargeAllMembers(groupId);
        // 성공 시 200 OK (Body 없음)
        return ResponseEntity.ok().build();
    }

    // 간단한 엔티티 -> DTO 변환 메서드
    private SubscriptionGroupDto toDto(SubscriptionGroup group) {
        return new SubscriptionGroupDto(
                group.getId(),
                group.getTitle(),
                group.getMaxMembers(),
                group.getDurationDays(),
                group.getStartDate(),
                group.getEndDate(),
                group.isActive(),
                (group.getOwner() != null) ? group.getOwner().getId() : null
        );
    }
}