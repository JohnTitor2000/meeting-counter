package ru.tinkoff.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.tinkoff.entity.enums.Product;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="meeting")
public class Meeting {
    @Id
    String id;
    @ManyToOne
    @JoinColumn(name = "telegram_user_id")
    AppUser user;
    @Enumerated(EnumType.STRING)
    Product product;
    @CreationTimestamp
    LocalDateTime wasHeld;
    String offers;
}
