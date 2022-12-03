package com.driver.controller;

import com.driver.models.Book;
import com.driver.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Add required annotations
@RestController
@RequestMapping("/book")
public class BookController {


    //Write createBook API with required annotations
    @Autowired
    BookService service;

    //Add required annotations

    @PostMapping()
    public ResponseEntity createBook(@RequestBody()Book book) {

        service.createBook(book);

        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity getBooks(@RequestParam(value = "genre", required = false) String genre,
                                   @RequestParam(value = "available", required = false, defaultValue = "false") boolean available,
                                   @RequestParam(value = "author", required = false) String author){

        List<Book> bookList = null; //find the elements of the list by yourself



        return new ResponseEntity<>(service.getBooks(genre, available, author), HttpStatus.OK);

    }
}
