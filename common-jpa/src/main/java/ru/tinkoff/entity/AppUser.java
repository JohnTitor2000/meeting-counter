package ru.tinkoff.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.tinkoff.entity.enums.UserState;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long telegramUserId;
    @CreationTimestamp
    LocalDateTime firstLoginDate;
    private String firstName;
    private String lastName;
    private String userName;
    @Enumerated(EnumType.STRING)
    private UserState state = UserState.WAITING;
}
