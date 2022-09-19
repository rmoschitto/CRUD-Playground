package com.galvanize.crudplayground.lessons;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
public class LessonController {

    LessonRepository repository;

    public LessonController(LessonRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/lesson")
    public Lesson createNewLesson(@RequestBody Lesson lesson) {
        return this.repository.save(lesson);
    }

    @GetMapping("/lesson")
    public List<Lesson> getAllLessons() {
        return this.repository.findAll();
    }

    @GetMapping("/lesson/{id}")
    public Lesson getOneLessonById(@PathVariable Long id) {
//            if (this.repository.findById(id).isPresent()) {
//                Lesson lesson = repository.findById(id).get();
//                return new ResponseEntity<>(lesson, HttpStatus.OK);
//            } else {
//                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
//            }

//        try {
//                Lesson lesson = repository.findById(id).get();
//                return new ResponseEntity<>(lesson, HttpStatus.OK);
//        } catch (NoSuchElementException e) {
//            return new ResponseEntity<>(String.format("Record with id %d is not present", id), HttpStatus.NOT_FOUND);
//        }
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException(String.format("Record with id %d is not present", id)));
    }

    @DeleteMapping("/lesson/{id}")
    public ResponseEntity<Object> deleteMapping(@PathVariable Long id) {
        this.repository.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/lesson/{id}")
    public Lesson updateRecord(@PathVariable Long id, @RequestBody Map<String, String> body) {    // if we pull the body in as a lesson we need to check for default values
        // Map is creating a key value pair like "title" , "JPA is OKAY"
        // First step find the old record
        Lesson oldLesson = repository.findById(id).orElseThrow(() -> new NoSuchElementException(String.format("Record with id %d is not present", id)));

        // Figure out what to edit
        body.forEach(
                (key, value) -> {
                    // Edit those fields
                    if (key.equals("title")) {
                        oldLesson.setTitle(value);
                    } else if (key.equals("deliveredOn")) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        //convert String to LocalDate
                        LocalDate localDate = LocalDate.parse(value, formatter);
                        oldLesson.setDeliveredOn(localDate);
                    }
                }
        );
        //save the record
       return this.repository.save(oldLesson);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public String handleElementNotFound(Exception e) {
        return e.getMessage();
    }

//    @ExceptionHandler(NoSuchElementException.class)
//    public ResponseEntity<String> handleElementNotFoundWithResponseEntity(Exception e) {
//        return new ResponseEntity<>( e.getMessage(), HttpStatus.NOT_FOUND);
//    }



}
