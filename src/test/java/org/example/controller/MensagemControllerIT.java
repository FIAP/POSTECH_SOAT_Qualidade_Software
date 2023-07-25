
package org.example.controller;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.UUID;

import org.example.utils.MensagemHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/clean.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class MensagemControllerIT {

  @LocalServerPort
  private int port;

  @BeforeEach
  public void setup() {
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    // RestAssured.filters(new AllureRestAssured()); // desta forma como estamos utilizando nested class gera informação duplicada
  }

  @Nested
  class RegistrarMensagem {

    @Test
    void devePermitirRegistrarMensagem() {
      var mensagemRequest = MensagemHelper.gerarMensagemRequest();

      given()
        .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(mensagemRequest)
          .when()
          .post("/mensagens")
          .then()
          .statusCode(HttpStatus.CREATED.value())
          .body("$", hasKey("id"))
          .body("$", hasKey("usuario"))
          .body("$", hasKey("conteudo"))
          .body("$", hasKey("dataCriacao"))
          .body("$", hasKey("gostei"))
          .body("usuario", equalTo(mensagemRequest.getUsuario()))
          .body("conteudo", equalTo(mensagemRequest.getConteudo()));
    }

    @Test
    void deveGerarExcecao_QuandoRegistrarMensagem_UsuarioEmBranco() {
      var mensagemRequest = MensagemHelper.gerarMensagemRequest();
      mensagemRequest.setUsuario("");

      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(mensagemRequest)
          .when()
          .post("/mensagens")
          .then()
          .statusCode(HttpStatus.BAD_REQUEST.value())
          .body("$", hasKey("message"))
          .body("$", hasKey("errors"))
          .body("message", equalTo("Validation error"))
          .body("errors[0]", equalTo("usuário não pode estar vazio"));
    }

    @Test
    void deveGerarExcecao_QuandoRegistrarMensagem_ConteudoEmBranco() {
      var mensagemRequest = MensagemHelper.gerarMensagemRequest();
      mensagemRequest.setConteudo("");

      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(mensagemRequest)
          .when()
          .post("/mensagens")
          .then()
          .statusCode(HttpStatus.BAD_REQUEST.value())
          .body("$", hasKey("message"))
          .body("$", hasKey("errors"))
          .body("message", equalTo("Validation error"))
          .body("errors[0]", equalTo("conteúdo não pode estar vazio"));
    }

    @Test
    void deveGerarExcecao_QuandoRegistrarMensagem_CamposInvalidos() throws JsonProcessingException {
      var jsonPayload = new ObjectMapper().readTree(
          "{\"ping\": \"ping\", \"quack\": \"adalberto\"}");

      given()
      .filter(new AllureRestAssured())
          .contentType(ContentType.JSON)
          .body(jsonPayload)
          .when()
          .post("/mensagens")
          .then()
          .statusCode(HttpStatus.BAD_REQUEST.value())
          .body("$", hasKey("message"))
          .body("$", hasKey("errors"))
          .body("message", equalTo("Validation error"))
          .body("errors[0]", equalTo("conteúdo não pode estar vazio"))
          .body("errors[1]", equalTo("usuário não pode estar vazio"));
    }

    @Test
    void deveGerarExcecao_QuandoRegistrarMensagem_PayloadComXml() {
      String xmlPayload = "<mensagem><usuario>John</usuario><conteudo>Conteúdo da mensagem</conteudo></mensagem>";

      given()
          .contentType(MediaType.APPLICATION_XML_VALUE)
          .body(xmlPayload)
          .when()
          .post("/mensagens")
          .then()
          .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
    }

    @Test
    void devePermitirRegistrarMensagem_ValidarSchema() {
      var mensagemRequest = MensagemHelper.gerarMensagemRequest();

      given()
          .header("Content-Type", "application/json")
          .body(mensagemRequest)
          .when()
          .post("/mensagens")
          .then()
          .statusCode(HttpStatus.CREATED.value())
          .header("Content-Type", notNullValue())
          .header("Content-Type", startsWith("application/json"))
          .body(matchesJsonSchemaInClasspath("./schemas/MensagemResponseSchema.json"));
    }
  }

  @Nested
  class BuscarMensagem {

    @Test
    @Sql(scripts = {"/clean.sql",
        "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void devePermitirBuscarMensagem() {
      var id = "5f789b39-4295-42c1-a65b-cfca5b987db2";
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get("/mensagens/{id}", id)
          .then()
          .statusCode(HttpStatus.OK.value())
          .body(matchesJsonSchemaInClasspath("./schemas/MensagemResponseSchema.json"));
    }

    @Test
    void deveGerarExcecao_QuandoBuscarMensagem_IdNaoExistente() {
      var id = "5f789b39-4295-42c1-a65b-cfca5b987db3";
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get("/mensagens/{id}", id)
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value())
          .body(equalTo("mensagem não encontrada"));
    }

    @Test
    void deveGerarExcecao_QuandoBuscarMensagem_IdInvalido() {
      var id = "2";
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get("/mensagens/{id}", id)
          .then()
          .statusCode(HttpStatus.BAD_REQUEST.value())
          .body(equalTo("ID inválido"));
    }
  }

  @Nested
  class AlterarMensagem {

    @Test
    @Sql(scripts = {"/clean.sql",
        "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void devePermirirAlterarMensagem() {
      var id = "5f789b39-4295-42c1-a65b-cfca5b987db2";
      var mensagem = MensagemHelper.gerarMensagemCompleta();
      mensagem.setId(UUID.fromString(id));

      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(mensagem)
          .when()
          .put("/mensagens/{id}", id)
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("conteudo", equalTo(mensagem.getConteudo()));
    }

    @Test
    void deveGerarExcecao_QuandoAlterarMensagem_IdNaoCoincide() {
      var id = "5f789b39-4295-42c1-a65b-cfca5b987db2";
      var mensagem = MensagemHelper.gerarMensagemCompleta();

      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(mensagem)
          .when()
          .put("/mensagens/{id}", id)
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value())
          .body(equalTo("mensagem não encontrada"));
    }

    @Test
    void deveGerarExcecao_QuandoAlterarMensagem_IdInvalido() {
      var id = "5";
      var mensagem = MensagemHelper.gerarMensagemCompleta();

      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .body(mensagem)
          .when()
          .put("/mensagens/{id}", id)
          .then()
          .statusCode(HttpStatus.BAD_REQUEST.value())
          .body(equalTo("ID inválido"));
    }

    @Test
    void deveGerarExcecao_QuandoAlterarMensagem_PayloadComXml() {
      var id = "5f789b39-4295-42c1-a65b-cfca5b987db2";
      String xmlPayload = "<mensagem><usuario>John</usuario><conteudo>Conteúdo da mensagem</conteudo></mensagem>";

      given()
      .filter(new AllureRestAssured())
          .contentType(ContentType.XML)
          .body(xmlPayload)
          .when()
          .put("/mensagens/{id}", id)
          .then()
          .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
    }
  }

  @Nested
  class ApagarMensagem {

    @Test
    @Sql(scripts = {"/clean.sql",
        "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void devePermitirApagarMensagem() {
      var id = "5f789b39-4295-42c1-a65b-cfca5b987db2";
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .delete("/mensagens/{id}", id)
          .then()
          .statusCode(HttpStatus.OK.value())
          .body(equalTo("mensagem removida"));
    }

    @Test
    void deveGerarExcecao_QuandoApagarMensagem_IdNaoExistente() {
      var id = "5f789b39-4295-42c1-a65b-cfca5b987db3";
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .delete("/mensagens/{id}", id)
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value())
          .body(equalTo("mensagem não encontrada"));
    }

    @Test
    void deveGerarExcecao_QuandoIncrementarGostei_IdInvalido() {
      var id = "2";
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .delete("/mensagens/{id}", id)
          .then()
          .statusCode(HttpStatus.BAD_REQUEST.value())
          .body(equalTo("ID inválido"));
    }

  }

  @Nested
  class IncrementarGostei {

    @Test
    @Sql(scripts = {"/clean.sql",
        "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void devePermitirIncrementarGostei() {
      var id = "5f789b39-4295-42c1-a65b-cfca5b987db2";
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .put("/mensagens/{id}/gostei", id)
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("gostei", equalTo(1))
          .body(matchesJsonSchemaInClasspath("./schemas/MensagemResponseSchema.json"));
    }

    @Test
    void deveGerarExcecao_QuandoIncrementarGostei_IdNaoExistente() {
      var id = "5f789b39-4295-42c1-a65b-cfca5b987db3";
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .put("/mensagens/{id}/gostei", id)
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value())
          .body(equalTo("mensagem não encontrada"));
    }

    @Test
    void deveGerarExcecao_QuandoIncrementarGostei_IdInvalido() {
      var id = "2";
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .put("/mensagens/{id}/gostei", id)
          .then()
          .statusCode(HttpStatus.BAD_REQUEST.value())
          .body(equalTo("ID inválido"));
    }
  }

  @Nested
  class ListarMensagem {

    @Test
    @Sql(scripts = {"/clean.sql",
        "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void devePermitirListarMensagens() {
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get("/mensagens")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body(matchesJsonSchemaInClasspath("./schemas/MensagemPaginationSchema.json"))
          .body("number", equalTo(0))
          .body("size", equalTo(10))
          .body("totalElements", equalTo(5));
    }

    @Test
    @Sql(scripts = {"/clean.sql",
        "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void devePermitirListarMensagens_QuandoInformadoParametros() {
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .queryParam("page", "2")
          .queryParam("size", "2")
          .when()
          .get("/mensagens")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body(matchesJsonSchemaInClasspath("./schemas/MensagemPaginationSchema.json"))
          .body("number", equalTo(2))
          .body("size", equalTo(2))
          .body("totalElements", equalTo(5));
    }

    @Test
    void devePermitirListarMensagens_QuandoNaoExisteRegistro() {
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get("/mensagens")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body(matchesJsonSchemaInClasspath("./schemas/MensagemPaginationSchema.json"))
          .body("number", equalTo(0))
          .body("size", equalTo(10))
          .body("totalElements", equalTo(0));
    }

    @Test
    void devePermitirListarMensagens_QuandoReceberParametrosInvalidos() {
      given()
      .filter(new AllureRestAssured())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .queryParam("page", "2")
          .queryParam("ping", "pong")
          .when()
          .get("/mensagens")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body(matchesJsonSchemaInClasspath("./schemas/MensagemPaginationSchema.json"))
          .body("number", equalTo(2))
          .body("size", equalTo(10))
          .body("totalElements", equalTo(0));
    }
  }

}
