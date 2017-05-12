package repositories.repositoryInterfaces;

import models.Allergy;
import models.Ingredient;

import java.util.Collection;

/**
 * Created by Kaempe on 24-02-2017.
 */
public interface IIngredientRepository extends IRepository<Ingredient>
{
    int create(Ingredient model);
    boolean update(Ingredient model);
    void failIfInvalid(Ingredient model);
    Collection<Allergy> getAllergiesFor(int id);
    void failDeleteIfRelationsExist(int id);
    boolean isRelationValid(int id);
}
