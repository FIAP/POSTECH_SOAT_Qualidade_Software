# language: pt
Funcionalidade: API - Mensagens

  @smoke
  Cenário: Registrar uma nova mensagem
    Quando submeter uma nova mensagem
    Então a mensagem é registrada com sucesso

  Cenário: Buscar uma mensagem existente
    Dado que uma mensagem já foi publicada
    Quando requisitar a busca da mensagem
    Então a mensagem é exibida com sucesso

  Cenário: Listar mensagens existente
    Dado que uma mensagem já foi publicada
    Quando requisitar a lista da mensagem
    Então as mensagens são exibidas com sucesso

  Cenário: Alterar uma mensagem existente
    Dado que uma mensagem já foi publicada
    Quando requisitar a alteração da mensagem
    Então a mensagem é atualizada com sucesso

  Cenário: Excluir uma mensagem existente
    Dado que uma mensagem já foi publicada
    Quando requisitar a exclusão da mensagem
    Então a mensagem é removida com sucesso

  @ignore
  Cenário: Em desenvolvimento
    Dado passo em desenvolvimento