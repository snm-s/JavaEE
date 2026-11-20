package fr.cytech.malinassur.dao;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.Transaction;

import fr.cytech.malinassur.util.HibernateUtil;

/**
 * DAO générique : le CRUD écrit UNE fois pour toutes les entités (factorisation du code).
 * Chaque DAO concret hérite de celui-ci et n'ajoute que ses requêtes spécifiques.
 *
 * Toutes les écritures passent par une transaction : si une erreur arrive au milieu,
 * on fait un rollback et la base reste cohérente (exigence des consignes).
 */
public abstract class BaseDao<T> {

    private final Class<T> entityClass;

    protected BaseDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // ---------- Outils de transaction (réutilisés partout) ----------

    /** Exécute du code qui renvoie un résultat, dans une transaction. */
    protected <R> R inTransaction(Function<Session, R> work) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            R result = work.apply(session);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /** Même chose pour du code qui ne renvoie rien. */
    protected void inTransactionVoid(Consumer<Session> work) {
        inTransaction(session -> { work.accept(session); return null; });
    }

    /** Pour les simples lectures. */
    protected <R> R read(Function<Session, R> work) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return work.apply(session);
        }
    }

    // ---------- CRUD ----------

    public Optional<T> findById(Long id) {
        return read(s -> Optional.ofNullable(s.get(entityClass, id)));
    }

    public List<T> findAll() {
        return read(s -> s.createQuery("from " + entityClass.getSimpleName(), entityClass).getResultList());
    }

    public T save(T entity) {
        inTransactionVoid(s -> s.persist(entity));
        return entity;
    }

    public T update(T entity) {
        return inTransaction(s -> s.merge(entity));
    }

    public void deleteById(Long id) {
        inTransactionVoid(s -> {
            T entity = s.get(entityClass, id);
            if (entity != null) s.remove(entity);
        });
    }
}
