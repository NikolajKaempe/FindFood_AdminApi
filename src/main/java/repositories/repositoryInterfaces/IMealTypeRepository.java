package repositories.repositoryInterfaces;

import models.MealType;

/**
 * Created by Kaempe on 17-03-2017.
 */
public interface IMealTypeRepository extends IRepository<MealType>
{
    int create(MealType model);
    boolean update(MealType model);
    void failIfInvalid(MealType model);
    void failDeleteIfRelationsExist(int id);
}
