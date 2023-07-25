package org.example.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.example.model.Mensagem;
import org.example.utils.MensagemHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class MensagemRepositoryIT {

  @Autowired
  private MensagemRepository mensagemRepository;

  @Test
  void devePermitirCriarTabela() {
    long totalTabelasCriada = mensagemRepository.count();
    assertThat(totalTabelasCriada).isNotNegative();
  }

  @Test
  void devePermitirRegistrarMensagem() {
    // Arrange
    var id = "f8faeba0-1f6b-46b7-bb68-3d8ebcc7f658";
    var mensagem = MensagemHelper.gerarMensagem();
    mensagem.setId(UUID.fromString(id));
    // Act
    var mensagemArmazenada = mensagemRepository.save(mensagem);
    // Assert
    assertThat(mensagemArmazenada)
        .isInstanceOf(Mensagem.class)
        .isNotNull();
    assertThat(mensagemArmazenada.getId())
        .isEqualTo(mensagem.getId());
    assertThat(mensagemArmazenada.getUsuario())
        .isEqualTo(mensagem.getUsuario());
    assertThat(mensagemArmazenada.getConteudo())
        .isEqualTo(mensagem.getConteudo());
    assertThat(mensagemArmazenada.getDataCriacao())
        .isNotNull();
    assertThat(mensagemArmazenada.getDataAlteracao())
        .isNotNull();
  }

  @Test
  void devePermitirConsultarMensagem() {
    // Arrange
    var mensagem = registrarMensagem();
    var id = mensagem.getId();
    // Act
    var mensagemOptional = mensagemRepository.findById(id);
    // Assert
    assertThat(mensagemOptional)
        .isPresent()
        .containsSame(mensagem);
    mensagemOptional.ifPresent(mensagemArmazenada -> {
      assertThat(mensagemArmazenada.getId())
          .isEqualTo(mensagem.getId());
      assertThat(mensagemArmazenada.getUsuario())
          .isEqualTo(mensagem.getUsuario());
      assertThat(mensagemArmazenada.getConteudo())
          .isEqualTo(mensagem.getConteudo());
      assertThat(mensagemArmazenada.getDataCriacao())
          .isEqualTo(mensagem.getDataCriacao());
    });
  }

  @Test
  void devePermitirApagarMensagem() {
    // Arrange
    var mensagem = registrarMensagem();
    var id = mensagem.getId();
    // Act
    mensagemRepository.deleteById(id);
    var mensagemOptional = mensagemRepository.findById(id);
    // Assert
    assertThat(mensagemOptional)
        .isEmpty();
  }

  @Test
  void devePermitirListarMensagens() {
    // Act
    var resultado = mensagemRepository.findAll();
    // Assert
    assertThat(resultado)
        .hasSize(5);
  }

  private Mensagem gerarMensagem() {
    return Mensagem.builder()
        .usuario("joe")
        .conteudo("xpto test")
        .build();
  }

  private Mensagem registrarMensagem() {
    var mensagem = gerarMensagem();
    mensagem.setId(UUID.randomUUID());
    return mensagemRepository.save(mensagem);
  }

}
