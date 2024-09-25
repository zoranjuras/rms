package recipes.service;

import recipes.entity.Chef;
import recipes.entity.Recipe;

import java.util.List;
import java.util.Optional;

public interface RecipeService {

    Optional<Recipe> findById(Long recipeId);

    List<Recipe> findByCategoryIgnoreCaseOrderByDateDesc(String category);

    List<Recipe> findByNameContainingIgnoreCaseOrderByDateDesc(String name);

    Recipe saveRecipe(Recipe theRecipe);

    Chef saveChef(Chef theChef);

    public Optional<Chef> findByEmail(String email);

    boolean deleteRecipeById(Long recipeId);

    public List<Recipe> findAll();
}
