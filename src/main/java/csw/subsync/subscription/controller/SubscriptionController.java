package csw.subsync.subscription.controller;

import csw.subsync.common.annotation.ApiV1;
import csw.subsync.subscription.doc.SubscriptionControllerDoc;
import csw.subsync.subscription.dto.SubscriptionCreateRequest;
import csw.subsync.subscription.dto.SubscriptionGroupDto;
import csw.subsync.subscription.dto.SubscriptionJoinRequest;
import csw.subsync.subscription.model.PredefinedSubscription;
import csw.subsync.subscription.model.SubscriptionGroup;
import csw.subsync.subscription.service.PredefinedSubscriptionService;
import csw.subsync.subscription.service.SubscriptionService;
import csw.subsync.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@ApiV1
@RequiredArgsConstructor
@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController implements SubscriptionControllerDoc {

    private final SubscriptionService subscriptionService;
//    private final UserRepository userRepository;
    private final PredefinedSubscriptionService predefinedSubscriptionService;

    @Override
    public ResponseEntity<SubscriptionGroupDto> create(SubscriptionCreateRequest request) {
        User owner = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Integer priceAmount = request.getPriceAmount();
        String priceCurrency = request.getPriceCurrency();

        // Check if predefinedSubscriptionId is provided
        if (request.getPredefinedSubscriptionId() != null) {
            PredefinedSubscription predefinedSubscription = predefinedSubscriptionService.getPredefinedSubscriptionById(request.getPredefinedSubscriptionId());
            priceAmount = predefinedSubscription.getPriceAmount();
            priceCurrency = predefinedSubscription.getPriceCurrency();
        } else {
            // Validate priceAmount and priceCurrency if predefinedSubscriptionId is not provided
            if (priceAmount == null || priceCurrency == null || priceCurrency.isEmpty()) {
                return ResponseEntity.badRequest().build(); // TODO: throw a custom exception
            }
        }

        var group = subscriptionService.createGroup(
                owner,
                request.getTitle(),
                request.getMaxMembers(),
                request.getDurationDays(),
                request.getPricingModel(),
                priceAmount, // Use determined priceAmount
                priceCurrency // Use determined priceCurrency
        );

        // 201 Created와 함께 DTO 반환
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDto(group));
    }

    @Override
    public ResponseEntity<SubscriptionGroupDto> join(SubscriptionJoinRequest request) {
        // 사용자 확인
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        var group = subscriptionService.joinGroup(request.getGroupId(), user);

        // 200 OK
        return ResponseEntity.ok(toDto(group));
    }

    @Override
    public ResponseEntity<Void> remove(Long groupId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 삭제 실행
        subscriptionService.removeGroup(groupId);

        // 성공 시 204 No Content
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> charge(Long groupId) {
        subscriptionService.chargeAllMembers(groupId);
        // 성공 시 200 OK (Body 없음)
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<PredefinedSubscription>> getPredefinedSubscriptionList() {
        List<PredefinedSubscription> subscriptions = predefinedSubscriptionService.getAllPredefinedSubscriptions();
        return ResponseEntity.ok(subscriptions);
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