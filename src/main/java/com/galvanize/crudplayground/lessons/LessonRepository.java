package com.galvanize.crudplayground.lessons;


import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Optional<Lesson> findByTitle(String title); //Optional Type is wrapping a lesson
    //Name very much matters, the Title must match the same named "title" field in Lesson

    List<Lesson> findByDeliveredOnBetween(LocalDate dateOne, LocalDate dateTwo);

    //Name very much matters, must match the same named "deliveredOn" field in Lesson
}
