package repositories;

import models.MealType;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import repositories.repositoryInterfaces.IMealTypeRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Created by Kaempe on 17-03-2017.
 */
public class MealTypeRepository implements IMealTypeRepository
{
    private Sql2o sql2o;

    public MealTypeRepository(Sql2o sql2o) { this.sql2o = sql2o; }

    @Override
    public Collection<MealType> getAll() {
        Collection<MealType> mealTypes;
        String sql =
                "SELECT mealTypeId, mealTypeName, published " +
                    "FROM MealTypes ";
        try{
            Connection con = sql2o.open();
            mealTypes = con.createQuery(sql)
                    .executeAndFetch(MealType.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return mealTypes;
    }

    @Override
    public MealType get(int id) {
        if (!this.exists(id)){
            throw new IllegalArgumentException("No mealType found with id " + id);
        }

        MealType mealType;
        String sql = "SELECT mealTypeId, mealTypeName, published " +
                "FROM MealTypes " +
                "WHERE mealTypeId = :id";
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
    public int create(MealType model) {
        int id;
        failIfInvalid(model);
        Date date = new Date();
        String sql =
                "INSERT INTO MealTypes (mealTypeName, published, createdDate) " +
                        "VALUES (:mealTypeName, :published, :date)";
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
    public boolean update(MealType model) {
        if (!this.exists(model.getMealTypeId())){
            throw new IllegalArgumentException("No MealType found with id " + model.getMealTypeId());
        }
        failIfInvalid(model);
        Date date = new Date();
        String sql =
                "UPDATE MealTypes SET " +
                        "mealTypeName = :mealTypeName, " +
                        "createdDate = :date" +
                        "WHERE mealTypeId = :mealTypeId";
        try{
            Connection con = sql2o.open();
            con.createQuery(sql)
                    .addParameter("date",date.getTime())
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
    public boolean delete(int id)
    {
        if (!this.exists(id)){
            throw new IllegalArgumentException("No mealType found with id " + id);
        }
        failDeleteIfRelationsExist(id);

        String sql =
                "DELETE FROM MealTypes " +
                        "WHERE mealTypeId = :id";
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
        MealType mealType;

        String sql = "SELECT mealTypeId " +
                "FROM MealTypes " +
                "WHERE mealTypeId = :id";
        try{
            Connection con = sql2o.open();
            mealType = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(MealType.class);
            if (mealType != null) return true;
            return false;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void failIfInvalid(MealType mealType)
    {
        if (mealType == null)
        {
            throw new IllegalArgumentException("mealType cannot be null");
        }
        if (mealType.getMealTypeName() == null || mealType.getMealTypeName().length() < 1) {
            throw new IllegalArgumentException("Parameter `name` cannot be empty");
        }
    }

    @Override
    public boolean isPublished(int id) {
        Integer result;
        String sql = "SELECT mealTypeId " +
                "FROM MealTypes " +
                "WHERE published = 1 " +
                "AND mealTypeId = :id";
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
                "UPDATE MealTypes SET " +
                        "published = 1, " +
                        "createdDate = :date " +
                        "WHERE mealTypeId = :mealTypeId";
        try{
            Connection con = sql2o.open();
            con.createQuery(sql)
                    .addParameter("date",date.getTime())
                    .addParameter("mealTypeId",id)
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
        String sql = "SELECT mealTypeId " +
                "FROM Menus " +
                "WHERE mealTypeId = :id";
        try{
            Connection con = sql2o.open();
            relations = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetch(Integer.class);
        }catch (Exception e)
        {
            throw new IllegalArgumentException("MealType not deleted. Problems with Menus associations");
        }
        if (!relations.isEmpty()) throw new IllegalArgumentException("MealType not deleted. Used in one or more Menus");
    }
}
