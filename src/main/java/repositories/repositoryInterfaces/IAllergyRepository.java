package repositories.repositoryInterfaces;

import models.Allergy;
import models.Ingredient;

import java.util.List;

/**
 * Created by Kaempe on 23-02-2017.
 */
public interface IAllergyRepository extends IRepository<Allergy>
{
    int create(Allergy model);
    boolean update(Allergy model);
    void failIfInvalid(Allergy model);
    void failDeleteIfRelationsExist(int id);
}
