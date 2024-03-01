package com.sarabrandserver.user.service;

import com.sarabrandserver.user.entity.SarreBrandUser;
import com.sarabrandserver.user.repository.UserRepository;
import com.sarabrandserver.user.res.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SarreBrandUserService {

    private final UserRepository userRepository;


    public Optional<SarreBrandUser> userByPrincipal(String principal) {
        return userRepository.findByPrincipal(principal);
    }

    /**
     * Returns a {@link Page} of {@link SarreBrandUser}.
     *
     * @param page number in the UI
     * @param size max amount of json pulled at one
     * @return {@link Page} of {@link UserResponse}.
     * */
    public Page<UserResponse> allUsers(int page, int size) {
        return userRepository
                .allUsers(PageRequest.of(page, size))
                .map(s -> new UserResponse(s.getFirstname(), s.getLastname(), s.getEmail(), s.getPhoneNumber()));
    }

}