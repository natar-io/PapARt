/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.exceptions;

/**
 *
 * @author jiii
 */
public class BoardNotDetectedException extends Exception {

    public BoardNotDetectedException() {
        super();
    }

    public BoardNotDetectedException(String message) {
        super(message);
    }

    public BoardNotDetectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public BoardNotDetectedException(Throwable cause) {
        super(cause);
    }

}
