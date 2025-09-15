package com.pado.domain.material.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "material_file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "file_key", nullable = false)
    private String fileKey; // S3에 저장된 파일의 키

    // Material 생성 이후 setter를 통해 연관관계를 지어줘야 함
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    public File(String name, String fileKey) {
        this.name = name;
        this.fileKey = fileKey;
    }
}
