package repositories;

import models.RecipeType;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import repositories.repositoryInterfaces.IRecipeTypeRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Created by Kaempe on 17-03-2017.
 */
public class RecipeTypeRepository implements IRecipeTypeRepository{

    private Sql2o sql2o;

    public RecipeTypeRepository(Sql2o sql2o)
    {
        this.sql2o = sql2o;
    }

    @Override
    public Collection<RecipeType> getAll() {
        Collection<RecipeType> recipeTypes;
        String sql =
                "SELECT recipeTypeId, recipeTypeName, published " +
                    "FROM RecipeTypes ";
        try{
            Connection con = sql2o.open();
            recipeTypes = con.createQuery(sql)
                    .executeAndFetch(RecipeType.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return recipeTypes;
    }

    @Override
    public RecipeType get(int id) {
        if (!this.exists(id)){
            throw new IllegalArgumentException("No recipeType found with id " + id);
        }

        RecipeType recipeType;
        String sql =
                "SELECT recipeTypeId, recipeTypeName, published " +
                    "FROM RecipeTypes " +
                    "WHERE recipeTypeId = :id";
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
    public int create(RecipeType model) {
        int id;
        failIfInvalid(model);
        Date date = new Date();
        String sql =
                "INSERT INTO RecipeTypes (recipeTypeName, published, createdDate) " +
                        "VALUES (:recipeTypeName, :published, :date)";
        try{
            Connection con = sql2o.open();
            id = Integer.parseInt(con.createQuery(sql, true)
                    .addParameter("date",date.getTime())
                    .bind(model)
                    .executeUpdate().getKey().toString());
        }catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
        return id;
    }

    @Override
    public boolean update(RecipeType model) {
        if (!this.exists(model.getRecipeTypeId())){
            throw new IllegalArgumentException("No RecipeType found with id " + model.getRecipeTypeId());
        }
        failIfInvalid(model);
        String sql =
                "UPDATE RecipeTypes SET " +
                        "recipeTypeName = :recipeTypeName " +
                        "WHERE recipeTypeId = :recipeTypeId";
        try{
            Connection con = sql2o.open();
            con.createQuery(sql)
                    .bind(model)
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
            throw new IllegalArgumentException("No recipeType found with id " + id);
        }
        failDeleteIfRelationsExist(id);

        String sql =
                "DELETE FROM RecipeTypes " +
                        "WHERE recipeTypeId = :id";
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
    public boolean exists(int id) {
        RecipeType recipeType;

        String sql = "SELECT recipeTypeId " +
                "FROM RecipeTypes " +
                "WHERE recipeTypeId = :id";
        try{
            Connection con = sql2o.open();
            recipeType = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(RecipeType.class);
            if (recipeType != null) return true;
            return false;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void failIfInvalid(RecipeType recipeType)
    {
        if (recipeType == null)
        {
            throw new IllegalArgumentException("recipeType cannot be null");
        }
        if (recipeType.getRecipeTypeName() == null || recipeType.getRecipeTypeName().length() < 1) {
            throw new IllegalArgumentException("Parameter `name` cannot be empty");
        }
    }

    @Override
    public boolean isPublished(int id) {
        Integer result;
        String sql = "SELECT recipeTypeId " +
                "FROM RecipeTypes " +
                "WHERE published = 1 " +
                "AND recipeTypeId = :id";
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
                "UPDATE RecipeTypes SET " +
                        "published = 1, " +
                        "createdDate = :date " +
                        "WHERE recipeTypeId = :recipeTypeId";
        try{
            Connection con = sql2o.open();
            con.createQuery(sql)
                    .addParameter("date",date.getTime())
                    .addParameter("recipeTypeId",id)
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
        String sql = "SELECT recipeTypeId " +
                "FROM Recipes " +
                "WHERE recipeTypeId = :id";
        try{
            Connection con = sql2o.open();
            relations = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetch(Integer.class);
        }catch (Exception e)
        {
            throw new IllegalArgumentException("RecipeType not deleted. Problems with Recipe associations");
        }
        if (!relations.isEmpty()) throw new IllegalArgumentException("RecipeType not deleted. Used in one or more Recipes");
    }
}
