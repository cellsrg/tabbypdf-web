package ru.cells.icc.tabbypdf.web.data.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Сущность, предназаначенная для поиска файла по его id.
 *
 * @author aaltaev
 * @since 0.1
 */
@Entity(name = "file")
public class FileReference {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    public FileReference() {
    }

    public FileReference(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
