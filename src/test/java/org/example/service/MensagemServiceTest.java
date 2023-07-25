
package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.example.exception.MensagemNotFoundException;
import org.example.model.Mensagem;
import org.example.repository.MensagemRepository;
import org.example.utils.MensagemHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class MensagemServiceTest {

  private MensagemService mensagemService;
  @Mock
  private MensagemRepository mensagemRepository;
  AutoCloseable openMocks;

  @BeforeEach
  void setUp() {
    openMocks = MockitoAnnotations.openMocks(this);
    mensagemService = new MensagemServiceImpl(mensagemRepository);
  }

  @AfterEach
  void tearDown() throws Exception {
    openMocks.close();
  }

  @Nested
  class RegistrarMensagem {

    @Test
    void devePermitirRegistrarMensagem() {
      var mensagem = MensagemHelper.gerarMensagem();
      when(mensagemRepository.save(any(Mensagem.class)))
          .thenAnswer(i -> i.getArgument(0));

      var mensagemArmazenada = mensagemService.criarMensagem(mensagem);

      assertThat(mensagemArmazenada)
          .isInstanceOf(Mensagem.class)
          .isNotNull();
      assertThat(mensagemArmazenada.getUsuario())
          .isEqualTo(mensagem.getUsuario());
      assertThat(mensagemArmazenada.getId())
          .isNotNull();
      assertThat(mensagemArmazenada.getConteudo())
          .isEqualTo(mensagem.getConteudo());
      verify(mensagemRepository, times(1)).save(mensagem);
    }
  }

  @Nested
  class BuscarMensagem {

    @Test
    void devePermitirBuscarMensagem() {
      var id = UUID.randomUUID();
      var mensagem = MensagemHelper.gerarMensagem();
      when(mensagemRepository.findById(any(UUID.class)))
          .thenReturn(Optional.of(mensagem));

      var mensagemObtida = mensagemService.buscarMensagem(id);

      verify(mensagemRepository, times(1))
          .findById(id);
      assertThat(mensagemObtida)
          .isEqualTo(mensagem);
      assertThat(mensagemObtida.getId())
          .isEqualTo(mensagem.getId());
      assertThat(mensagemObtida.getUsuario())
          .isEqualTo(mensagem.getUsuario());
      assertThat(mensagemObtida.getConteudo())
          .isEqualTo(mensagem.getConteudo());
      assertThat(mensagemObtida.getDataCriacao())
          .isEqualTo(mensagem.getDataCriacao());
    }

    @Test
    void deveGerarExcecao_QuandoBuscarMensagem_IdNaoExistente() {
      var id = UUID.randomUUID();

      when(mensagemRepository.findById(any(UUID.class)))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> mensagemService.buscarMensagem(id))
          .isInstanceOf(MensagemNotFoundException.class)
          .hasMessage("mensagem não encontrada");
      verify(mensagemRepository, times(1)).findById(id);
    }
  }

  @Nested
  class AlterarMensagem {

    @Test
    void devePermirirAlterarMensagem() {
      var id = UUID.randomUUID();
      var mensagemAntiga = MensagemHelper.gerarMensagem();
      mensagemAntiga.setId(id);
      var mensagemNova = mensagemAntiga;
      mensagemNova.setConteudo("abcd");

      when(mensagemRepository.findById(any(UUID.class)))
          .thenReturn(Optional.of(mensagemAntiga));

      when(mensagemRepository.save(any(Mensagem.class)))
          .thenAnswer(i -> i.getArgument(0));

      var mensagemObtida = mensagemService
          .alterarMensagem(id, mensagemNova);

      assertThat(mensagemObtida)
          .isInstanceOf(Mensagem.class)
          .isNotNull();
      assertThat(mensagemObtida.getId())
          .isEqualTo(mensagemNova.getId());
      assertThat(mensagemObtida.getUsuario())
          .isEqualTo(mensagemNova.getUsuario());
      assertThat(mensagemObtida.getConteudo())
          .isEqualTo(mensagemNova.getConteudo());
      verify(mensagemRepository, times(1)).save(any(Mensagem.class));
    }

    @Test
    void deveGerarExcecao_QuandoAlterarMensagem_IdNaoCoincide() {
      var id = UUID.randomUUID();
      var mensagemAntiga = MensagemHelper.gerarMensagem();
      mensagemAntiga.setId(id);
      var mensagemNova = mensagemAntiga.toBuilder().build();
      mensagemNova.setId(UUID.randomUUID());

      assertThatThrownBy(
          () -> mensagemService.alterarMensagem(id, mensagemNova))
          .isInstanceOf(MensagemNotFoundException.class)
          .hasMessage("mensagem não encontrada");
      verify(mensagemRepository, never()).save(any(Mensagem.class));
    }

  }

  @Nested
  class RemoverMensagem {

    @Test
    void devePermitirApagarMensagem() {
      var id = UUID.fromString("51fa607a-1e61-11ee-be56-0242ac120002");
      var mensagem = MensagemHelper.gerarMensagem();
      mensagem.setId(id);
      when(mensagemRepository.findById(id))
          .thenReturn(Optional.of(mensagem));
      doNothing()
          .when(mensagemRepository).deleteById(id);

      var resultado = mensagemService.apagarMensagem(id);

      assertThat(resultado).isTrue();
      verify(mensagemRepository, times(1)).findById(any(UUID.class));
      verify(mensagemRepository, times(1)).delete(any(Mensagem.class));
    }

  }

  @Nested
  class IncrementarGostei {

    @Test
    void devePermitirIncrementarGostei() {
      var mensagem = MensagemHelper.gerarMensagem();
      mensagem.setId(UUID.randomUUID());

      when(mensagemRepository.findById(any(UUID.class)))
          .thenReturn(Optional.of(mensagem));

      when(mensagemRepository.save(any(Mensagem.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      var mensagemRecebida = mensagemService.incrementarGostei(mensagem.getId());

      verify(mensagemRepository, times(1)).save(mensagemRecebida);
      assertThat(mensagemRecebida.getGostei()).isEqualTo(1);
    }

  }

  @Nested
  class ListarMensagens {

    @Test
    void devePermitirListarMensagens() {
      Page<Mensagem> page = new PageImpl<>(Arrays.asList(
          MensagemHelper.gerarMensagem(),
          MensagemHelper.gerarMensagem()
      ));

      when(mensagemRepository.listarMensagens(any(Pageable.class)))
          .thenReturn(page);

      Page<Mensagem> mensagens = mensagemService.listarMensagens(Pageable.unpaged());

      assertThat(mensagens).hasSize(2);
      assertThat(mensagens.getContent())
          .asList()
          .allSatisfy(mensagem -> {
            assertThat(mensagem).isNotNull();
            assertThat(mensagem).isInstanceOf(Mensagem.class);
          });
      verify(mensagemRepository, times(1)).listarMensagens(any(Pageable.class));
    }

    @Test
    void devePermitirListarMensagens_QuandoNaoExisteRegistro() {
      Page<Mensagem> page = new PageImpl<>(Collections.emptyList());

      when(mensagemRepository.listarMensagens(any(Pageable.class)))
          .thenReturn(page);

      Page<Mensagem> mensagens = mensagemService.listarMensagens(Pageable.unpaged());

      assertThat(mensagens).isEmpty();
      verify(mensagemRepository, times(1)).listarMensagens(any(Pageable.class));
    }
  }
}
