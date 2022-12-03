package com.driver.services;

import com.driver.models.Student;
import com.driver.repositories.CardRepository;
import com.driver.repositories.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StudentService {


    @Autowired
    CardService cardService4;

    @Autowired
    StudentRepository studentRepository4;

    @Autowired
    CardRepository cardRepository;

    public Student getDetailsByEmail(String email){

        Student student = studentRepository4.findByEmailId(email);

        return student;
    }

    public Student getDetailsById(int id){
        Student student = studentRepository4.findById(id).orElse(null);
//        log.info(student.toString());
        return student;
    }

    public void createStudent(Student student){
        studentRepository4.save(student);
    }

    public void updateStudent(Student student){

        int i = studentRepository4.updateStudentDetails(student);
        log.info("rows affected = " + i);
    }

    public void deleteStudent(int id){
        //Delete student and deactivate corresponding card
        cardService4.deactivateCard(id);
        studentRepository4.deleteCustom(id);
    }
}
