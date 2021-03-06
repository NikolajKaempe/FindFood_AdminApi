package controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;
import firebaseFiles.FireBaseDatabase;
import firebaseFiles.User;
import models.Allergy;
import models.wrapper_models.Allergies;
import org.sql2o.Sql2o;
import repositories.AllergyRepository;
import repositories.repositoryInterfaces.IAllergyRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static jsonUtil.JsonUtil.*;
import static spark.Spark.*;

/**
 * Created by Kaempe on 27-02-2017.
 */
public class AllergyController
{
    private IAllergyRepository allergyRepository;

    public AllergyController(Sql2o sql2o)
    {
        allergyRepository = new AllergyRepository(sql2o);

        get("/allergies", (req, res) ->
        {
            Collection<Allergy> allergies = allergyRepository.getAll();
            if (allergies.size() != 0){
                res.status(200);
                return new Allergies(allergies);
            }
            res.status(204);
            return new String("No allergies found in the database");
        }, json());

        get("/allergies/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            Allergy allergy = allergyRepository.get(id);

            if (allergy != null) {
                res.status(200);
                return allergy;
            }
            res.status(204);
            return new String("No allergy with id "+ id +" found");
        }, json());

        post("/allergies", (req, res) -> {
            Allergy allergy = fromJson(req.body(),Allergy.class);

            int id = allergyRepository.create(allergy);

            if (id != 0)
            {
                res.status(200);
                // TODO ADD MESSAGE
                return id;
            }
            res.status(400);
            return new String("Allergy not created");
        }, json());

        put("/allergies/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            Allergy allergy = fromJson(req.body(),Allergy.class);
            allergy.setAllergyId(id);
            boolean result = allergyRepository.update(allergy);

            if (result)
            {
                res.status(200);
                return new String("allergy " + id + " Updated");
            }
            res.status(400);
            return new String("allergy not updated");
        }, json());

        put("/allergies/accept/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            if (!allergyRepository.exists(id))
            {
                return new String("no allergy with id " + id + " found");
            }
            if (allergyRepository.isPublished(id))
            {
                return new String("allergy with id " + id + " already published");
            }

            boolean result = allergyRepository.publish(id);

            if (result)
            {
                res.status(200);
                return new String("allergy " + id + " published");
            }

            res.status(400);
            return new String("allergy not published");
        }, json());

        delete("/allergies/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            boolean result = allergyRepository.delete(id);
            if (result)
            {
                res.status(200);
                return result;
            }
            res.status(500);
            return new String("Could not delete allergy with id " + id);
        },json());


        before((req,res) -> {
            String authToken = null;
            try{
                authToken = req.headers("Authorization");
                if (authToken == null || authToken == "")
                {
                    throw new IllegalArgumentException("Wrong authentication");
                }
            }catch (Exception e){
                throw new IllegalArgumentException("Wrong authentication");
            }

            try
            {
                HashMap<String,Boolean> response = new HashMap<String, Boolean>();
                response.put("validResponse",false);
                FirebaseAuth.getInstance().verifyIdToken(authToken)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseToken>() {
                            @Override
                            public void onSuccess(FirebaseToken decodedToken) {
                                String uid = decodedToken.getUid();
                                req.attribute("adminId",uid);

                                User user = FireBaseDatabase.getUserInfo(uid);

                                req.attribute("role",user.getRole());
                                response.put("validResponse",true);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e)  {
                                req.attribute("role","admin");
                                req.attribute("adminId","admin");
                                System.out.println("Fake admin logged in");
                                response.put("validResponse",true);
                            }
                        });
                long timer = System.currentTimeMillis();
                while (!response.get("validResponse")){
                    if (System.currentTimeMillis() - timer >= 3000){
                        throw new TimeoutException("Login service timed out");
                    }
                }
                if (req.attribute("role") == null || !(req.attribute("role").equals("admin"))){
                    throw new IllegalArgumentException("Admin access denied");
                }
                if (req.attribute("adminId") == null){
                    throw new IllegalArgumentException("Could'nt retrieve adminId");
                }
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException(e.getMessage());
            }
        });

        after((req, res) -> {
            res.type("application/json");
            res.header("responseServer","Server: 1");
        });

        exception(IllegalArgumentException.class, (e, req, res) -> {
            res.status(400);
            res.body(toJson(e.getMessage()));
            res.type("application/json");
        });
    }
}
