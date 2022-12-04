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

        if(book == null || book.isAvailable() == false){
            throw new Exception("Book is either unavailable or not present");
        }


        //2. card is present and activated
        // If it fails: throw new Exception("Card is invalid");

        Card card = cardRepository5.findById(cardId).orElse(null);

        if(card == null || card.getCardStatus().toString().equals("DEACTIVATED")) {
            throw new Exception("Card is invalid");
        }

        //3. number of books issued against the card is strictly less than max_allowed_books
        // If it fails: throw new Exception("Book limit has reached for this card");

        if(card.getBooks().size() >= max_allowed_books) {
            throw new Exception("Book limit has reached for this card");
        }

        //If the transaction is successful, save the transaction to the list of transactions and return the id
        book.setAvailable(false);
        book.setCard(card);
        bookRepository5.updateBook(book);
        Transaction transaction = Transaction.builder()
                                    .book(book)
                                    .card(card)
                                    .transactionStatus(TransactionStatus.SUCCESSFUL)
                                    .isIssueOperation(true)
                                .build();

        transaction = transactionRepository5.save(transaction);


        //Note that the error message should match exactly in all cases

       return transaction.getTransactionId(); //return transactionId instead
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId, TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);

        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        Date issueDate = transaction.getTransactionDate();
        Transaction newTransaction = Transaction.builder().build();


        //make the book available for other users

        Book book = transaction.getBook();
        book.setCard(null);
        book.setAvailable(true);
        bookRepository5.updateBook(book);

        //make a new transaction for return book which contains the fine amount as well

        Transaction returnBookTransaction  = null;
        return returnBookTransaction; //return the transaction after updating all details
    }
}
