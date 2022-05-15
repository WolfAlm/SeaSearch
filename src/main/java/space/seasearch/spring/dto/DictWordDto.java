package space.seasearch.spring.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DictWordDto {

    private String word;

    private long count;
}
