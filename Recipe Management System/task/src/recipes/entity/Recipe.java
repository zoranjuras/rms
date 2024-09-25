package recipes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long recipeId;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Category is mandatory")
    private String category;

    @UpdateTimestamp
    private LocalDateTime date;

    @NotBlank(message = "Description is mandatory")
    private String description;

    @ElementCollection
    @Size(min = 1)
    private List<String> ingredients = new ArrayList<>();

    @ElementCollection
    @Size(min = 1)
    private List<String> directions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Chef chef = null;

    public Recipe(String name, String category, String description,
                  List<String> ingredients, List<String> directions) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.ingredients = ingredients;
        this.directions = directions;
    }
}
