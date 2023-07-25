package org.example.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.example.model.Mensagem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MensagemRepositoryTest {

  @Mock
  private MensagemRepository mensagemRepository;

  AutoCloseable openMocks;

  @BeforeEach
  void setUp() {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    openMocks.close();
  }

  @Test
  void devePermitirRegistrarMensagem() {
    // Arrange
    var mensagem = gerarMensagem();
    when(mensagemRepository.save(any(Mensagem.class))).thenReturn(mensagem);
    // Act
    var mensagemArmazenada = mensagemRepository.save(mensagem);
    // Assert
    verify(mensagemRepository, times(1)).save(mensagem);
    assertThat(mensagemArmazenada)
        .isInstanceOf(Mensagem.class)
        .isNotNull()
        .isEqualTo(mensagem);
    assertThat(mensagemArmazenada)
        .extracting(Mensagem::getId)
        .isEqualTo(mensagem.getId());
    assertThat(mensagemArmazenada)
        .extracting(Mensagem::getUsuario)
        .isEqualTo(mensagem.getUsuario());
    assertThat(mensagemArmazenada)
        .extracting(Mensagem::getConteudo)
        .isEqualTo(mensagem.getConteudo());
    assertThat(mensagemArmazenada)
        .extracting(Mensagem::getDataCriacao)
        .isEqualTo(mensagem.getDataCriacao());
  }

  @Test
  void devePermitirConsultarMensagem() {
    // Arrange
    var id = UUID.randomUUID();
    var mensagem = gerarMensagem();
    mensagem.setId(id);

    when(mensagemRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(mensagem));
    // Act
    var mensagemOptional = mensagemRepository.findById(id);
    // Assert
    verify(mensagemRepository, times(1)).findById(id);
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
    var id = UUID.randomUUID();
    doNothing().when(mensagemRepository).deleteById(id);
    // Act
    mensagemRepository.deleteById(id);
    // Assert
    verify(mensagemRepository, times(1)).deleteById(id);
  }

  @Test
  void devePermitirListarMensagens() {
    // Arrange
    var mensagem1 = gerarMensagem();
    var mensagem2 = gerarMensagem();
    var mensagemList = Arrays.asList(mensagem1, mensagem2);

    when(mensagemRepository.findAll()).thenReturn(mensagemList);

    // Act
    var resultado = mensagemRepository.findAll();

    // Assert
    verify(mensagemRepository, times(1)).findAll();
    assertThat(resultado)
        .hasSize(2)
        .containsExactlyInAnyOrder(mensagem1, mensagem2);
  }

  private Mensagem gerarMensagem() {
    return Mensagem.builder().usuario("joe").conteudo("xpto test").build();
  }

}
