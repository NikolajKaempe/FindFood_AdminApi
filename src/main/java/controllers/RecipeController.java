package controllers;

import models.Allergy;
import models.Recipe;
import models.wrapper_models.Allergies;
import models.wrapper_models.Recipes;
import org.sql2o.Sql2o;
import repositories.RecipeRepository;
import repositories.repositoryInterfaces.IRecipeRepository;

import java.util.Collection;

import static jsonUtil.JsonUtil.fromJson;
import static jsonUtil.JsonUtil.json;
import static jsonUtil.JsonUtil.toJson;
import static spark.Spark.*;

/**
 * Created by Kaempe on 22-03-2017.
 */
public class RecipeController
{
    private IRecipeRepository recipeRepository;

    public RecipeController(Sql2o sql2o)
    {
        recipeRepository = new RecipeRepository(sql2o);

        get("/recipes", (req, res) ->
        {
            Collection<Recipe> recipes = recipeRepository.getAll();
            if (recipes.size() != 0){
                res.status(200);
                return  new Recipes(recipes);
            }
            res.status(204);
            return new String("No recipes found in the database");
        }, json());

        get("/recipes/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            Recipe recipe = recipeRepository.get(id);

            if (recipe != null) {
                res.status(200);
                return recipe;
            }
            res.status(204);
            return new String("No recipe with id "+ id +" found");
        }, json());

        get("/recipes/:id/allergies", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }

            Collection<Allergy> allergies = recipeRepository.getAllergiesFor(id);

            if (allergies.size() > 0) {
                res.status(200);
                return new Allergies(allergies);
            }
            res.status(204);
            return new String("No allergies found for recipe with id "+ id);
        }, json());

        put("/recipes/accept/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            if (!recipeRepository.exists(id))
            {
                return new String("no ingredient with id " + id + " found");
            }
            if (recipeRepository.isPublished(id))
            {
                return new String("ingredient with id " + id + " already published");
            }

            boolean result = recipeRepository.publish(id);

            if (result)
            {
                res.status(200);
                return new String("recipe " + id + " published");
            }

            res.status(400);
            return new String("recipe not published");
        }, json());

    }
}
