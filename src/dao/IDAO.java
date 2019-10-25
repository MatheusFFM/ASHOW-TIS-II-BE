package dao;
import java.util.List;

public interface IDAO<T, K> {
    public T get(K chave);
    public void add(T p);
    public void update(T p);
    public void remove(T p);
    public List<T> getAll();
}