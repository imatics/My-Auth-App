package com.auth.app.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Privilege {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(columnDefinition = "VARCHAR(255)")
        @JdbcType(VarcharJdbcType.class)
        private UUID id;
        private String name;
        }


