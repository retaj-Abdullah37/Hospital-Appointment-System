package projectcode1.dao;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Manageable Interface - Generic CRUD operations interface
 * Demonstrates: Interfaces, Generics
 *
 * This interface defines standard database operations that all DAO classes must implement
 * Uses generic type T to work with different entity types (Patient, Doctor, etc.)
 *
 * @param <T> The type of entity this interface manages
 * @author CS313 Term Project
 */
public interface Manageable<T> {

    /**
     * Add a new entity to the database
     * @param item The entity to add
     * @throws SQLException if database error occurs
     */
    void add(T item) throws SQLException;

    /**
     * Update an existing entity in the database
     * @param item The entity to update
     * @throws SQLException if database error occurs
     */
    void update(T item) throws SQLException;

    /**
     * Delete an entity from the database by ID
     * @param id The ID of the entity to delete
     * @throws SQLException if database error occurs
     */
    void delete(int id) throws SQLException;

    /**
     * Retrieve an entity by its ID
     * @param id The ID of the entity to retrieve
     * @return The entity object, or null if not found
     * @throws SQLException if database error occurs
     */
    T getById(int id) throws SQLException;

    /**
     * Retrieve all entities from the database
     * @return ArrayList of all entities
     * @throws SQLException if database error occurs
     */
    ArrayList<T> getAll() throws SQLException;
}
