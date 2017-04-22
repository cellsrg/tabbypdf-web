package ru.cells.icc.tabbypdf.web.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.cells.icc.tabbypdf.web.data.entities.FileReference;

/**
 * CRUD-репозиторий для {@link FileReference}.
 *
 * @author aaltaev
 * @since 0.1
 */
@Repository
public interface FileReferenceRepository extends CrudRepository<FileReference, Long> {
}
