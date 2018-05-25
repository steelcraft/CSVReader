package ru.mts.csvreader.reciever;

import java.io.IOException;

public class ReceiveFileException extends Exception{

    ReceiveFileException(Throwable ex) {
        super(ex);
    }

}
