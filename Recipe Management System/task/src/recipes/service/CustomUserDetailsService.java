package recipes.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recipes.entity.Chef;
import recipes.repository.ChefRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private ChefRepository chefRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Chef chef = chefRepository.findByEmail(username);
        if (chef == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return chef;
    }
}