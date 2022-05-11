package space.seasearch.spring.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TelegramInputDto {

  @NotBlank(message = "Телефон не может быть пустым!")
  @Pattern(regexp = "^\\d+$", message = "Телефон должен состоять только из чисел!")
  private String phoneNumber = null;
  private String code = null;
  private String password = null;
  /**
   * Шаги авторизации.
   */
  private Boolean[] steps = new Boolean[]{true, false, false};
}
