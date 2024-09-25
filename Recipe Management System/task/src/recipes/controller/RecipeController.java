package recipes.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import recipes.entity.Chef;
import recipes.entity.Recipe;
import recipes.exception.RecipeNotFoundException;
import recipes.security.SecurityUtils;
import recipes.service.RecipeService;
import recipes.service.RecipeServiceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@Validated
@RequestMapping("/api")
public class RecipeController {

@Autowired
private RecipeService recipeService;

private Chef currentChef;

private static final Logger LOGGER = Logger.getLogger(RecipeController.class.getName());


    @Autowired
    public RecipeController(RecipeServiceImpl theRecipeService) {
        recipeService = theRecipeService;
        currentChef = null;
    }

    @GetMapping("/recipe/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable("id") Long recipeId) {
        Optional<Recipe> theRecipe = recipeService.findById(recipeId);
        if (theRecipe.isPresent()) {
            return new ResponseEntity<>(theRecipe.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/recipe/")
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        try {
            List<Recipe> recipes = recipeService.findAll();

            if (recipes == null || recipes.isEmpty()) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
            }

            return new ResponseEntity<>(recipes, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving recipes", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/recipe/category/{category}")
    public List<Recipe> getRecipesByCategory(@PathVariable String category) {
        return recipeService.findByCategoryIgnoreCaseOrderByDateDesc(category);
    }

    @GetMapping("/recipe/name/{name}")
    public List<Recipe> getRecipesByName(@PathVariable String name) {
        return recipeService.findByNameContainingIgnoreCaseOrderByDateDesc(name);
    }

    @GetMapping("/recipe/search/")
    public List<Recipe> searchRecipes(@RequestParam(required = false) String category,
                                      @RequestParam(required = false) String name) {
        if (category != null && name != null) {
            throw new IllegalArgumentException("Only one of 'category' or 'name' should be provided.");
        } else if (category != null) {
            return recipeService.findByCategoryIgnoreCaseOrderByDateDesc(category);
        } else if (name != null) {
            return recipeService.findByNameContainingIgnoreCaseOrderByDateDesc(name);
        } else {
            throw new IllegalArgumentException("Either 'category' or 'name' parameter must be provided.");
        }
    }

    @DeleteMapping("/recipe/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable("id") Long recipeId) {
        try {
            Optional<Recipe> theRecipe = recipeService.findById(recipeId);
            if (theRecipe.isPresent()) {
                String currentUserEmail = SecurityUtils.getCurrentUserEmail();
                if (currentUserEmail.equals(theRecipe.get().getChef().getEmail())) {
                    boolean isDeleted = recipeService.deleteRecipeById(recipeId);
                    if (isDeleted) {
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    } else {
                        throw new RecipeNotFoundException("Recipe not found with id - " + recipeId);
                    }
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting recipe", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/recipe/{id}")
    public ResponseEntity<Object> modifyRecipe(@PathVariable Long id, @Valid @RequestBody Recipe theRecipe) {
        try {
            Optional<Recipe> existingRecipe = recipeService.findById(id);
            if (existingRecipe.isPresent()) {
                String currentUserEmail = SecurityUtils.getCurrentUserEmail();
                if (currentUserEmail.equals(existingRecipe.get().getChef().getEmail())) {
                    theRecipe.setRecipeId(id);
                    theRecipe.setChef(existingRecipe.get().getChef()); // Zadrži istog kuhara
                    Recipe savedRecipe = recipeService.saveRecipe(theRecipe);
                    return new ResponseEntity<>(new RecipeIdResponse(savedRecipe.getRecipeId()), HttpStatus.NO_CONTENT);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error modifying recipe", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/recipe/new")
    public ResponseEntity<Object> saveRecipe(@Valid @RequestBody Recipe theRecipe) {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        Optional<Chef> optionalChef = recipeService.findByEmail(currentUserEmail);
        if (optionalChef.isPresent()) {
            currentChef = optionalChef.get();
        } else {
            return new ResponseEntity<>("Chef not found", HttpStatus.NOT_FOUND);
        }

        try {
            theRecipe.setChef(currentChef);
            theRecipe.setDate(LocalDateTime.now());
            Recipe savedRecipe = recipeService.saveRecipe(theRecipe);
            return new ResponseEntity<>(new RecipeIdResponse(savedRecipe.getRecipeId()), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving recipe", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerChef(@Valid @RequestBody Chef theChef) {

        Optional<Chef> existingChef = recipeService.findByEmail(theChef.getEmail());

        if (existingChef.isPresent() && existingChef.get().getEmail().equals(theChef.getEmail())) {
            login(theChef);
            return new ResponseEntity<>("Chef with this email already exists", HttpStatus.BAD_REQUEST);
        }

        PasswordEncoder bCryptEncoder = new BCryptPasswordEncoder(7);
        theChef.setPassword(bCryptEncoder.encode(theChef.getPassword()));

        try {
            currentChef = recipeService.saveChef(theChef);
            login(theChef);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving chef", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> login(@RequestBody Chef theChef) {
        // Authenticate the user
        Authentication authentication = new UsernamePasswordAuthenticationToken(theChef.getEmail(), theChef.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Verify the current authenticated user
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Current authenticated user: " + currentUserEmail);

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = "Validation error message here"; // Just in case
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleException(RecipeNotFoundException exc) {
        return new ResponseEntity<>(exc.getMessage(), HttpStatus.NOT_FOUND);
    }

    @Getter
    static class RecipeIdResponse {
        private Long id;

        public RecipeIdResponse(Long id) {
            this.id = id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    @Getter
    static class ChefIdResponse {
        private String password;

        public ChefIdResponse(@NotBlank(message = "Password is mandatory") @Size(min = 8, message = "Password must be at least 8 characters") String password) {
            this.password = password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
