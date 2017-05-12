package repositories.repositoryInterfaces;

import models.RecipeType;

/**
 * Created by Kaempe on 17-03-2017.
 */
public interface IRecipeTypeRepository extends IRepository<RecipeType>
{
    int create(RecipeType model);
    boolean update(RecipeType model);
    void failIfInvalid(RecipeType model);
    void failDeleteIfRelationsExist(int id);
}
