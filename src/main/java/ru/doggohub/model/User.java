package ru.doggohub.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class User {
    private Long id;
    private String name;
    private String email;
    private List<Long> dogIds;
}
