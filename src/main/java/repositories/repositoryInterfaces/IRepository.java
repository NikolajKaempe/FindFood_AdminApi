package repositories.repositoryInterfaces;

import java.util.Collection;
import java.util.List;

/**
 * Created by Kaempe on 23-02-2017.
 */
public interface IRepository<T>
{
    Collection<T> getAll();
    T get(int id);
    boolean delete(int id);
    boolean exists(int id);
    boolean isPublished(int id);
    boolean publish(int id);
}
