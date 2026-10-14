
package hd.quanlysinhvien_hibernatedao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "academic_years")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academic_year_id")
    private Long id;

    @Column(name = "academic_year_code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "academic_year_name", nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "start_year", nullable = false)
    private Integer startYear;

    @Column(name = "end_year", nullable = false)
    private Integer endYear;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
