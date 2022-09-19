package com.galvanize.crudplayground.lessons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.transaction.Transactional;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LessonControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    LessonRepository repository;

    Lesson lesson1;
    Lesson lesson2;
    ObjectMapper mapper = new ObjectMapper().registerModule( new JavaTimeModule());

    @BeforeEach
    void setup() {
        lesson1 = new Lesson();
        lesson2 = new Lesson();
        lesson1.setDeliveredOn(LocalDate.now());
        lesson1.setTitle("Testing JPA is Fun");
        lesson2.setDeliveredOn(LocalDate.of(2022, 9, 11));
        lesson2.setTitle("The colts are going to the superbowl");
    }

    @Test
    @Transactional
    @Rollback
    public void getAllReturnsEmptyArrayWithNoRecords() throws Exception {
        MockHttpServletRequestBuilder request = get("/lesson");

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    @Rollback
    public void getAllReturnsArrayWithMultipleRecords() throws Exception {
        MockHttpServletRequestBuilder request = get("/lesson");
        this.repository.save(lesson1);
        this.repository.save(lesson2);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[1].title").value("The colts are going to the superbowl"))
                .andExpect(jsonPath("$[0].deliveredOn").value("2022-09-16"))
                .andExpect(jsonPath("$[0].id").value(lesson1.getId()));
    }

    @Test
    @Transactional
    @Rollback
    public void canGetRecordById() throws Exception {
        this.repository.save(lesson1);
        this.repository.save(lesson2);

        String url = String.format("/lesson/%d", lesson2.getId());
        MockHttpServletRequestBuilder request = get(url);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The colts are going to the superbowl"))
                .andExpect(jsonPath("$.id").value(lesson2.getId()))
                .andExpect(jsonPath("$.deliveredOn").value("2022-09-11"));

        this.mvc.perform(get("/lesson/-1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Record with id -1 is not present"));
    }

    @Test
    @Transactional
    @Rollback
    public void postCreatesNewDbEntry() throws Exception {
//        HashMap<String, String> body = new HashMap<>();
//        body.put("title", "JT for MVP");
//        body.put("deliveredOn", "2021-08-10");
//        String json = mapper.writeValueAsString(body);
        lesson1.setTitle("JT for MVP");
        lesson1.setDeliveredOn(LocalDate.of(2021, 8, 10));
        String json = mapper.writeValueAsString(lesson1);

        MockHttpServletRequestBuilder request = post("/lesson")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("JT for MVP"))
                .andExpect(jsonPath("$.deliveredOn").value("2021-08-10"))
                .andExpect(jsonPath("$.id").isNumber());

        assertEquals(1, this.repository.count());
        this.mvc.perform(get("/lesson"))
                .andExpect(jsonPath("$[0].title").value("JT for MVP"));
    }

    @Test
    @Transactional
    @Rollback
    public void canDeleteRecord() throws Exception {
        this.repository.save(lesson1);
        this.repository.save(lesson2);

        String url = String.format("/lesson/%d", lesson1.getId());
        MockHttpServletRequestBuilder request = delete(url);

        this.mvc.perform(request)
                .andExpect(status().isNoContent());

        assertEquals(1, this.repository.count());
    }

    @Test
    @Transactional
    @Rollback
    public void canUpdateRecord() throws Exception {
        this.repository.save(lesson1);

        //make sone JSON for the patch request

        String json = "{\"title\":\"JPA is OKAY\"}";

                //Make the request
        MockHttpServletRequestBuilder request = patch(String.format("/lesson/%d", lesson1.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //Send the request
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("JPA is OKAY"));

        this.mvc.perform(get(String.format("/lesson/%d", lesson1.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("JPA is OKAY"));

    }

}
