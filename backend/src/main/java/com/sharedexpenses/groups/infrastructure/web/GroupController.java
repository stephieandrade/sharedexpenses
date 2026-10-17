package com.sharedexpenses.groups.infrastructure.web;

import com.sharedexpenses.groups.application.AddMemberUseCase;
import com.sharedexpenses.groups.application.CreateGroupUseCase;
import com.sharedexpenses.groups.application.GetGroupDetailUseCase;
import com.sharedexpenses.groups.application.ListMyGroupsUseCase;
import com.sharedexpenses.groups.application.RemoveMemberUseCase;
import com.sharedexpenses.shared.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Grupos", description = "Grupos de gastos y sus miembros")
public class GroupController {

    private final ListMyGroupsUseCase listMyGroups;
    private final CreateGroupUseCase createGroup;
    private final GetGroupDetailUseCase getGroupDetail;
    private final AddMemberUseCase addMember;
    private final RemoveMemberUseCase removeMember;

    public GroupController(ListMyGroupsUseCase listMyGroups, CreateGroupUseCase createGroup,
                           GetGroupDetailUseCase getGroupDetail, AddMemberUseCase addMember,
                           RemoveMemberUseCase removeMember) {
        this.listMyGroups = listMyGroups;
        this.createGroup = createGroup;
        this.getGroupDetail = getGroupDetail;
        this.addMember = addMember;
        this.removeMember = removeMember;
    }

    @GetMapping
    @Operation(summary = "Lista los grupos del usuario", description = "Los más recientes primero")
    public List<GroupSummaryResponse> list(@CurrentUserId Long userId) {
        return listMyGroups.execute(userId).stream().map(GroupSummaryResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un grupo", description = "El usuario que lo crea es su primer miembro")
    public GroupSummaryResponse create(@CurrentUserId Long userId, @Valid @RequestBody CreateGroupRequest request) {
        return GroupSummaryResponse.from(createGroup.execute(new CreateGroupUseCase.Command(
                request.name(), request.description(), request.currency(), userId)));
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Detalle de un grupo con sus miembros")
    public GroupDetailResponse detail(@CurrentUserId Long userId, @PathVariable Long groupId) {
        return GroupDetailResponse.from(getGroupDetail.execute(groupId, userId));
    }

    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Añade un miembro por email", description = "El usuario debe estar registrado")
    public MemberResponse addMember(@CurrentUserId Long userId, @PathVariable Long groupId,
                                    @Valid @RequestBody AddMemberRequest request) {
        return MemberResponse.from(addMember.execute(groupId, userId, request.email()));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un miembro o abandona el grupo",
            description = "Solo el creador puede eliminar a otros miembros. El miembro debe tener balance 0")
    public void removeMember(@CurrentUserId Long userId, @PathVariable Long groupId, @PathVariable Long memberId) {
        removeMember.execute(groupId, userId, memberId);
    }
}
