package repositories;

import models.*;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import repositories.repositoryInterfaces.IRecipeRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by Kaempe on 20-03-2017.
 */
public class RecipeRepository implements IRecipeRepository {

    private Sql2o sql2o;

    public RecipeRepository(Sql2o sql2o){
        this.sql2o = sql2o;
    }

    @Override
    public Collection<Recipe> getAll() {
        Collection<Recipe> recipes;
        String sql =
                "SELECT recipeId, recipeName, recipeDescription, recipeImageFilePath, published " +
                        "FROM Recipes";
        try{
            Connection con = sql2o.open();
            recipes = con.createQuery(sql)
                    .executeAndFetch(Recipe.class);
            recipes.forEach(recipe -> recipe.setRecipeType(this.getRecipeTypeFor(recipe.getRecipeId())));
            recipes.forEach(recipe -> recipe.setMeasuredIngredients(this.getMeasuredIngredientsFor(recipe.getRecipeId())));
        }catch (Exception e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return recipes;
    }

    @Override
    public Recipe get(int id) {
        if (!this.exists(id)){
            throw new IllegalArgumentException("No recipe found with id " + id);
        }
        Recipe recipe;
        String sql =
                "SELECT recipeId, recipeName, recipeDescription, recipeImageFilePath, published " +
                        "FROM Recipes WHERE recipeId = :id";
        try{
            Connection con = sql2o.open();
            recipe = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(Recipe.class);
            recipe.setRecipeType(this.getRecipeTypeFor(recipe.getRecipeId()));
            recipe.setMeasuredIngredients(this.getMeasuredIngredientsFor(recipe.getRecipeId()));
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return recipe;
    }

    @Override
    public boolean delete(int id) {
        if (!this.exists(id)){
            throw new IllegalArgumentException("No Recipe found with id: " + id);
        }
        failDeleteIfRelationsExist(id);
        Connection con;
        String sqlRelationsToDelete =
                "DELETE FROM MeasuredIngredients WHERE " +
                        "recipeId = :id";

        String sql =
                "DELETE FROM Recipes WHERE " +
                        "recipeId = :id ";
        try{
            con = sql2o.beginTransaction();
            con.createQuery(sqlRelationsToDelete)
                    .addParameter("id",id)
                    .executeUpdate();
            con.createQuery(sql)
                    .addParameter("id",id)
                    .executeUpdate();
            con.commit();
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean exists(int id) {
        Recipe recipe;

        String sql = "SELECT recipeId " +
                "FROM Recipes " +
                "WHERE recipeId = :id";
        try{
            Connection con = sql2o.open();
            recipe = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(Recipe.class);
            if (recipe != null) return true;
            return false;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isPublished(int id) {
        Integer result;
        String sql = "SELECT recipeId " +
                "FROM Recipes " +
                "WHERE published = 1 " +
                "AND recipeId = :id";
        try{
            Connection con = sql2o.open();
            result = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(Integer.class);
            if (result != 0) return true;
            return false;
        }catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public boolean publish(int id) {
        Date date = new Date();
        String sql =
                "UPDATE Recipes SET " +
                        "published = 1, " +
                        "createdDate = :date " +
                        "WHERE recipeId = :recipeId";
        try{
            Connection con = sql2o.open();
            con.createQuery(sql)
                    .addParameter("date",date.getTime())
                    .addParameter("recipeId",id)
                    .executeUpdate();
            return true;
        }catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public RecipeType getRecipeTypeFor(int id){
        RecipeType recipeType;
        String sql =
                "SELECT recipeTypeId, recipeTypeName, published " +
                    "FROM RecipeTypes " +
                    "WHERE recipeTypeId IN (" +
                        "SELECT recipeTypeId FROM Recipes " +
                        "WHERE recipeId = :id" +
                    ")";
        try{
            Connection con = sql2o.open();
            recipeType = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(RecipeType.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return recipeType;
    }

    @Override
    public Collection<MeasuredIngredient> getMeasuredIngredientsFor(int id){
        Collection<MeasuredIngredient> ingredients;
        String sql =
                "SELECT measuredIngredientId, amount, measure FROM MeasuredIngredients " +
                        "WHERE recipeId = :id";
        try{
            Connection con = sql2o.open();
            ingredients = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetch(MeasuredIngredient.class);
            ingredients.forEach(ingredient -> ingredient.setIngredient(getIngredientFor(id)));
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return ingredients;
    }

    @Override
    public Ingredient getIngredientFor(int id){
        Ingredient ingredient;
        String sql =
                "SELECT ingredientId, ingredientName, ingredientDescription, published " +
                    "FROM Ingredients " +
                    "WHERE ingredientId IN (" +
                        "SELECT ingredientId FROM MeasuredIngredients " +
                        "WHERE recipeId= :id" +
                    ")";
        try{
            Connection con = sql2o.open();
            ingredient = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(Ingredient.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return ingredient;
    }

    @Override
    public Collection<Allergy> getAllergiesFor(int id){
        Collection<Allergy> allergies;
        String sql =
                "SELECT allergyId, allergyName, allergyDescription, published " +
                    "FROM Allergies " +
                    "WHERE allergyId in (" +
                        "SELECT allergyId from RecipeAllergies WHERE " +
                        "recipeId = :id" +
                    ")";
        try{
            Connection con = sql2o.open();
            allergies = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetch(Allergy.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return allergies;
    }

    @Override
    public void failDeleteIfRelationsExist(int id){
        Collection<Integer> relations;
        String sql = "SELECT recipeId " +
                "FROM MenuRecipes " +
                "WHERE recipeId = :id";
        try{
            Connection con = sql2o.open();
            relations = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetch(Integer.class);
        }catch (Exception e)
        {
            throw new IllegalArgumentException("Recipe not deleted. Problems with menu associations");
        }
        if (!relations.isEmpty()) throw new IllegalArgumentException("Recipe not deleted. Used in one or more Menus");
    }

}
