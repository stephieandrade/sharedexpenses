package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.users.application.UserSummary;

import java.util.List;

public record GroupDetail(Group group, List<UserSummary> members) {
}
