package com.andres.course.agy.springboot.user_manager.app.repositories;

import com.andres.course.agy.springboot.user_manager.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
