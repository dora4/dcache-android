package dora.db.dao;

import dora.db.OrmTable;
import dora.db.builder.QueryBuilder;
import dora.db.builder.WhereBuilder;

import java.util.List;

public interface Dao<T extends OrmTable> {

    boolean insert(T bean);

    boolean insert(List<T> beans);

    boolean delete(WhereBuilder builder);

    boolean delete(T bean);

    boolean deleteAll();

    boolean update(WhereBuilder builder, T newBean);

    boolean update(T bean);

    @Deprecated
    boolean updateAll(T newBean);

    List<T> selectAll();

    List<T> select(WhereBuilder builder);

    List<T> select(QueryBuilder builder);

    T selectOne();

    T selectOne(WhereBuilder builder);

    T selectOne(QueryBuilder builder);

    long selectCount();

    long selectCount(WhereBuilder builder);

    long selectCount(QueryBuilder builder);
}
