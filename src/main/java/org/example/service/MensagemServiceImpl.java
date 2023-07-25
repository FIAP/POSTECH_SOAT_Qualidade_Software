
package org.example.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.exception.MensagemNotFoundException;
import org.example.model.Mensagem;
import org.example.repository.MensagemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MensagemServiceImpl implements MensagemService {

  private final MensagemRepository mensagemRepository;

  @Override
  public Mensagem criarMensagem(Mensagem mensagem) {
    mensagem.setId(UUID.randomUUID());
    return mensagemRepository.save(mensagem);
  }

  @Override
  public Mensagem buscarMensagem(UUID id) {
    return mensagemRepository.findById(id)
        .orElseThrow(() -> new MensagemNotFoundException("mensagem não encontrada"));
  }

  @Override
  public Mensagem alterarMensagem(UUID id, Mensagem mensagemAtualizada) {
    var mensagem = buscarMensagem(id);
    if (!mensagem.getId().equals(mensagemAtualizada.getId())) {
      throw new MensagemNotFoundException("mensagem não apresenta o ID correto");
    }
    mensagem.setDataAlteracao(LocalDateTime.now());
    mensagem.setConteudo(mensagemAtualizada.getConteudo());
    return mensagemRepository.save(mensagem);
  }

  @Override
  public boolean apagarMensagem(UUID id) {
    var mensagem = buscarMensagem(id);
    mensagemRepository.delete(mensagem);
    return true;
  }

  @Override
  public Mensagem incrementarGostei(UUID id) {
    var mensagem = buscarMensagem(id);
    mensagem.setGostei(mensagem.getGostei() + 1);
    return mensagemRepository.save(mensagem);
  }

  @Override
  public Page<Mensagem> listarMensagens(Pageable pageable) {
    return mensagemRepository.listarMensagens(pageable);
  }
}
