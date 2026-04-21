package org.example.notes.repository;

import org.example.notes.repository.model.NoteEntity;
import org.example.notes.repository.model.NoteVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<NoteEntity, Long>, JpaSpecificationExecutor<NoteEntity> {

    @Query("""
            select count(distinct n)
            from NoteEntity n
            join n.authors a
            where a.id = :userId and n.visibility = :visibility
            """)
    long countByAuthorAndVisibility(@Param("userId") Long userId, @Param("visibility") NoteVisibility visibility);

    @Query("select n from NoteEntity n join n.authors a where a.id = :userId")
    List<NoteEntity> findByAuthorId(@Param("userId") Long userId);

    @Query("select n.id from NoteEntity n join n.authors a where a.id = :userId")
    List<Long> findNoteIdsByAuthorId(@Param("userId") Long userId);

    @Query("select n from NoteEntity n join n.categories c where c.id = :categoryId")
    List<NoteEntity> findByCategoryId(@Param("categoryId") Long categoryId);
}
