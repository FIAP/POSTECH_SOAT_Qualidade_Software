package org.example.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import org.example.dto.MensagemRequest;
import org.example.exception.MensagemNotFoundException;
import org.example.handler.GlobalExceptionHandler;
import org.example.model.Mensagem;
import org.example.service.MensagemService;
import org.example.utils.MensagemHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.callibrity.logging.test.LogTracker;
import com.callibrity.logging.test.LogTrackerStub;
import com.fasterxml.jackson.databind.ObjectMapper;

class MensagemControllerTest {

  private MockMvc mockMvc;

//  @RegisterExtension
//  static MockMvcListener mockMvcListener = new MockMvcListener();

  @RegisterExtension
  LogTrackerStub logTracker = LogTrackerStub.create().recordForLevel(LogTracker.LogLevel.INFO)
      .recordForType(MensagemController.class);


  @Mock
  private MensagemService mensagemService;

  AutoCloseable openMocks;

  @BeforeEach
  void setUp() {
    openMocks = MockitoAnnotations.openMocks(this);
    MensagemController mensagemController = new MensagemController(mensagemService);
    mockMvc = MockMvcBuilders.standaloneSetup(mensagemController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .addFilter((request, response, chain) -> {
          response.setCharacterEncoding("UTF-8");
          chain.doFilter(request, response);
        }, "/*")
        .build();
  }

  @AfterEach
  void tearDown() throws Exception {
    openMocks.close();
  }

  @Nested
  class RegistrarMensagem {

@Test
void devePermitirRegistrarMensagem() throws Exception {
  var mensagemRequest = MensagemHelper.gerarMensagemRequest();
  when(mensagemService.criarMensagem(any(Mensagem.class)))
      .thenAnswer(i -> i.getArgument(0));

  mockMvc.perform(post("/mensagens")
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(mensagemRequest)))
//                    .andDo(print())
      .andExpect(status().isCreated());
  verify(mensagemService, times(1))
      .criarMensagem(any(Mensagem.class));
}

@Test
void deveGerarExcecao_QuandoRegistrarMensagem_UsuarioEmBraco() throws Exception {
  var mensagemRequest = MensagemRequest.builder()
      .usuario("")
      .conteudo("xpto")
      .build();

  mockMvc.perform(post("/mensagens")
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(mensagemRequest)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value("Validation error"))
      .andExpect(jsonPath("$.errors.[0]").value("usuário não pode estar vazio"));
  verify(mensagemService, never())
      .criarMensagem(any(Mensagem.class));
}

@Test
void deveGerarExcecao_QuandoRegistrarMensagem_ConteudoEmBranco() throws Exception {
  var mensagemRequest = MensagemRequest.builder()
      .usuario("John")
      .conteudo("")
      .build();

  mockMvc.perform(post("/mensagens")
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(mensagemRequest)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value("Validation error"))
      .andExpect(jsonPath("$.errors.[0]").value("conteúdo não pode estar vazio"));
  verify(mensagemService, never()).criarMensagem(any(Mensagem.class));
}

@Test
void deveGerarExcecao_QuandoRegistrarMensagem_CamposInvalidos() throws Exception {
  var mensagemRequest = new ObjectMapper().readTree(
      "{\"ping\": \"ping\", \"quack\": \"adalberto\"}");

  mockMvc.perform(post("/mensagens")
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(mensagemRequest)))
//      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(result -> {
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(json).contains("Validation error");
        assertThat(json).contains("usuário não pode estar vazio");
        assertThat(json).contains("conteúdo não pode estar vazio");
      });
  verify(mensagemService, never())
      .criarMensagem(any(Mensagem.class));
}

@Test
void deveGerarExcecao_QuandoRegistrarMensagem_PayloadComXml() throws Exception {
  String xmlPayload = "<mensagem><usuario>John</usuario><conteudo>Conteúdo da mensagem</conteudo></mensagem>";

  mockMvc.perform(post("/mensagens")
          .contentType(MediaType.APPLICATION_XML)
          .content(xmlPayload))
//      .andDo(print())
      .andExpect(status().isUnsupportedMediaType());
  verify(mensagemService, never()).criarMensagem(any(Mensagem.class));
}

@Test
void deveGerarMensagemDeLog_QuandoRegistrarMensagem() throws Exception {
  var mensagemRequest = MensagemHelper.gerarMensagemRequest();
  when(mensagemService.criarMensagem(any(Mensagem.class))).thenAnswer(i -> i.getArgument(0));

  mockMvc.perform(post("/mensagens")
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(mensagemRequest)))
      .andExpect(status().isCreated());
  verify(mensagemService, times(1))
      .criarMensagem(any(Mensagem.class));
  assertThat(logTracker.size()).isEqualTo(1);
}
  }

  @Nested
  class BuscarMensagem {

    @Test
    void devePermitirBuscarMensagem() throws Exception {
      var id = UUID.fromString("259bdc02-1ab5-11ee-be56-0242ac120002");
      var mensagem = MensagemHelper.gerarMensagem();
      mensagem.setId(id);
      mensagem.setDataCriacao(LocalDateTime.now());

      when(mensagemService.buscarMensagem(any(UUID.class))).thenReturn(mensagem);

      mockMvc.perform(get("/mensagens/{id}", id)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(mensagem.getId().toString()))
          .andExpect(jsonPath("$.conteudo").value(mensagem.getConteudo()))
          .andExpect(jsonPath("$.usuario").value(mensagem.getUsuario()))
          .andExpect(jsonPath("$.dataCriacao").exists())
          .andExpect(jsonPath("$.gostei").exists());
      verify(mensagemService, times(1)).buscarMensagem(any(UUID.class));
    }

    @Test
    void deveGerarExcecao_QuandoBuscarMensagem_IdNaoExistente()
        throws Exception {
      var id = UUID.fromString("259bdc02-1ab5-11ee-be56-0242ac120002");

      when(mensagemService.buscarMensagem(any(UUID.class)))
          .thenThrow(new MensagemNotFoundException("mensagem não encontrada"));

      mockMvc.perform(get("/mensagens/{id}", id)
              .contentType(MediaType.APPLICATION_JSON))
//          .andDo(print())
          .andExpect(status().isNotFound());
      verify(mensagemService, times(1))
          .buscarMensagem(any(UUID.class));
    }

    @Test
    void deveGerarExcecao_QuandoBuscarMensagem_IdInvalido()
        throws Exception {
      var id = "2";

      mockMvc.perform(get("/mensagens/{id}", id)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("ID inválido"));
      verify(mensagemService, never())
          .buscarMensagem(any(UUID.class));
    }

    @Test
    void deveGerarMensagemDeLog_QuandoBuscarMensagem() throws Exception {
      var id = UUID.fromString("259bdc02-1ab5-11ee-be56-0242ac120002");
      var mensagem = MensagemHelper.gerarMensagem();
      mensagem.setId(id);
      mensagem.setDataCriacao(LocalDateTime.now());

      when(mensagemService.buscarMensagem(any(UUID.class))).thenReturn(mensagem);

      mockMvc.perform(get("/mensagens/{id}", id)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
      assertThat(logTracker.size()).isEqualTo(1);
      assertThat(logTracker.contains("requisição para buscar mensagem foi efetuada"))
          .isTrue();
    }
  }

  @Nested
  class AlterarMensagem {

    @Test
    void devePermirirAlterarMensagem() throws Exception {
      var id = UUID.fromString("259bdc02-1ab5-11ee-be56-0242ac120002");
      var mensagem = MensagemHelper.gerarMensagem();
      mensagem.setId(id);

      when(mensagemService.alterarMensagem(any(UUID.class), any(Mensagem.class)))
          .thenAnswer(i -> i.getArgument(1));

      mockMvc.perform(put("/mensagens/{id}", id)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(mensagem)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(mensagem.getId().toString()))
          .andExpect(jsonPath("$.conteudo").value(mensagem.getConteudo()))
          .andExpect(jsonPath("$.usuario").value(mensagem.getUsuario()))
          .andExpect(jsonPath("$.dataCriacao").value(mensagem.getDataCriacao()))
          .andExpect(jsonPath("$.gostei").value(mensagem.getGostei()));
      verify(mensagemService, times(1))
          .alterarMensagem(any(UUID.class), any(Mensagem.class));
    }

    @Test
    void deveGerarExcecao_QuandoAlterarMensagem_IdNaoCoincide() throws Exception {
      var id = "259bdc02-1ab5-11ee-be56-0242ac120002";
      var mensagemRequest = MensagemHelper.gerarMensagem();

      when(mensagemService.alterarMensagem(any(UUID.class), any(Mensagem.class)))
          .thenThrow(new MensagemNotFoundException("mensagem não apresenta o ID correto"));

      mockMvc.perform(put("/mensagens/{id}", id)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(mensagemRequest)))
          .andExpect(status().isNotFound())
          .andExpect(content().string("mensagem não apresenta o ID correto"));
      verify(mensagemService, never()).apagarMensagem(any(UUID.class));
    }

    @Test
    void deveGerarExcecao_QuandoAlterarMensagem_IdInvalido() throws Exception {
      var id = "2";
      var mensagemRequest = MensagemHelper.gerarMensagem();

      mockMvc.perform(put("/mensagens/{id}", id)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(mensagemRequest)))
//          .andDo(print())
          .andExpect(status().isBadRequest())
          .andExpect(content().string("ID inválido"));
      verify(mensagemService, never())
          .apagarMensagem(any(UUID.class));
    }

    @Test
    void deveGerarExcecao_QuandoAlterarMensagem_PayloadComXml() throws Exception {
      var id = UUID.fromString("259bdc02-1ab5-11ee-be56-0242ac120002");
      String xmlPayload = "<mensagem><usuario>John</usuario><conteudo>Conteúdo da mensagem</conteudo></mensagem>";

      mockMvc.perform(put("/mensagens/{id}", id)
              .contentType(MediaType.APPLICATION_XML)
              .content(xmlPayload))
//          .andDo(print())
          .andExpect(status().isUnsupportedMediaType());
      verify(mensagemService, never()).alterarMensagem(any(UUID.class), any(Mensagem.class));
    }

    @Test
    void deveGerarMensagemDeLog_QuandoAlterarMensagem() throws Exception {
      var id = UUID.fromString("259bdc02-1ab5-11ee-be56-0242ac120002");
      var mensagem = MensagemHelper.gerarMensagem();
      mensagem.setId(id);

      when(mensagemService.alterarMensagem(any(UUID.class), any(Mensagem.class)))
          .thenAnswer(i -> i.getArgument(1));

      mockMvc.perform(put("/mensagens/{id}", id)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(mensagem)))
          .andExpect(status().isOk());
      assertThat(logTracker.size()).isEqualTo(1);
      assertThat(logTracker.contains("requisição para atualizar mensagem foi efetuada"))
          .isTrue();
    }
  }

  @Nested
  class ApagarMensagem {

    @Test
    void devePermitirApagarMensagem() throws Exception {
      var id = UUID.fromString("259bdc02-1ab5-11ee-be56-0242ac120002");
      when(mensagemService.apagarMensagem(any(UUID.class)))
          .thenReturn(true);

      mockMvc.perform(delete("/mensagens/{id}", id))
          .andExpect(status().isOk())
          .andExpect(content().string("mensagem removida"));
      verify(mensagemService, times(1))
          .apagarMensagem(any(UUID.class));
    }

    @Test
    void deveGerarExcecao_QuandoIncrementarGostei_IdInvalido()
        throws Exception {
      var id = "2";

      mockMvc.perform(delete("/mensagens/{id}", id)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("ID inválido"));
      verify(mensagemService, never())
          .apagarMensagem(any(UUID.class));
    }

    @Test
    void deveGerarExcecao_QuandoApagarMensagem_IdNaoExistente()
        throws Exception {
      var id = UUID.randomUUID();

      when(mensagemService.apagarMensagem(any(UUID.class)))
          .thenThrow(new MensagemNotFoundException("mensagem não encontrada"));

      mockMvc.perform(delete("/mensagens/{id}", id)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(content().string("mensagem não encontrada"));
      verify(mensagemService, times(1))
          .apagarMensagem(any(UUID.class));
    }

    @Test
    void deveGerarMensagemDeLog_QuandoApagarMensagem() throws Exception {
      var id = UUID.fromString("259bdc02-1ab5-11ee-be56-0242ac120002");
      when(mensagemService.apagarMensagem(any(UUID.class))).thenReturn(true);

      mockMvc.perform(delete("/mensagens/{id}", id))
          .andExpect(status().isOk());
      assertThat(logTracker.size()).isEqualTo(1);
      assertThat(logTracker.contains("requisição para apagar mensagem foi efetuada"))
          .isTrue();
    }

  }

  @Nested
  class IncrementarGostei {

@Test
void devePermitirIncrementarGostei() throws Exception {
  var mensagem = MensagemHelper.gerarMensagemCompleta();
  mensagem.setGostei(mensagem.getGostei() + 1);
  var id = mensagem.getId().toString();

  when(mensagemService.incrementarGostei(any(UUID.class))).thenReturn(mensagem);

  mockMvc.perform(put("/mensagens/{id}/gostei", id)
          .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(mensagem.getId().toString()))
      .andExpect(jsonPath("$.conteudo").value(mensagem.getConteudo()))
      .andExpect(jsonPath("$.usuario").value(mensagem.getUsuario()))
      .andExpect(jsonPath("$.dataCriacao").exists())
      .andExpect(jsonPath("$.gostei").exists())
      .andExpect(jsonPath("$.gostei").value(1));
  verify(mensagemService, times(1))
      .incrementarGostei(any(UUID.class));
}

@Test
void deveGerarExcecao_QuandoIncrementarGostei_IdInvalido()
    throws Exception {
  var id = "2";

  mockMvc.perform(put("/mensagens/{id}/gostei", id)
          .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest())
      .andExpect(content().string("ID inválido"));
  verify(mensagemService, never()).incrementarGostei(any(UUID.class));
}

@Test
void deveGerarExcecao_QuandoIncrementarGostei_IdNaoExistente()
    throws Exception {
  var id = "9b0d8b5b-99a8-4635-b92f-d234bb4c2c5a";
  when(mensagemService.incrementarGostei(any(UUID.class)))
      .thenThrow(new MensagemNotFoundException("mensagem não encontrada"));

  mockMvc.perform(put("/mensagens/{id}/gostei", id)
          .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(content().string("mensagem não encontrada"));
  verify(mensagemService, times(1))
      .incrementarGostei(any(UUID.class));
}

@Test
void deveGerarMensagemDeLog_QuandoIncrementarGostei() throws Exception {
  var mensagem = MensagemHelper.gerarMensagemCompleta();
  mensagem.setGostei(mensagem.getGostei() + 1);
  var id = mensagem.getId().toString();

  when(mensagemService.incrementarGostei(any(UUID.class))).thenReturn(mensagem);

  mockMvc.perform(put("/mensagens/{id}/gostei", id)
          .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());
  assertThat(logTracker.size()).isEqualTo(1);
  assertThat(logTracker.contains("requisição para incrementar gostei foi efetuada"))
      .isTrue();
}
  }

  @Nested
  class ListarMensagem {

    @Test
    void devePermitirListarMensagens() throws Exception {
      var mensagem = MensagemHelper.gerarMensagemCompleta();
      Page<Mensagem> page = new PageImpl<>(Collections.singletonList(
          mensagem
      ));
      when(mensagemService.listarMensagens(any(Pageable.class)))
          .thenReturn(page);
      mockMvc.perform(get("/mensagens")
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].id").value(mensagem.getId().toString()))
          .andExpect(jsonPath("$.content[0].conteudo").value(mensagem.getConteudo()))
          .andExpect(jsonPath("$.content[0].usuario").value(mensagem.getUsuario()))
          .andExpect(jsonPath("$.content[0].dataCriacao").exists())
          .andExpect(jsonPath("$.content[0].gostei").exists());
      verify(mensagemService, times(1))
          .listarMensagens(any(Pageable.class));
    }

    @Test
    void devePermitirListarMensagens_QuandoNaoExisteRegistro()
        throws Exception {
      Page<Mensagem> page = new PageImpl<>(Collections.emptyList());
      when(mensagemService.listarMensagens(any(Pageable.class)))
          .thenReturn(page);
      mockMvc.perform(get("/mensagens")
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray())
          .andExpect(jsonPath("$.content", empty()))
          .andExpect(jsonPath("$.content", hasSize(0)));
      verify(mensagemService, times(1))
          .listarMensagens(any(Pageable.class));
    }

    @Test
    void devePermitirListarMensagens_QuandoReceberParametrosInvalidos()
        throws Exception {
      Page<Mensagem> page = new PageImpl<>(Collections.emptyList());
      when(mensagemService.listarMensagens(any(Pageable.class)))
          .thenReturn(page);
      mockMvc.perform(get("/mensagens?page=2&ping=pong")
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray())
          .andExpect(jsonPath("$.content", empty()))
          .andExpect(jsonPath("$.content", hasSize(0)));
      verify(mensagemService, times(1)).listarMensagens(any(Pageable.class));
    }

    @Test
    void deveGerarMensagemDeLog_QuandoListarMensagens() throws Exception {
      var mensagem = MensagemHelper.gerarMensagemCompleta();
      Page<Mensagem> page = new PageImpl<>(Collections.singletonList(
          mensagem
      ));
      when(mensagemService.listarMensagens(any(Pageable.class)))
          .thenReturn(page);
      mockMvc.perform(get("/mensagens")
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
      assertThat(logTracker.size()).isEqualTo(1);
      assertThat(logTracker.contains(
          "requisição para listar mensagens foi efetuada: Página=0, Tamanho=10")).isTrue();

    }
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
