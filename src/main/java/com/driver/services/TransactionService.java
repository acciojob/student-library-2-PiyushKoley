package com.driver.services;

import com.driver.models.*;
import com.driver.repositories.BookRepository;
import com.driver.repositories.CardRepository;
import com.driver.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequestMapping("/transaction")
public class TransactionService {

    @Autowired
    BookRepository bookRepository5;

    @Autowired
    CardRepository cardRepository5;

    @Autowired
    TransactionRepository transactionRepository5;

    @Value("${books.max_allowed}")
    public int max_allowed_books;

    @Value("${books.max_allowed_days}")
    public int getMax_allowed_days;

    @Value("${books.fine.per_day}")
    public int fine_per_day;


    public String issueBook(int cardId, int bookId) throws Exception {
        //check whether bookId and cardId already exist
        //conditions required for successful transaction of issue book:
        //1. book is present and available
        // If it fails: throw new Exception("Book is either unavailable or not present");

        Book book = bookRepository5.findById(bookId).orElse(null);
        Card card = cardRepository5.findById(cardId).orElse(null);

        Transaction transaction = Transaction.builder()
                                    .isIssueOperation(true)
                                    .card(card)
                                    .book(book)
                                    .build();



        if (book == null || book.isAvailable() == false) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Book is either unavailable or not present");
        }


        //2. card is present and activated
        // If it fails: throw new Exception("Card is invalid");


        if (card == null || card.getCardStatus().toString().equals("DEACTIVATED")) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Card is invalid");
        }

        //3. number of books issued against the card is strictly less than max_allowed_books
        // If it fails: throw new Exception("Book limit has reached for this card");

        if (card.getBooks().size() >= max_allowed_books) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository5.save(transaction);
            throw new Exception("Book limit has reached for this card");
        }


        //If the transaction is successful, save the transaction to the list of transactions and return the id

       book.setAvailable(false);
       book.setCard(card);

       List<Book> books = card.getBooks();
       books.add(book);
       card.setBooks(books);

       bookRepository5.updateBook(book);


        transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        transaction = transactionRepository5.save(transaction);


        //Note that the error message should match exactly in all cases

        return transaction.getTransactionId(); //return transactionId instead


    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId, TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);


        //***************** HOW TO FIND NUMBER OF DAYS IN BETWEEN ******************************

        Date issueDate = transaction.getTransactionDate();

        long totalIssueDuration = Math.abs(System.currentTimeMillis() - issueDate.getTime());

        long no_of_days = TimeUnit.DAYS.convert(totalIssueDuration,TimeUnit.MILLISECONDS);

        //*****************************************************************************************

        Book book = transaction.getBook();
        Card card = transaction.getCard();

        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        int fine = 0;
        if(no_of_days > getMax_allowed_days ){
            fine =(int) ((no_of_days - getMax_allowed_days) * fine_per_day);
        }

        Transaction newTransaction = Transaction.builder()
                .isIssueOperation(false)
                .fineAmount(fine)
                .transactionStatus(TransactionStatus.SUCCESSFUL)
                .card(card)
                .book(book)
                .build();


        newTransaction = transactionRepository5.save(newTransaction);

        //make the book available for other users


        book.setCard(null);
        book.setAvailable(true);

        List<Book> books = card.getBooks();
        books.remove(book);

        bookRepository5.updateBook(book);

        //make a new transaction for return book which contains the fine amount as well

//        Transaction returnBookTransaction  = null;


        return newTransaction; //return the transaction after updating all details
    }
}
