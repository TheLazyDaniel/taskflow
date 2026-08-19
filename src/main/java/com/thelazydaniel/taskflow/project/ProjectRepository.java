package com.thelazydaniel.taskflow.project;

import com.thelazydaniel.taskflow.project.entity.Project;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Override
    @NonNull
    Page<Project> findAll(@NonNull Pageable pageable);
}
