package ir.ceit.resa.services;


import ir.ceit.resa.model.User;
import ir.ceit.resa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User loadUserByUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                return user.get();
            }
        }
        return null;
    }

    public User getLoggedInUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String loggedInUser = ((UserDetails) principal).getUsername();
            return loadUserByUsername(loggedInUser);
        }
        return null;
    }
}
