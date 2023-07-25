
package org.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class Mensagem {

  @Id
  @GenericGenerator(name = "uuid")
  private UUID id;

  @NotEmpty(message = "usuário não pode estar vazio")
  private String usuario;

  @NotEmpty(message = "conteúdo não pode estar vazio")
  private String conteudo;

  @CreationTimestamp
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSS")
  private LocalDateTime dataCriacao;

  @CreationTimestamp
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSS")
  private LocalDateTime dataAlteracao;

  @Default
  private int gostei = 0;

  @PrePersist
  public void prePersist() {
    var timestamp = LocalDateTime.now();
    dataCriacao = timestamp;
    dataAlteracao = timestamp;
  }
}

