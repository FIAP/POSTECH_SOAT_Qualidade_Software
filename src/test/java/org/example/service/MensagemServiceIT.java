package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.example.exception.MensagemNotFoundException;
import org.example.model.Mensagem;
import org.example.repository.MensagemRepository;
import org.example.utils.MensagemHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class MensagemServiceIT {

  @Autowired
  private MensagemRepository mensagemRepository;

  @Autowired
  private MensagemService mensagemService;

  @Test
  void devePermitirRegistrarMensagem() {
    var mensagem = MensagemHelper.gerarMensagem();

    var mensagemArmazenada = mensagemService.criarMensagem(mensagem);

    assertThat(mensagemArmazenada)
        .isNotNull()
        .isInstanceOf(Mensagem.class);
    assertThat(mensagemArmazenada.getId())
        .isNotNull();
    assertThat(mensagemArmazenada.getUsuario())
        .isNotNull()
        .isNotEmpty()
        .isEqualTo(mensagem.getUsuario());
    assertThat(mensagemArmazenada.getConteudo())
        .isNotNull()
        .isNotEmpty()
        .isEqualTo(mensagem.getConteudo());
  }

  @Test
  void devePermitirBuscarMensagem() {
    var mensagem = MensagemHelper.registrarMensagem(mensagemRepository);

    var mensagemObtidaOptional = mensagemRepository.findById(mensagem.getId());

    assertThat(mensagemObtidaOptional)
        .isPresent()
        .containsSame(mensagem);
    mensagemObtidaOptional.ifPresent(mensagemObtida -> {
      assertThat(mensagemObtida.getId())
          .isEqualTo(mensagem.getId());
      assertThat(mensagemObtida.getUsuario())
          .isEqualTo(mensagem.getUsuario());
      assertThat(mensagemObtida.getConteudo())
          .isEqualTo(mensagem.getConteudo());
      assertThat(mensagemObtida.getDataCriacao())
          .isEqualTo(mensagem.getDataCriacao());
    });
  }

  @Test
  void deveGerarExcecao_QuandoBuscarMensagem_IdNaoExistente() {
    var id = UUID.fromString("50537a52-1ab2-11ee-be56-0242ac120002");

    assertThatThrownBy(() -> mensagemService.buscarMensagem(id))
        .isInstanceOf(MensagemNotFoundException.class)
        .hasMessage("mensagem não encontrada");
  }

  @Test
  void devePermirirAlterarMensagem() {
    var mensagemOriginal = MensagemHelper.registrarMensagem(mensagemRepository);
    var mensagemModificada = mensagemOriginal.toBuilder().build();
    mensagemModificada.setConteudo("abcd");

    var mensagemObtida = mensagemService.alterarMensagem(mensagemOriginal.getId(),
        mensagemModificada);

    assertThat(mensagemObtida)
        .isInstanceOf(Mensagem.class)
        .isNotNull();
    assertThat(mensagemObtida.getId())
        .isEqualTo(mensagemModificada.getId());
    assertThat(mensagemObtida.getUsuario())
        .isEqualTo(mensagemModificada.getUsuario());
    assertThat(mensagemObtida.getConteudo())
        .isEqualTo(mensagemModificada.getConteudo());
  }

  @Test
  void deveGerarExcecao_QuandoAlterarMensagem_IdNaoCoincide() {
    var id = UUID.fromString("5f789b39-4295-42c1-a65b-cfca5b987db2");
    var mensagemNova = MensagemHelper.gerarMensagemCompleta();

    assertThatThrownBy(
        () -> mensagemService.alterarMensagem(id, mensagemNova))
        .isInstanceOf(MensagemNotFoundException.class)
        .hasMessage("mensagem não apresenta o ID correto");
  }

  @Test
  void devePermitirApagarMensagem() {
    var mensagemRegistrada = MensagemHelper.registrarMensagem(mensagemRepository);
    var resultado = mensagemService.apagarMensagem(mensagemRegistrada.getId());
    assertThat(resultado).isTrue();
  }

  @Test
  void devePermitirIncrementarGostei() {
    var mensagemRegistrada = MensagemHelper.registrarMensagem(mensagemRepository);

    var mensagemRecebida = mensagemService.incrementarGostei(mensagemRegistrada.getId());

    assertThat(mensagemRecebida.getGostei()).isEqualTo(1);
  }

  @Test
  void devePermitirListarMensagens() {
    Page<Mensagem> mensagens = mensagemService.listarMensagens(Pageable.unpaged());

    assertThat(mensagens).hasSize(5);
    assertThat(mensagens.getContent())
        .asList()
        .allSatisfy(mensagem -> {
          assertThat(mensagem).isNotNull();
          assertThat(mensagem).isInstanceOf(Mensagem.class);
        });
  }

  @Test
  @Sql(scripts = {"/clean.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  void devePermitirListarTodasAsMensagens_QuandoNaoExisteRegistro() {
    Page<Mensagem> mensagens = mensagemService.listarMensagens(Pageable.unpaged());
    assertThat(mensagens).isEmpty();
  }

}
