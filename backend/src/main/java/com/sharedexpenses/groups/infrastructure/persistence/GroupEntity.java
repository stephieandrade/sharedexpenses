package com.sharedexpenses.groups.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expense_groups")
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ElementCollection
    @CollectionTable(name = "group_members", joinColumns = @JoinColumn(name = "group_id"))
    @OrderBy("joinedAt ASC")
    private List<MemberEmbeddable> members = new ArrayList<>();

    @Version
    private Long version;

    protected GroupEntity() {
    }

    GroupEntity(Long id, String name, String description, String currency, Long creatorId,
                Instant createdAt, List<MemberEmbeddable> members, Long version) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.members = new ArrayList<>(members);
        this.version = version;
    }

    Long getId() {
        return id;
    }

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    String getCurrency() {
        return currency;
    }

    Long getCreatorId() {
        return creatorId;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    List<MemberEmbeddable> getMembers() {
        return members;
    }

    Long getVersion() {
        return version;
    }
}
