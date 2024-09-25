package recipes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import recipes.entity.Chef;

public interface ChefRepository extends JpaRepository<Chef, Long> {
    Chef findByEmail(String email);
}