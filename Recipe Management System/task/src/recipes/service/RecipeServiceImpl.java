package recipes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import recipes.entity.Chef;
import recipes.repository.ChefRepository;
import recipes.repository.RecipeRepository;
import recipes.entity.Recipe;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeServiceImpl implements RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private ChefRepository chefRepository;

    public RecipeServiceImpl(ChefRepository chefRepository) {
        this.chefRepository = chefRepository;
    }

    @Override
    public Optional<Recipe> findById(Long id) {
        return recipeRepository.findById(id);
    }

    @Override
    public List<Recipe> findByCategoryIgnoreCaseOrderByDateDesc(String category) {
        return recipeRepository.findByCategoryIgnoreCaseOrderByDateDesc(category);
    }

    @Override
    public List<Recipe> findByNameContainingIgnoreCaseOrderByDateDesc(String name) {
        return recipeRepository.findByNameContainingIgnoreCaseOrderByDateDesc(name);
    }

    @Override
    public Recipe saveRecipe(Recipe theRecipe) {
        return recipeRepository.save(theRecipe);
    }

    @Override
    @Transactional
    public Chef saveChef(Chef theChef) {
        return chefRepository.save(theChef);
    }

    @Override
    public Optional<Chef> findByEmail(String email) {
        return Optional.ofNullable(chefRepository.findByEmail(email));
    }

    @Override
    public boolean deleteRecipeById(Long id) {
        if (recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }
}