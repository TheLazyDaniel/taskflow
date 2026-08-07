package com.thelazydaniel.taskflow.repository;


import com.thelazydaniel.taskflow.domain.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Override
    @NonNull
    Page<User> findAll(@NonNull Pageable pageable);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
