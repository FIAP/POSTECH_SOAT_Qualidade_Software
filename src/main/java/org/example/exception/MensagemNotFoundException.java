
package org.example.exception;

public class MensagemNotFoundException extends RuntimeException {

  public MensagemNotFoundException(String mensagem) {
    super(mensagem);
  }

}
