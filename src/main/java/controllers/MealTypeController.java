package controllers;

import models.MealType;
import models.wrapper_models.MealTypes;
import org.sql2o.Sql2o;
import repositories.MealTypeRepository;
import repositories.repositoryInterfaces.IMealTypeRepository;

import java.util.Collection;

import static jsonUtil.JsonUtil.fromJson;
import static jsonUtil.JsonUtil.json;
import static jsonUtil.JsonUtil.toJson;
import static spark.Spark.*;
import static spark.Spark.after;
import static spark.Spark.exception;

/**
 * Created by Kaempe on 19-03-2017.
 */
public class MealTypeController
{
    private IMealTypeRepository mealTypeRepository;

    public MealTypeController(Sql2o sql2o)
    {
        this.mealTypeRepository = new MealTypeRepository(sql2o);

        get("/mealTypes", (req, res) ->
        {
            Collection<MealType> mealTypes = mealTypeRepository.getAll();
            if (mealTypes.size() != 0){
                res.status(200);
                return  new MealTypes(mealTypes);
            }
            res.status(204);
            return new String("No mealTypes found in the database");
        }, json());

        get("/mealTypes/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            MealType mealType = mealTypeRepository.get(id);

            if (mealType!= null) {
                res.status(200);
                return mealType;
            }
            res.status(204);
            return new String("No mealType with id "+ id +" found");
        }, json());

        post("/mealTypes", (req, res) -> {
            MealType mealType = fromJson(req.body(),MealType.class);

            int id = mealTypeRepository.create(mealType);

            if (id != 0)
            {
                // TODO ADD MESSAGE
                res.status(200);
                return id;
            }
            res.status(400);
            return new String("mealType not created");
        }, json());

        put("/mealTypes/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            MealType mealType = fromJson(req.body(),MealType.class);
            mealType.setMealTypeId(id);
            boolean result = mealTypeRepository.update(mealType);

            if (result)
            {
                res.status(200);
                return new String("mealType " + id + " Updated");
            }
            res.status(400);
            return new String("mealtype not updated");
        }, json());

        put("/mealTypes/accept/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            if (!mealTypeRepository.exists(id))
            {
                return new String("no mealTypes with id " + id + " found");
            }
            if (mealTypeRepository.isPublished(id))
            {
                return new String("mealType with id " + id + " already published");
            }

            boolean result = mealTypeRepository.publish(id);

            if (result)
            {
                // TODO ADD MESSAGE
                res.status(200);
                return new String("mealTypes " + id + " published");
            }

            res.status(400);
            return new String("mealTypes not published");
        }, json());

        delete("/mealTypes/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            boolean result = mealTypeRepository.delete(id);
            if (result)
            {
                res.status(200);
                return result;
            }
            res.status(500);
            return new String("Could not delete mealType with id " + id);
        },json());
    }
}
