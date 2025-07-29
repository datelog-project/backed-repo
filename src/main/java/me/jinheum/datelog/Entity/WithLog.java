package me.jinheum.datelog.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Getter
@Setter
@Table(name = "with_logs")
@NoArgsConstructor
@AllArgsConstructor
public class WithLog {
    
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_connection_id", nullable = false)
    private UserConnection userConnection;

    private LocalDate date;

    private String placeName;

    private String placeAddress;

    @Column(precision = 15, scale = 10)
    private BigDecimal placeLat;

    @Column(precision = 15, scale = 10)
    private BigDecimal placeLng; 

    @Min(1)
    @Max(10)
    private Integer feelingScore;

    @Lob
    private String note;

    @Min(0)
    private Long cost;
    
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "withLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> mediaList = new ArrayList<>();

    @OneToMany(mappedBy = "withLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedLink> sharedLinks = new ArrayList<>();
}
