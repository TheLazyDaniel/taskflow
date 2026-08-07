package com.thelazydaniel.taskflow.repository;


import com.thelazydaniel.taskflow.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
