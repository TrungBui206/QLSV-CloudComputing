
package hd.quanlysinhvien_hibernatedao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "class_info")
@Getter
@Setter
@NoArgsConstructor
public class ClassInfo {

    @Id
    @Column(name = "ID", length = 36)
    private String id;

    @Column(name = "NAME", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "DOMAIN", nullable = false, length = 120)
    private String domain;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }
}
