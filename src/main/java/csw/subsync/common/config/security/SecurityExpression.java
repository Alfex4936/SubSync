package csw.subsync.common.config.security;

import csw.subsync.subscription.model.SubscriptionGroup;
import csw.subsync.subscription.repository.SubscriptionGroupRepository;
import csw.subsync.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("securityExpression")
public class SecurityExpression {
    private final SubscriptionGroupRepository subscriptionGroupRepo;

    /**
     * Check if the current user can remove a given group.
     */
    public boolean canRemoveGroup(Authentication authentication, Long groupId) {
        if (authentication == null || groupId == null) {
            return false;
        }

        User principalUser = (User) authentication.getPrincipal();

        // If user is Admin
        if ("ADMIN".equals(principalUser.getRole().name())) {
            return true;
        }

        // Or if user is the group owner
        SubscriptionGroup group = subscriptionGroupRepo.findByIdAndActiveTrueWithMemberships(groupId);
        return group != null && group.getOwner().getId().equals(principalUser.getId());
    }

    /**
     * Check if the current user is an admin or manager.
     */
    public boolean isAdminOrManager(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        User principalUser = (User) authentication.getPrincipal();
        String roleName = principalUser.getRole().name();
        return "ADMIN".equals(roleName) || "MANAGER".equals(roleName);
    }

}
