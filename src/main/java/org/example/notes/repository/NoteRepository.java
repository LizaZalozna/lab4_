package org.example.notes.repository;

import org.example.notes.repository.model.NoteEntity;
import org.example.notes.repository.model.NoteVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<NoteEntity, Long>, JpaSpecificationExecutor<NoteEntity> {

    @Query("""
            select count(distinct n)
            from NoteEntity n
            join n.authors a
            where a.id = :userId and n.visibility = :visibility
            """)
    long countByAuthorAndVisibility(@Param("userId") Long userId, @Param("visibility") NoteVisibility visibility);
}
