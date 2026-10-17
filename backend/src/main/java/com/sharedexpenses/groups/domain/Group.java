package com.sharedexpenses.groups.domain;

import com.sharedexpenses.shared.domain.ForbiddenException;
import com.sharedexpenses.shared.domain.Guard;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Group {

    public static final String DEFAULT_CURRENCY = "EUR";

    private final Long id;
    private final String name;
    private final String description;
    private final String currency;
    private final Long creatorId;
    private final Instant createdAt;
    private final List<Member> members;
    private final Long version;

    public Group(Long id, String name, String description, String currency, Long creatorId,
                 Instant createdAt, List<Member> members, Long version) {
        this.id = id;
        this.name = Guard.requiredText(name, "El nombre del grupo es obligatorio");
        this.description = description == null || description.isBlank() ? null : description.strip();
        this.currency = normalizeCurrency(currency);
        this.creatorId = Guard.required(creatorId, "El creador del grupo es obligatorio");
        this.createdAt = Guard.required(createdAt, "La fecha de creación es obligatoria");
        this.members = new ArrayList<>(Guard.required(members, "Los miembros son obligatorios"));
        this.version = version;
    }

    public static Group create(String name, String description, String currency, Long creatorId, Instant now) {
        return new Group(null, name, description, currency, creatorId, now,
                List.of(new Member(creatorId, now)), null);
    }

    public boolean isMember(Long userId) {
        return members.stream().anyMatch(m -> m.userId().equals(userId));
    }

    public boolean isCreator(Long userId) {
        return creatorId.equals(userId);
    }

    public void requireMembers(Collection<Long> userIds) {
        List<Long> outsiders = userIds.stream().filter(id -> !isMember(id)).sorted().toList();
        Guard.check(outsiders.isEmpty(), "Usuarios que no son miembros del grupo: " + outsiders);
    }

    public void addMember(Long userId, Instant now) {
        Guard.check(!isMember(userId), "El usuario ya es miembro del grupo");
        members.add(new Member(userId, now));
    }

    public void checkCanRemove(Long userId, Long requesterId) {
        Guard.check(isMember(userId), "El usuario no es miembro del grupo");
        if (!userId.equals(requesterId) && !isCreator(requesterId)) {
            throw new ForbiddenException("Solo el creador del grupo puede eliminar a otros miembros");
        }
        Guard.check(!isCreator(userId), "El creador no puede salir del grupo");
    }

    public void removeMember(Long userId, Long requesterId, BigDecimal balance) {
        checkCanRemove(userId, requesterId);
        Guard.check(balance.signum() == 0, "No se puede eliminar a un miembro con balance distinto de 0");
        members.removeIf(m -> m.userId().equals(userId));
    }

    private static String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return DEFAULT_CURRENCY;
        }
        String code = currency.strip().toUpperCase();
        Guard.check(code.matches("[A-Z]{3}"), "La moneda debe ser un código ISO de 3 letras");
        return code;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrency() {
        return currency;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Member> getMembers() {
        return List.copyOf(members);
    }

    public Set<Long> getMemberIds() {
        return members.stream().map(Member::userId).collect(Collectors.toUnmodifiableSet());
    }

    public Long getVersion() {
        return version;
    }
}
