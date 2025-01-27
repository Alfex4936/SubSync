package csw.subsync.user.service;

import csw.subsync.common.config.security.JwtService;
import csw.subsync.common.exception.ResourceNotFoundException;
import csw.subsync.user.dto.UserProfileResponse;
import csw.subsync.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
//    @Cacheable(value = "userProfile", key = "#userId")
    public UserProfileResponse getMyProfile() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .map(UserProfileResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

}
