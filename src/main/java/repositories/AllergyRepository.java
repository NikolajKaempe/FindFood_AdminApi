package repositories;

import models.Allergy;
import models.Ingredient;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import repositories.repositoryInterfaces.IAllergyRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class AllergyRepository implements IAllergyRepository
{
    private Sql2o sql2o;

    public AllergyRepository(Sql2o sql2o)
    {
        this.sql2o = sql2o;
    }

    @Override
    public Collection<Allergy> getAll() {
        Collection<Allergy> allergies;
        String sql =
                "SELECT allergyId, allergyName, allergyDescription, published " +
                        "FROM Allergies";
        try{
            Connection con = sql2o.open();
            allergies = con.createQuery(sql)
                    .executeAndFetch(Allergy.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return allergies;
    }

    @Override
    public Allergy get(int id) {
        Allergy allergy;
        String sql =
                "SELECT allergyId, allergyName, allergyDescription, published " +
                        "FROM Allergies " +
                        "WHERE allergyId = :id ";
        try{
            Connection con = sql2o.open();
            allergy = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(Allergy.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException("No Allergy with id " + id + " found");
        }
        return allergy;
    }

    @Override
    public int create(Allergy model) {
        int id;
        model.setPublished(true);
        this.failIfInvalid(model);
        Date date = new Date();
        String sql =
                "INSERT INTO Allergies (allergyName, allergyDescription, published, createdDate) " +
                        "VALUES (:allergyName, :allergyDescription, :published, :date)";
        try{
            Connection con = sql2o.open();
            id = Integer.parseInt(con.createQuery(sql, true)
                    .bind(model)
                    .addParameter("date",date.getTime())
                    .executeUpdate().getKey().toString());
        }catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
        return id;
    }

    @Override
    public boolean update(Allergy model) {
        if (!this.exists(model.getAllergyId())){
            throw new IllegalArgumentException("No allergy found with id: " + model.getAllergyId());
        }
        this.failIfInvalid(model);
        model.setPublished(true);
        Date date = new Date();
        String sql =
                "UPDATE Allergies SET " +
                        "allergyName = :allergyName, " +
                        "allergyDescription = :allergyDescription, " +
                        "createdDate = :date " +
                        "WHERE allergyId = :allergyId";
        try{
            Connection con = sql2o.open();
            con.createQuery(sql)
                    .bind(model)
                    .addParameter("date",date.getTime())
                    .executeUpdate();
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        if (!this.exists(id)){
            throw new IllegalArgumentException("No allergy found with id: " + id);
        }
        failDeleteIfRelationsExist(id);

        String sql =
                "DELETE FROM Allergies " +
                        "WHERE allergyId = :id";
        try{
            Connection con = sql2o.open();
            con.createQuery(sql)
                    .addParameter("id",id)
                    .executeUpdate();
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean exists(int id)
    {
        Allergy allergy;
        String sql =
                "SELECT allergyId FROM Allergies " +
                        "Where allergyId = :id";

        try{
            Connection con = sql2o.open();
            allergy = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(Allergy.class);
            if(allergy != null) return true;
            return false;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void failIfInvalid(Allergy model)
    {
        if (model == null)
        {
            throw new IllegalArgumentException("Allergy cannot be null");
        }
        if (model.getAllergyName() == null || model.getAllergyName().length() < 1) {
            throw new IllegalArgumentException("Parameter `name` cannot be empty");
        }
        if (model.getAllergyDescription() == null || model.getAllergyDescription().length() < 1) {
            throw new IllegalArgumentException("Parameter `description` cannot be empty");
        }
    }


    @Override
    public boolean isPublished(int id) {
        Integer result;
        String sql = "SELECT allergyId " +
                "FROM Allergies " +
                "WHERE published = 1 " +
                "AND allergyId = :id";
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
                "UPDATE Allergies SET " +
                        "published = 1, " +
                        "createdDate = :date " +
                        "WHERE allergyId = :allergyId";
        try{
            Connection con = sql2o.open();
            con.createQuery(sql)
                    .addParameter("date",date.getTime())
                    .addParameter("allergyId",id)
                    .executeUpdate();
            return true;
        }catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public void failDeleteIfRelationsExist(int id)
    {
        Collection<Integer> relations;
        String sql = "SELECT allergyId " +
                "FROM IngredientAllergies " +
                "WHERE allergyId = :id";
        try{
            Connection con = sql2o.open();
            relations = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetch(Integer.class);
        }catch (Exception e)
        {
            throw new IllegalArgumentException("Allergy not deleted. Problems with ingredients associations");
        }
        if (relations.size() < 1) throw new IllegalArgumentException("Allergy not deleted. Used in one or more ingredients");
    }
}
