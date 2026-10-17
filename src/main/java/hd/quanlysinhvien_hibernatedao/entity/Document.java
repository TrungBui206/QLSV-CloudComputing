
package hd.quanlysinhvien_hibernatedao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@NamedQuery(
        name = "getDocumentByName",
        query = """
                SELECT d FROM Document d
                WHERE d.name = :name
                AND d.deleted = false
                AND d.active = true
                AND d.appId = :appId
                ORDER BY d.version DESC
                """
)
@NamedQuery(
        name = "getDocumentByAppId",
        query = """
                SELECT d FROM Document d
                WHERE d.appId = :appId
                AND d.deleted = false
                ORDER BY d.creationTime DESC
                """
)


@Entity
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @Column(name = "ID", length = 36)
    private String id;

    @Column(name = "APP_DETAIL_FK", nullable = false)
    private String appId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "PATH", nullable = false, length = 1000)
    private String path;

    @Column(name = "OWNER")
    private String owner;

    @Column(name = "VERSION")
    private Double version;

    @Column(name = "DELETED")
    private Boolean deleted;

    @Column(name = "ACTIVE")
    private Boolean active;

    @Column(name = "CREATION_TIME")
    private LocalDateTime creationTime;

    @Column(name = "MODIFICATION_TIME")
    private LocalDateTime modificationTime;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        LocalDateTime now = LocalDateTime.now();

        creationTime = now;
        modificationTime = now;

        if (deleted == null) deleted = false;
        if (active == null) active = true;
        if (version == null) version = 1.0;
    }

    @PreUpdate
    protected void onUpdate() {
        modificationTime = LocalDateTime.now();
    }
}
