package controllers;

import models.Menu;
import models.Recipe;
import models.wrapper_models.Menus;
import models.wrapper_models.Recipes;
import org.sql2o.Sql2o;
import repositories.MenuRepository;
import repositories.repositoryInterfaces.IMenuRepository;

import java.util.Collection;
import java.util.Date;

import static jsonUtil.JsonUtil.json;
import static jsonUtil.JsonUtil.toJson;
import static spark.Spark.*;

/**
 * Created by Kaempe on 29-03-2017.
 */
public class MenuController
{
    private IMenuRepository menuRepository;

    public MenuController(Sql2o sql2o){

        menuRepository = new MenuRepository(sql2o);

        get("/menus", (req, res) ->
        {
            Collection<Menu> menus = menuRepository.getAll();
            if (menus.size() != 0){
                res.status(200);
                return new Menus(menus);
            }
            res.status(204);
            return new String("No recipeTypes found in the database");
        }, json());

        get("/menus/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            Menu menu = menuRepository.get(id);

            if (menu != null) {
                res.status(200);
                return menu;
            }
            res.status(204);
            return new String("No menu with id "+ id +" found");
        }, json());

        get("/menus/:id/recipes", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            Collection<Recipe> recipes = menuRepository.getRecipesFor(id);

            if (recipes != null) {
                res.status(200);
                return new Recipes(recipes);
            }
            res.status(204);
            return new String("No recipes found for menu with id "+ id);
        }, json());

        put("/menus/accept/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            if (!menuRepository.exists(id))
            {
                return new String("no menu with id " + id + " found");
            }
            if (menuRepository.isPublished(id))
            {
                return new String("menu with id " + id + " already published");
            }

            boolean result = menuRepository.publish(id);

            if (result)
            {
                res.status(200);
                return new String("menu " + id + " published");
            }

            res.status(400);
            return new String("menu not published");
        }, json());
    }
}
