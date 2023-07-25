package org.example.performance;

import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ApiPerformanceSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .header("Content-Type", "application/json");

    ActionBuilder adicinarMensagemRequest = http("adicionar mensagem")
            .post("/mensagens")
            .body(StringBody("{ \"usuario\": \"user\", \"conteudo\": \"demo\" }"))
            .check(status().is(201))
            .check(jsonPath("$.id").saveAs("mensagemId"));

    ActionBuilder buscarMensagemRequest = http("buscar mensagem")
            .get("/mensagens/#{mensagemId}")
            .check(status().is(200));

    ActionBuilder listarMensagemRequest = http("listar mensagens")
            .get("/mensagens")
            .queryParam("page", "0")
            .queryParam("size", "10")
            .check(status().is(200));

    ActionBuilder removerMensagemRequest = http("remover mensagem")
            .delete("/mensagens/#{mensagemId}")
            .check(status().is(200));

    ScenarioBuilder cenarioAdicionarMensagem = scenario("Adicionar mensagem")
            .exec(adicinarMensagemRequest);

    ScenarioBuilder cenarioListarMensagem = scenario("Listar mensagens")
            .exec(listarMensagemRequest);

    ScenarioBuilder cenarioAdicionarBuscarMensagem = scenario("Adicionar e Buscar mensagem")
            .exec(adicinarMensagemRequest)
            .exec(buscarMensagemRequest);

    ScenarioBuilder cenarioAdicionarRemoverMensagem = scenario("Adicionar e Remover mensagem")
            .exec(adicinarMensagemRequest)
            .exec(removerMensagemRequest);

    {
        setUp(
                cenarioAdicionarMensagem.injectOpen(
                        rampUsersPerSec(1)
                                .to(10)
                                .during(Duration.ofSeconds(10)),
                        constantUsersPerSec(10)
                                .during(Duration.ofSeconds(60)),
                        rampUsersPerSec(10)
                                .to(1)
                                .during(Duration.ofSeconds(10))),
                cenarioAdicionarRemoverMensagem.injectOpen(
                        rampUsersPerSec(1)
                                .to(30)
                                .during(Duration.ofSeconds(10)),
                        constantUsersPerSec(30)
                                .during(Duration.ofSeconds(60)),
                        rampUsersPerSec(30)
                                .to(1)
                                .during(Duration.ofSeconds(10))),
                cenarioAdicionarBuscarMensagem.injectOpen(
                        rampUsersPerSec(1)
                                .to(30)
                                .during(Duration.ofSeconds(10)),
                        constantUsersPerSec(30)
                                .during(Duration.ofSeconds(60)),
                        rampUsersPerSec(30)
                                .to(1)
                                .during(Duration.ofSeconds(10))),
                cenarioListarMensagem.injectOpen(
                        rampUsersPerSec(1)
                                .to(100)
                                .during(Duration.ofSeconds(10)),
                        constantUsersPerSec(100)
                                .during(Duration.ofSeconds(60)),
                        rampUsersPerSec(100)
                                .to(1)
                                .during(Duration.ofSeconds(10))))
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().max().lt(50),
                        global().failedRequests().count().is(0L));
    }
}
