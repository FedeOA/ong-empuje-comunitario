package com.ong.empuje.comunitario.web_services.repository;

import com.ong.empuje.comunitario.web_services.model.Category;
import com.ong.empuje.comunitario.web_services.model.SavedFilter;
import com.ong.empuje.comunitario.web_services.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SavedFilterRepository extends JpaRepository<SavedFilter, Long> {
    
    // Usar @Query para categoryId (campo calculado)
    @Query("SELECT s FROM SavedFilter s WHERE s.category.id = :categoryId")
    List<SavedFilter> findByCategoryId(@Param("categoryId") Integer categoryId);
    
    // Usar @Query para userId (campo calculado)
    @Query("SELECT s FROM SavedFilter s WHERE s.user.id = :userId")
    List<SavedFilter> findByUserId(@Param("userId") Integer userId);
    
    // Método derivado para la relación directa (esto SÍ funciona)
    List<SavedFilter> findByCategory(Category category);
    List<SavedFilter> findByUser(User user);
    
    // Búsqueda por nombre y usuario
    @Query("SELECT s FROM SavedFilter s WHERE s.user.id = :userId AND LOWER(s.name) = LOWER(:name)")
    Optional<SavedFilter> findByUserIdAndName(@Param("userId") Integer userId, @Param("name") String name);
    
    // Búsqueda por ID y usuario para validación de permisos
    @Query("SELECT s FROM SavedFilter s WHERE s.id = :id AND s.user.id = :userId")
    Optional<SavedFilter> findByIdAndUserId(@Param("id") Long id, @Param("userId") Integer userId);
}