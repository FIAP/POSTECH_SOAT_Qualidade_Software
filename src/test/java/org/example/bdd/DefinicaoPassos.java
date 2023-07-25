package org.example.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.example.model.Mensagem;
import org.example.utils.MensagemHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

public class DefinicaoPassos {

    private Response response;

    private Mensagem mensagemResponse;

    private String ENDPOINT_MENSAGENS = "http://localhost:8080/mensagens";

    @Quando("submeter uma nova mensagem")
    public Mensagem submeterNovaMensagem() {
        var mensagemRequest = MensagemHelper.gerarMensagemRequest();
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(mensagemRequest)
                .when().post(ENDPOINT_MENSAGENS);
        return response.then().extract().as(Mensagem.class);
    }

    @Então("a mensagem é registrada com sucesso")
    public void mensagemRegistradaComSucesso() {
        response.then()
                .statusCode(HttpStatus.CREATED.value())
                .body(matchesJsonSchemaInClasspath("./schemas/MensagemResponseSchema.json"));
    }

    @Dado("que uma mensagem já foi publicada")
    public void mensagemJaPublicada() {
        mensagemResponse = submeterNovaMensagem();
    }

    @Quando("requisitar a busca da mensagem")
    public void requisitarBuscarMensagem() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/mensagens/{id}", mensagemResponse.getId().toString());
    }

    @Então("a mensagem é exibida com sucesso")
    public void mensagemExibidaComSucesso() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body(matchesJsonSchemaInClasspath("./schemas/MensagemResponseSchema.json"));
    }

    @Quando("requisitar a lista da mensagem")
    public void requisitarListaMensagens() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/mensagens");
    }

    @Então("as mensagens são exibidas com sucesso")
    public void mensagensSaoExibidasComSucesso() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body(matchesJsonSchemaInClasspath("./schemas/MensagemPaginationSchema.json"))
                .body("number", equalTo(0))
                .body("size", equalTo(10));
    }

    @Quando("requisitar a alteração da mensagem")
    public void requisitarAlteracaoDaMensagem() {
        mensagemResponse.setConteudo("novo conteudo");
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(mensagemResponse)
                .when()
                .put("/mensagens/{id}", mensagemResponse.getId().toString());
    }

    @Então("a mensagem é atualizada com sucesso")
    public void mensagemAtualizadaComSucesso() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body(matchesJsonSchemaInClasspath("./schemas/MensagemResponseSchema.json"));
    }

    @Quando("requisitar a exclusão da mensagem")
    public void requisitarExclusaoDaMensagem() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete("/mensagens/{id}", mensagemResponse.getId().toString());
    }

    @Então("a mensagem é removida com sucesso")
    public void mensagemRemovidaComSucesso() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("mensagem removida"));
    }
}
