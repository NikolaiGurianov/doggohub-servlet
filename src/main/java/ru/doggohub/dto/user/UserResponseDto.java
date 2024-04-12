package ru.doggohub.dto.user;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private List<Long> dogs;
}
