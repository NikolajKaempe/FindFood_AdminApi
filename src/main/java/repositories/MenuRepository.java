package repositories;

import models.*;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import repositories.repositoryInterfaces.IMenuRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Created by Kaempe on 28-03-2017.
 */
public class MenuRepository implements IMenuRepository{

    private Sql2o sql2o;

    public MenuRepository(Sql2o sql2o){
        this.sql2o = sql2o;
    }

    @Override
    public Collection<Menu> getAll() {
        Collection<Menu> menus;
        String sql =
                "SELECT menuId, menuName, menuDescription, menuImageFilePath, published " +
                        "FROM Menus";
        try{
            Connection con = sql2o.open();
            menus = con.createQuery(sql)
                    .executeAndFetch(Menu.class);
            menus.forEach(menu -> menu.setMealType(this.getMealTypeFor(menu.getMenuId())));
            menus.forEach(menu -> menu.setRecipes(this.getRecipesFor(menu.getMenuId())));
        }catch (Exception e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return menus;
    }

    @Override
    public Menu get(int id) {
        if (!this.exists(id)){
            throw new IllegalArgumentException("No menu found with id " + id);
        }
        Menu menu;
        String sql =
                "SELECT menuId, menuName, menuDescription, menuImageFilePath, published " +
                        "FROM Menus WHERE menuId = :id";
        try{
            Connection con = sql2o.open();
            menu = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(Menu.class);
            menu.setMealType(this.getMealTypeFor(menu.getMenuId()));
            menu.setRecipes(this.getRecipesFor(menu.getMenuId()));
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return menu;
    }

    @Override
    public boolean delete(int id) {
        return false;
    }

    @Override
    public boolean exists(int id) {
        Menu menu;

        String sql = "SELECT menuId " +
                "FROM Menus " +
                "WHERE menuId = :id";
        try{
            Connection con = sql2o.open();
            menu = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(Menu.class);
            if (menu != null) return true;
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
        String sql = "SELECT menuId " +
                "FROM Menus " +
                "WHERE published = 1 " +
                "AND menuId = :id";
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
                "UPDATE Menus SET " +
                        "published = 1, " +
                        "createdDate = :date " +
                        "WHERE menuId = :menuId";
        try{
            Connection con = sql2o.open();
            con.createQuery(sql)
                    .addParameter("date",date.getTime())
                    .addParameter("menuId",id)
                    .executeUpdate();
            return true;
        }catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public MealType getMealTypeFor(int id){
        MealType mealType;
        String sql =
                "SELECT mealTypeId, mealTypeName, published " +
                    "FROM MealTypes " +
                        "WHERE mealTypeId IN (" +
                        "SELECT mealTypeId FROM Menus " +
                        "WHERE menuId = :id" +
                        ")";
        try{
            Connection con = sql2o.open();
            mealType = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(MealType.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return mealType;
    }

    @Override
    public Collection<Recipe> getRecipesFor(int id){
        Collection<Recipe> recipes;
        String sql =
                "SELECT recipeId, recipeName, recipeDescription, recipeImageFilePath, published FROM Recipes " +
                        "WHERE recipeId IN (" +
                            "SELECT recipeId FROM MenuRecipes " +
                            "WHERE menuId = :id" +
                        ")";
        try{
            Connection con = sql2o.open();
            recipes = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetch(Recipe.class);
            recipes.forEach(recipe -> recipe.setRecipeType(getRecipeType(recipe.getRecipeId())));
        }catch (Exception e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return recipes;
    }

    @Override
    public Collection<Ingredient> getIngredientFor(int id) {
        Collection<Ingredient> ingredients;

        String sql =
                "SELECT ingredientId, ingredientName, ingredientDescription, published " +
                    "FROM Ingredients " +
                    "WHERE ingredientId IN (" +
                        "SELECT ingredientId FROM MeasuredIngredients " +
                        "WHERE recipeId IN (" +
                            "SELECT recipeId FROM MenuRecipes " +
                            "WHERE menuId = :id))";

        try{
            Connection con = sql2o.open();
            ingredients = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetch(Ingredient.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return ingredients;
    }

    @Override
    public Collection<Allergy> getAllergiesFor(int id) {
        Collection<Allergy> allergies;

        String sql =
                "SELECT ingredientId FROM MeasuredIngredients " +
                        "WHERE recipeId IN (" +
                        "SELECT recipeId FROM MenuRecipes " +
                        "WHERE menuId = :id)";

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

    private RecipeType getRecipeType(int recipeId){
        RecipeType recipeType;
        String sql =
                "SELECT recipeTypeId, recipeTypeName, published " +
                    "FROM RecipeTypes " +
                        "WHERE recipeTypeId IN (" +
                        "SELECT recipeTypeId FROM Recipes " +
                        "WHERE recipeTypeId = :id" +
                        ")";
        try{
            Connection con = sql2o.open();
            recipeType = con.createQuery(sql)
                    .addParameter("id",recipeId)
                    .executeAndFetchFirst(RecipeType.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return recipeType;
    }

}
